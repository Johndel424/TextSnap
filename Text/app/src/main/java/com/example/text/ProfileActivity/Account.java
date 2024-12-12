package com.example.text.ProfileActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.text.MainActivity.MainActivity;
import com.example.text.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Account extends Fragment {
    private TextView emailTextView, fullNameTextView;
    private String buyerUid;
    private ImageView profileImageView;
    LinearLayout changeemail,change_password_button,logout_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        buyerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fullNameTextView = view.findViewById(R.id.name);
        emailTextView = view.findViewById(R.id.email);
        profileImageView = view.findViewById(R.id.avatarImageView);
        changeemail = view.findViewById(R.id.changeemail);
        change_password_button = view.findViewById(R.id.change_password_button);
        logout_button = view.findViewById(R.id.logout_button);
        changeemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // Get the user's provider ID
            List<? extends UserInfo> providerData = user.getProviderData();
            boolean isGoogleUser = false;

            for (UserInfo profile : providerData) {
                if (profile.getProviderId().equals("google.com")) {
                    isGoogleUser = true;
                    break;
                }
            }

            if (isGoogleUser) {
                // Make the button non-clickable
                change_password_button.setClickable(false);
                change_password_button.setEnabled(false); // Also disable it visually if needed
            } else {
                // Make the button clickable
                change_password_button.setClickable(true);
                change_password_button.setEnabled(true);
            }
        }
        changeemail = view.findViewById(R.id.changeemail);
        changeemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_change_email, null);

                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(dialogView);

                // Find the views in the dialog
                EditText newEmailEditText = dialogView.findViewById(R.id.new_email);
                Button saveEmailButton = dialogView.findViewById(R.id.save_email_button);
                Button cancelButton = dialogView.findViewById(R.id.cancelButton);

                // Create the AlertDialog
                AlertDialog dialog = builder.create();

                // Set the save button click listener
                saveEmailButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newEmail = newEmailEditText.getText().toString().trim();

                        if (isValidEmail(newEmail)) {
                            // Call the method to change the email
                            changeEmail(newEmail);
                            dialog.dismiss(); // Close the dialog
                        } else {
                            Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the confirmation dialog
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed logout, proceed with logging out
                                logout();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User cancelled the dialog, do nothing
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        change_password_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the layout for the dialog
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

                // Create the dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(dialogView)
                        .setTitle("Change Password");

                // Find the views in the dialog
                EditText currentPassword = dialogView.findViewById(R.id.current_password);
                EditText newPassword = dialogView.findViewById(R.id.new_password);
                EditText confirmNewPassword = dialogView.findViewById(R.id.confirm_new_password);
                Button savePasswordButton = dialogView.findViewById(R.id.save_password_button);
                Button cancel = dialogView.findViewById(R.id.cancelButton);

                // Create the AlertDialog
                AlertDialog dialog = builder.create();

                // Set the save button click listener
                savePasswordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String currentPass = currentPassword.getText().toString();
                        String newPass = newPassword.getText().toString();
                        String confirmNewPass = confirmNewPassword.getText().toString();

                        // Check if the user is logged in with Google
                        if (isLoggedInWithGoogle()) {
                            Toast.makeText(getContext(), "Cannot change password with Google account", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Close the dialog
                            return; // Exit the click listener
                        }

                        // Proceed with changing the password if not logged in with Google
                        if (newPass.equals(confirmNewPass)) {
                            changePassword(currentPass, newPass);
                            dialog.dismiss(); // Close the dialog
                        } else {
                            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });



        if (user != null) {
            String emailStr = user.getEmail();
            emailTextView.setText(emailStr);
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserUID = mAuth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserUID);

        userRef.addValueEventListener(new ValueEventListener() { // Change here
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profilePicUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                    String userName = dataSnapshot.child("username").getValue(String.class);

                    // Load profile picture using Glide or any image loading library
                    RequestOptions options = new RequestOptions()
                            .circleCrop() // Make the image circular
                            .placeholder(R.drawable.logoo); // Placeholder image while loading
                    Context context = getActivity(); // or getContext(), depending on where you are

                    // Load profile picture using Glide
                    Glide.with(context)
                            .load(profilePicUrl)
                            .apply(options)
                            .into(profileImageView); // Assume profileImageView is your ImageView in the fragment
                    fullNameTextView.setText(userName);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.e("UserProfile", "Database error: " + databaseError.getMessage());
            }
        });




        return view;


    }
    private void changeEmail(String newEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isLoggedInWithGoogle() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Check the provider data to see if the user logged in with Google
            for (UserInfo profile : user.getProviderData()) {
                if (profile.getProviderId().equals("google.com")) {
                    return true; // User is logged in with Google
                }
            }
        }
        return false; // User is not logged in with Google
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if current password or new password is empty
        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(getContext(), "Current password and new password cannot be empty", Toast.LENGTH_SHORT).show();
            return; // Exit the method if validation fails
        }

        if (user != null) {
            // Re-authenticate user
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Change password
                        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Check if the failure is due to an incorrect password
                        if (task.getException() != null) {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            if ("ERROR_INVALID_CREDENTIAL".equals(errorCode)) {
                                Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Re-authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}