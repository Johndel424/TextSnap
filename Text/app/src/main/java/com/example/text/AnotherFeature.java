package com.example.text;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.text.HomeActivity.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class AnotherFeature extends Fragment {
    private TextView textViewContent;
    private Button buttonSaveAsPdf;
    private DatabaseReference chatroomsRef;
    private String currentUserUid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_another_feature, container, false);

        textViewContent = view.findViewById(R.id.textViewContent);
        buttonSaveAsPdf = view.findViewById(R.id.buttonSaveAsPdf);
        // Initialize Firebase Database Reference
        chatroomsRef = FirebaseDatabase.getInstance().getReference("chatrooms");

        // Initialize Firebase Authentication and get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserUid = currentUser.getUid(); // Gets the UID of the current user
            fetchLatestMessageForCurrentUser(); // Ensure chatroomsRef is initialized before this call
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
        buttonSaveAsPdf.setOnClickListener(v -> openFilePicker());

        return view;
    }

    private final ActivityResultLauncher<Intent> createPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        saveTextViewAsPdf(uri);
                    }
                }
            }
    );

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "TextViewContent.pdf");
        createPdfLauncher.launch(intent);
    }
    private void saveTextViewAsPdf(Uri uri) {
        PdfDocument pdfDocument = new PdfDocument();

        // Get dimensions based on TextView content
        int width = textViewContent.getWidth();
        int height = textViewContent.getHeight();

        // Create a page with the dimensions of the TextView
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Draw the TextView content on the PDF page
        textViewContent.draw(page.getCanvas());
        pdfDocument.finishPage(page);

        try (OutputStream outputStream = getActivity().getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                Toast.makeText(getContext(), "PDF Saved Successfully", Toast.LENGTH_SHORT).show();

                // Step 4: Go back to MainActivity
                Intent intent = new Intent(getActivity(), Home.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error Saving PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private void fetchLatestMessageForCurrentUser() {
        if (currentUserUid == null) return;

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading latest message...");
        progressDialog.setCancelable(false); // Optional: prevent dismissing by tapping outside
        progressDialog.show();

        // Query to get all messages for the current user, ordered by timestamp
        Query latestMessageQuery = chatroomsRef.orderByChild("userUid").equalTo(currentUserUid);

        latestMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot latestMessageSnapshot = null;

                // Iterate through each chatroom entry for currentUserUid
                for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                    if (latestMessageSnapshot == null ||
                            chatRoomSnapshot.child("timestamp").getValue(Long.class) >
                                    latestMessageSnapshot.child("timestamp").getValue(Long.class)) {
                        latestMessageSnapshot = chatRoomSnapshot;
                    }
                }

                // Dismiss loading dialog
                progressDialog.dismiss();

                if (latestMessageSnapshot != null) {
                    String lastMessage = latestMessageSnapshot.child("lastMessage").getValue(String.class);
                    Long timestamp = latestMessageSnapshot.child("timestamp").getValue(Long.class);

                    // Display the last message and timestamp in TextView
                    textViewContent.setText(lastMessage);
                } else {
                    Toast.makeText(getContext(), "No messages found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Dismiss loading dialog
                progressDialog.dismiss();

                Toast.makeText(getContext(), "Failed to fetch message: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}