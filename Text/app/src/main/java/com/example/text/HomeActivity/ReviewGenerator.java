package com.example.text.HomeActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.text.ConvoMessage;
import com.example.text.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ReviewGenerator extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageView circle1, circle2;
    private RelativeLayout progressBar;
    String textValue,selectedItem,type,title,chatRoomId;
    private TextView loadingText;
    private Handler handler;
    private int dotCount = 0;
    private RecyclerView recyclerView;
    String currentUserUid;
    ImageButton sendButton,Button;
    private HomeAdapter adapter;
    private EditText messageEditText;
    private DatabaseReference usersRef;
    private DatabaseReference mDatabase;
    private List<HomeModel> messageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_generator);
        Intent intent = getIntent();
        if (intent != null) {
            textValue = intent.getStringExtra("textValue");
            selectedItem = intent.getStringExtra("selectedItem");
            type = intent.getStringExtra("selectedReason");
            title = intent.getStringExtra("title");
            chatRoomId = intent.getStringExtra("chatRoomId");
        }
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        recyclerView = findViewById(R.id.recycler_view);

        Button = findViewById(R.id.Button);
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the new activity
                Intent intent = new Intent(ReviewGenerator.this, Home.class);
                startActivity(intent);
            }
        });
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        adapter = new HomeAdapter(this, messageList, chatRoomId);
        // Set the layout manager to stack from the end
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


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
        loadingText = findViewById(R.id.loadingText);

        handler = new Handler();
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        progressBar = findViewById(R.id.progressBar);

        // Set OnClickListener to trigger the function when image is clicked
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the function when the image is clicked
                buttonCallGeminiAPIWithLastMessage(v);
            }
        });
        buttonCallGeminiAPI(findViewById(android.R.id.content));


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        String currentUserUID = user.getUid();

    }

