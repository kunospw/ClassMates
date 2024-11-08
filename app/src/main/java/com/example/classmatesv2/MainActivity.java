package com.example.classmatesv2;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private GenerativeModel model;
    private DatabaseReference mDatabase;
    private ExecutorService executorService;
    private static final String API_KEY = "AIzaSyDKiODWjr5mZ3-9Q3v56kYu3KfoUoPA7Ms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("messages");

        // Initialize Gemini AI model
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessage(model));
    }

    private void sendMessage(GenerativeModelFutures model) {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Clear input and show user message
        messageInput.setText("");
        Message userMessage = new Message(messageText, true);
        adapter.addMessage(userMessage);

        // Save user message to Firebase
        String key = mDatabase.push().getKey();
        if (key != null) {
            mDatabase.child(key).setValue(userMessage);
        }

        // Generate AI response using Gemini
        Content content = new Content.Builder().addText(messageText).build();
        ListenableFuture<GenerateContentResponse> future = model.generateContent(content);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText();
                if (aiResponse == null || aiResponse.isEmpty()) {
                    aiResponse = "I apologize, but I couldn't generate a response.";
                }

                Message aiMessage = new Message(aiResponse, false);

                // Update UI with AI response
                runOnUiThread(() -> {
                    adapter.addMessage(aiMessage);
                    recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                });

                // Save AI response to Firebase
                String aiKey = mDatabase.push().getKey();
                if (aiKey != null) {
                    mDatabase.child(aiKey).setValue(aiMessage);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Message errorMessage = new Message("Sorry, I couldn't process that request: " + t.getMessage(), false);
                    adapter.addMessage(errorMessage);
                });
            }
        }, executorService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
