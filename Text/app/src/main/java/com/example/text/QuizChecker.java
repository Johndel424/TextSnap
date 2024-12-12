package com.example.text;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuizChecker extends AppCompatActivity {
    Button sendButton;
    LinearLayout layoutt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_checker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        layoutt = findViewById(R.id.chatt);
        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonCallGeminiAPIWithLastMessage(v);
            }
        });

    }
    public void buttonCallGeminiAPIWithLastMessage(View view) {

        // Get the text from the EditText
        EditText editText = findViewById(R.id.message_edit_text); // Replace with your EditText ID
        String editTextValue = editText.getText().toString().trim();

        if (!editTextValue.isEmpty()) { // Ensure EditText is not empty

            // Initialize Generative Model
            GenerativeModel gm = new GenerativeModel("gemini-pro", "AIzaSyATC9_D4LfonMbXInRlkvdOhLEFzsQVKhI");
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            // Content to be generated based only on the EditText input
            Content content = new Content.Builder()
                    .addText("Copy this text ang check if the answer is right if right put a check if wrong put an ex also compute all items, and put a score for example 5/10" + editTextValue)  // Use the user input directly
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        // Get the generated content
                        String resultText = result.getText();
                        resultText = resultText.replace("*", ""); // Remove any '*' characters


                        // Find the layout and TextView where the result will be displayed
                        LinearLayout resultLayout = findViewById(R.id.result_layout);  // Your layout ID
                        TextView resultTextView = findViewById(R.id.result_textview);  // Your TextView ID

                        // Make the layout visible and set the result in the TextView
                        resultLayout.setVisibility(View.VISIBLE);
                        layoutt.setVisibility(View.GONE);
                        resultTextView.setText(resultText);

                        // Clear the EditText after the message is sent
                        editText.setText("");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("APIError", "Failed to generate content", t);
                    }
                }, getMainExecutor());
            }
        } else {
            // If the EditText is empty, notify the user (e.g., using a Toast)
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }



}