public void buttonCallGeminiAPI(View view) {
    showLoading(); // Show loading indicator

    // Initialize Generative Model
    GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyATC9_D4LfonMbXInRlkvdOhLEFzsQVKhI");
    GenerativeModelFutures model = GenerativeModelFutures.from(gm);

    // Content to be generated (text and bullet type)
    Content content = new Content.Builder()
            .addText("Create a study reviewer based on this text. Include the main points, definitions, and examples. Make it easier to " +
                    "understand and more useful for studying. " + textValue + ". Make this in " + selectedItem + " language in" + type + "Make a quiz from the text atleast 3")
            .build();

    ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {

                String resultText = result.getText();

                // Check if the selected item type is not bullet before removing '*' and '#'
                if (selectedItem.equals("Paragraph Type")) {
                    // Remove all '*' and '#' characters from resultText
                    resultText = resultText.replace("*", "").replace("#", "");

                    // Display the result in the textView
                    // textView.setText(resultText);
                    hideLoading();
                    // Load existing messages from the chat room
                    loadMessages();
                    // Generate a new messageId for the message in 'convo' node
                    String messageId = FirebaseDatabase.getInstance().getReference("convo").child(chatRoomId).push().getKey();

                    // Step 2: Save to Firebase with chatRoomId and messageId
                    if (messageId != null) {
                        saveMessageToFirebase(chatRoomId, messageId, "ai", resultText); // Pass the chatRoomId and messageId here

                        // Step 3: Update the lastMessage in the 'chatroom' node with the generated resultText
                        FirebaseDatabase.getInstance().getReference("chatrooms")
                                .child(chatRoomId)
                                .child("lastMessage")
                                .setValue(resultText)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Successfully updated the lastMessage in chatroom node
                                        Log.d("Firebase", "Last message updated successfully.");
                                    } else {
                                        // Failed to update the lastMessage
                                        Log.e("Firebase", "Failed to update last message.", task.getException());
                                    }
                                });
                    }
                }
                else {
                    resultText = resultText.replace("*", "").replace("#", "");
                    hideLoading();
                    // Load existing messages from the chat room
                    loadMessages();
                    // Generate a new messageId for the message in 'convo' node
                    String messageId = FirebaseDatabase.getInstance().getReference("convo").child(chatRoomId).push().getKey();

                    // Step 2: Save to Firebase with chatRoomId and messageId
                    if (messageId != null) {
                        saveMessageToFirebase(chatRoomId, messageId, "ai", resultText); // Pass the chatRoomId and messageId here

                        // Step 3: Update the lastMessage in the 'chatroom' node with the generated resultText
                        FirebaseDatabase.getInstance().getReference("chatrooms")
                                .child(chatRoomId)
                                .child("lastMessage")
                                .setValue(resultText)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Successfully updated the lastMessage in chatroom node
                                        Log.d("Firebase", "Last message updated successfully.");
                                    } else {
                                        // Failed to update the lastMessage
                                        Log.e("Firebase", "Failed to update last message.", task.getException());
                                    }
                                });
                    }
                }



            }

            @Override
            public void onFailure(Throwable t) {
                //textView.setText(t.toString());
                hideLoading();
            }
        }, this.getMainExecutor());
    }
}
    private void loadMessages() {
        // Check if chatRoomId is null or empty
        if (chatRoomId == null || chatRoomId.isEmpty()) {
            Log.e("ReviewGenerator", "Error: chatRoomId is null or empty.");
            Toast.makeText(this, "Chat room ID is not available.", Toast.LENGTH_SHORT).show();
            return; // Exit the method if chatRoomId is invalid
        }

        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("convo")
                .child(chatRoomId);

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
                Toast.makeText(ReviewGenerator.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void buttonCallGeminiAPIWithLastMessage(View view) {
        showLoading(); // Show loading indicator

        // Get the text from the EditText
        EditText editText = findViewById(R.id.message_edit_text); // Replace with your EditText ID
        String editTextValue = editText.getText().toString().trim();

        if (!editTextValue.isEmpty()) { // Ensure EditText is not empty
            // If message is not empty, send message
            sendMessage();

            // Reference to chatroom and last message
            DatabaseReference chatroomRef = mDatabase.child("chatrooms").child(chatRoomId);
            DatabaseReference lastMessageRef = FirebaseDatabase.getInstance()
                    .getReference("chatrooms").child(chatRoomId).child("lastMessage");

            // Retrieve the lastMessage from Firebase
            lastMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String previousResultText = dataSnapshot.getValue(String.class); // Retrieve last message

                        // Initialize Generative Model
                        GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyATC9_D4LfonMbXInRlkvdOhLEFzsQVKhI");
                        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                        // Content to be generated based on the last message and EditText input
                        Content content = new Content.Builder()
                                .addText("Based on the previous result: " + previousResultText + ", I want " + editTextValue)
                                .build();

                        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                                @Override
                                public void onSuccess(GenerateContentResponse result) {
                                    // Get the generated content
                                    String resultText = result.getText();
                                    resultText = resultText.replace("*", ""); // Remove any '*' characters

                                    hideLoading(); // Hide loading indicator

                                    // Generate messageId and save to Firebase
                                    String messageId = FirebaseDatabase.getInstance().getReference("convo")
                                            .child(chatRoomId).push().getKey();

                                    if (messageId != null) {
                                        saveMessageToFirebase(chatRoomId, messageId, "ai", resultText); // Save message

                                        // Update the last message in Firebase
                                        lastMessageRef.setValue(resultText);

                                        // Clear the EditText after the message is sent
                                        editText.setText("");
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    hideLoading(); // Hide loading indicator on failure
                                    Log.e("APIError", "Failed to generate content", t);
                                }
                            }, getMainExecutor());
                        }
                    } else {
                        hideLoading();
                        Log.e("DataError", "No previous message found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    hideLoading();
                    Log.e("FirebaseError", "onCancelled", databaseError.toException());
                }
            });
        } else {
            hideLoading();
            // If the EditText is empty, notify the user (e.g., using a Toast)
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        // Get message text from EditText
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Generate unique message ID
            String messageId = FirebaseDatabase.getInstance().getReference("convo").child(chatRoomId).push().getKey();
            saveMessageToFirebase(chatRoomId, messageId, "user", messageText);
        } else {
            messageEditText.setError("Message is required");
            messageEditText.requestFocus();
        }
    }
    private void saveMessageToFirebase(String chatroomId, String messageId, String sender, String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("convo").child(chatroomId);
        long timestamp = System.currentTimeMillis();
        ConvoMessage convoMessage = new ConvoMessage(sender, message, messageId, timestamp);
        databaseReference.child(messageId).setValue(convoMessage)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Message saved successfully under chatroomId: " + chatroomId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error saving message: ", e);
                });
    }



    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        animateLoadingText();
        animateCircles();
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        // Optionally, you can enable other UI elements here
    }
    private void animateCircles() {
        // Get screen width for determining the travel distance
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Circle 1 Animation (move to the right)
        ObjectAnimator animCircle1 = ObjectAnimator.ofFloat(circle1, "translationX", 0, screenWidth / 2 - 100);
        animCircle1.setRepeatCount(ValueAnimator.INFINITE);
        animCircle1.setRepeatMode(ValueAnimator.REVERSE);

        // Circle 2 Animation (move to the left)
        ObjectAnimator animCircle2 = ObjectAnimator.ofFloat(circle2, "translationX", 0, -(screenWidth / 2 - 100));
        animCircle2.setRepeatCount(ValueAnimator.INFINITE);
        animCircle2.setRepeatMode(ValueAnimator.REVERSE);

        // Start the animations
        animCircle1.setDuration(1000);
        animCircle2.setDuration(1000);
        animCircle1.start();
        animCircle2.start();
    }

    private void animateLoadingText() {
        handler.postDelayed(loadingTextRunnable, 500); // Start after 500ms delay
    }

    // Runnable for animating the loading text with dots
    private final Runnable loadingTextRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the text based on the current dot count
            StringBuilder text = new StringBuilder("Loading");
            for (int i = 0; i < dotCount; i++) {
                text.append(".");
            }

            // Update the TextView
            loadingText.setText(text.toString());

            // Update dot count (reset if it reaches 3)
            dotCount = (dotCount + 1) % 4;

            // Repeat every 500ms
            handler.postDelayed(this, 500);
        }
    };
    @Override
    public void onBackPressed() {
        if (adapter != null) {
            adapter.stopTextToSpeech();
        }
        super.onBackPressed();
    }

}