package com.example.text;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.text.HomeActivity.Home;
import com.example.text.HomeActivity.HomeAdapter;
import com.example.text.HomeActivity.HomeAdapter2;
import com.example.text.HomeActivity.HomeModel;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiFeature extends Fragment {
    private FirebaseAuth mAuth;
    String textValue,selectedItem,type,title;

    String currentUserUid;
    ImageButton sendButton,Button;
    private HomeAdapter2 adapter;
    private EditText messageEditText;
    private DatabaseReference usersRef;
    private DatabaseReference mDatabase;
    private List<HomeModel> messageList;
    String uid;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ai_feature, container, false);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Initialize Views
        recyclerView = view.findViewById(R.id.recycler_view);
        messageEditText = view.findViewById(R.id.message_edit_text);
        sendButton = view.findViewById(R.id.send_button);
        // Add a TextWatcher to listen for changes in messageEditText
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if there is text in the messageEditText
                if (s.toString().trim().isEmpty()) {
                    sendButton.setVisibility(View.GONE); // Hide send button if no text
                } else {
                    sendButton.setVisibility(View.VISIBLE); // Show send button if there is text
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonCallGeminiAPI(v);
                sendMessage();
            }
        });

        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        adapter = new HomeAdapter2(getContext(), messageList, uid);
        // Set the layout manager to stack from the end
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Load existing messages from the chat room
        loadMessages();

        // Scroll to the bottom after the layout is complete
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                recyclerView.removeOnLayoutChangeListener(this);
                if (adapter.getItemCount() > 0) {
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        });




        return view;
    }
    private void loadMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("permanent")
                .child(uid);

        messagesRef.limitToLast(50).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HomeModel message = dataSnapshot.getValue(HomeModel.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }

                adapter.notifyDataSetChanged();

                // Scroll to the bottom of the RecyclerView, if the list is not empty
                if (!messageList.isEmpty()) {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buttonCallGeminiAPI(View view) {
        // Get the question from EditText
        String question = messageEditText.getText().toString();
        String sender = "ai";

        long timestamp = System.currentTimeMillis();

        GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyATC9_D4LfonMbXInRlkvdOhLEFzsQVKhI");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Add the question to the content for API call
        Content content = new Content.Builder()
                .addText(question)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText().replace("*", "").replace("#", "");


                    // Prepare data to save in Firebase
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("sender", sender);
                    messageData.put("uid", uid);
                    messageData.put("timestamp", timestamp);
                    messageData.put("message", resultText);

                    // Save to Firebase under "permanent/{userUID}" node
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("permanent");
                    databaseRef.child(uid).push().setValue(messageData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("Firebase", "Response saved successfully");
                        } else {
                            Log.e("Firebase", "Failed to save response", task.getException());
                        }
                    });
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("APIError", "Failed to generate content", t);
                    //recycler_view.setText("Failed to get response");
                }
            }, getActivity().getMainExecutor());
        }
    }
    private void sendMessage() {
        // Get message text from EditText
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Generate unique message ID
            String messageId = FirebaseDatabase.getInstance().getReference("permanent").child(uid).push().getKey();
            saveMessageToFirebase(uid, messageId, "user", messageText);
            messageEditText.setText("");
        } else {
            messageEditText.setError("Message is required");
            messageEditText.requestFocus();
        }
    }

    private void saveMessageToFirebase(String uid, String messageId, String sender, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("permanent").child(uid);
        long timestamp = System.currentTimeMillis();
        ConvoMessage convoMessage = new ConvoMessage(sender, message, messageId, timestamp);
        databaseReference.child(messageId).setValue(convoMessage)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Message saved successfully under chatRoomId: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving message: ", e);
                });
    }


}