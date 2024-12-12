package com.example.text.HomeActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.text.AiFeature;
import com.example.text.AnotherFeature;
import com.example.text.HistoryActivity.History;
import com.example.text.ProfileActivity.Account;
import com.example.text.QuizChecker;
import com.example.text.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.HashMap;

public class Home extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private ImageView imageView;
    private TextView textView, buttonSubmit,buttonCancel;
    private TextView submitButton;
    Bitmap imageBitmap;
    String selectedItem;
    ScrollView secondPage;
    RelativeLayout firstPage;
    ImageButton home, history, profile,Button,copy,ai,save,quiz;
    RelativeLayout homeLayout;
    FrameLayout frameLayout;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        quiz = findViewById(R.id.quiz);
        copy = findViewById(R.id.copy);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToCopy = textView.getText().toString();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("copiedText", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        Button = findViewById(R.id.Button);
        frameLayout = findViewById(R.id.frameLayout);
        homeLayout = findViewById(R.id.homeLayout);
        profile = findViewById(R.id.profile);
        home = findViewById(R.id.home);
        history = findViewById(R.id.history);
        ai = findViewById(R.id.ai);
        save = findViewById(R.id.save);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        ImageButton openCameraButton = findViewById(R.id.openCameraButton);
        ImageButton openGalleryButton = findViewById(R.id.openGalleryButton);
        submitButton = findViewById(R.id.myButton);
        firstPage = findViewById(R.id.firstPage);
        secondPage = findViewById(R.id.secondPage);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeLayout.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                profile.setImageResource(R.drawable.menu_profile_unselected);
                home.setImageResource(R.drawable.menu_home_selected);
                history.setImageResource(R.drawable.menu_history_unselected);
                save.setImageResource(R.drawable.menu_download_unselected);
                ai.setImageResource(R.drawable.menu_ai_unselected);
            }
        });
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the new activity
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });
        quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the new activity
                Intent intent = new Intent(Home.this, QuizChecker.class);
                startActivity(intent);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the fragment when the image is clicked
                homeLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                profile.setImageResource(R.drawable.menu_profile_selected);
                home.setImageResource(R.drawable.menu_home_unselected);
                history.setImageResource(R.drawable.menu_history_unselected);
                save.setImageResource(R.drawable.menu_download_unselected);
                ai.setImageResource(R.drawable.menu_ai_unselected);
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(currentFragment)
                            .commit();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new Account())
                        .addToBackStack(null)
                        .commit();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the fragment when the image is clicked
                homeLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                profile.setImageResource(R.drawable.menu_profile_unselected);
                home.setImageResource(R.drawable.menu_home_unselected);
                history.setImageResource(R.drawable.menu_history_unselected);
                save.setImageResource(R.drawable.menu_download_selected);
                ai.setImageResource(R.drawable.menu_ai_unselected);
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(currentFragment)
                            .commit();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new AnotherFeature())
                        .addToBackStack(null)
                        .commit();
            }
        });
        ai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the fragment when the image is clicked
                homeLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                profile.setImageResource(R.drawable.menu_profile_unselected);
                home.setImageResource(R.drawable.menu_home_unselected);
                history.setImageResource(R.drawable.menu_history_unselected);
                save.setImageResource(R.drawable.menu_download_unselected);
                ai.setImageResource(R.drawable.menu_ai_selected);
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(currentFragment)
                            .commit();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new AiFeature())
                        .addToBackStack(null)
                        .commit();
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the fragment when the image is clicked
                homeLayout.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                profile.setImageResource(R.drawable.menu_profile_unselected);
                home.setImageResource(R.drawable.menu_home_unselected);
                history.setImageResource(R.drawable.menu_history_selected);
                save.setImageResource(R.drawable.menu_download_unselected);
                ai.setImageResource(R.drawable.menu_ai_unselected);
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
                if (currentFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(currentFragment)
                            .commit();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frameLayout, new History())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Set listener for the camera button
        openCameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });
        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected item
                selectedItem = parentView.getItemAtPosition(position).toString();
               }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Set listener for the gallery button
        openGalleryButton.setOnClickListener(v -> openGallery());
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the dialog view
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.layout_option, null);

                // Create the AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setView(dialogView)
                        .setCancelable(false); // Prevent dismissing by tapping outside

                // Create the dialog
                AlertDialog alertDialog = builder.create();

                // Find views
                RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupReasons);
                Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
                Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

                // Show the dialog
                alertDialog.show();

                // Handle submit button click
                buttonSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int checkedId = radioGroup.getCheckedRadioButtonId(); // Get the checked radio button ID

                        if (checkedId == -1) {
                            // No radio button is selected
                            Toast.makeText(Home.this, "Please select a format.", Toast.LENGTH_SHORT).show();
                        } else {
                            String selectedReason = "";
                            if (checkedId == R.id.radioNotEligible) {
                                selectedReason = "Paragraph Type";
                            } else if (checkedId == R.id.radioInvalidRequirements) {
                                selectedReason = "STRICKLY BULLET TYPE";
                            }

                            String textValue = textView.getText().toString();

                            if (textValue != null) {
                                String[] lines = textValue.split("\\n");
                                title = lines[0];

                            }


                            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            long timestamp = System.currentTimeMillis();
                            String chatRoomId;

                            chatRoomId = userUid + "_" + timestamp;


                            // Retrieve profile images for each user
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            String finalSelectedReason = selectedReason;

                            usersRef.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Create a HashMap to save chat room details
                                        HashMap<String, Object> chatRoomInfo = new HashMap<>();
                                        chatRoomInfo.put("chatRoomIdTrue", true); // Set the value as true
                                        chatRoomInfo.put("timestamp", timestamp);
                                        chatRoomInfo.put("title", title);
                                        chatRoomInfo.put("userUid", userUid);
                                        chatRoomInfo.put("chatRoomId", chatRoomId);

                                        // Reference to the chat room in the database
                                        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatRoomId);

                                        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                // Chat room does not exist, create it with all details
                                                chatRoomRef.setValue(chatRoomInfo)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Start the ReviewGenerator activity after chat room is created
                                                                Intent intent = new Intent(Home.this, ReviewGenerator.class);
                                                                intent.putExtra("chatRoomId", chatRoomId);
                                                                intent.putExtra("textValue", textValue);
                                                                intent.putExtra("title", title);
                                                                intent.putExtra("selectedItem", selectedItem);
                                                                intent.putExtra("selectedReason", finalSelectedReason);

                                                                alertDialog.dismiss(); // Dismiss the dialog
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(Home.this, "Failed to start chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(Home.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }


                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Home.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                });

                // Handle cancel button click
                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss(); // Dismiss the dialog
                    }
                });
            }
        });


    }

    // Method to open the camera
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // Method to open the gallery
    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_GALLERY_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Create and configure the ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false); // Prevent dismissing the dialog by touching outside

        if (resultCode == RESULT_OK) {
            // Show ProgressDialog while processing the image
            progressDialog.show();

            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // Handle camera image
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");

                // Simulate loading
                new Handler().postDelayed(() -> {
                    // After loading is done, dismiss the dialog and show the second page
                    progressDialog.dismiss();
                    firstPage.setVisibility(View.GONE);
                    secondPage.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(imageBitmap);

                    // Recognize text from the captured image
                    recognizeTextFromImage(imageBitmap);
                }, 2000); // Simulating 2-second loading time

            } else if (requestCode == REQUEST_GALLERY_IMAGE && data != null) {
                // Handle gallery image
                Uri imageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    // Simulate loading
                    new Handler().postDelayed(() -> {
                        // After loading is done, dismiss the dialog and show the second page
                        progressDialog.dismiss();
                        firstPage.setVisibility(View.GONE);
                        secondPage.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(imageBitmap);

                        // Recognize text from the selected image
                        recognizeTextFromImage(imageBitmap);
                    }, 2000); // Simulating 2-second loading time

                } catch (IOException e) {
                    e.printStackTrace();
                    textView.setText("Failed to load image from gallery");
                    progressDialog.dismiss();
                    firstPage.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                textView.setText("Camera permission denied.");
            }
        }
    }

    // Method to recognize text from a Bitmap image
    private void recognizeTextFromImage(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String recognizedText = text.getText();
                    textView.setText(recognizedText);
                })
                .addOnFailureListener(e -> textView.setText("Failed to recognize text"));
    }
    @Override
    public void onBackPressed() {
        // Make the RelativeLayout visible when back is pressed
        super.onBackPressed();
        homeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
        profile.setImageResource(R.drawable.menu_profile_unselected);
        home.setImageResource(R.drawable.menu_home_selected);
        history.setImageResource(R.drawable.menu_history_unselected);
        save.setImageResource(R.drawable.menu_download_unselected);
        ai.setImageResource(R.drawable.menu_ai_unselected);

    }


}
