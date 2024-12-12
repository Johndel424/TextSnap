package com.example.text.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.text.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText editTextUsername, editTextEmail, editTextPassword,editTextConfirmPassword;
    private Button buttonRegister;
    LinearProgressIndicator lpb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        lpb = findViewById(R.id.lpb);
        editTextUsername = findViewById(R.id.login_username);
        editTextEmail = findViewById(R.id.login_email);
        editTextPassword = findViewById(R.id.login_password);
        buttonRegister = findViewById(R.id.google_sign_in_button);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
//        buttonRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Retrieve username, email, and password from EditText fields
//                final String username = editTextUsername.getText().toString().trim();
//                String email = editTextEmail.getText().toString().trim();
//                String password = editTextPassword.getText().toString().trim();
//
//                // Check if username is empty
//                if (TextUtils.isEmpty(username)) {
//                    editTextUsername.setError("Username is required");
//                    editTextUsername.requestFocus();
//                    return;
//                }
//
//                // Check if email is empty
//                if (TextUtils.isEmpty(email)) {
//                    editTextEmail.setError("Email is required");
//                    editTextEmail.requestFocus();
//                    return;
//                }
//
//                // Check if password is empty
//                if (TextUtils.isEmpty(password)) {
//                    editTextPassword.setError("Password is required");
//                    editTextPassword.requestFocus();
//                    return;
//                }
//
//                // Proceed with user registration
//                lpb.setVisibility(View.VISIBLE);
//                registerUser(username, email, password);
//            }
//        });
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve input values
                final String username = editTextUsername.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();

                // Validate inputs
                if (TextUtils.isEmpty(username)) {
                    editTextUsername.setError("Username is required");
                    editTextUsername.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    editTextConfirmPassword.setError("Confirm Password is required");
                    editTextConfirmPassword.requestFocus();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    editTextConfirmPassword.setError("Passwords do not match");
                    editTextConfirmPassword.requestFocus();
                    return;
                }

                // Check password strength
                if (!isPasswordStrong(password)) {
                    editTextPassword.setError("Password must be at least 8 characters long, include uppercase, lowercase, numbers, and special characters.");
                    editTextPassword.requestFocus();
                    return;
                }

                // Proceed with registration
                lpb.setVisibility(View.VISIBLE);
                registerUser(username, email, password);
            }
        });



        TextView signUpRedirectText = findViewById(R.id.signUpRedirectText);
        signUpRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });

        TextView textView = findViewById(R.id.signUpRedirectText);
        String fullText = "Already have a account? Login";
        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf("Login"); // makuha ang index kung saan nagsisimula ang "Sign Up"
        int end = start + "Login".length(); // makuha ang index ng dulo ng "Sign Up"

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
        TextView log = findViewById(R.id.log);
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        TextView signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        });

    }
    // Method to check password strength
    private boolean isPasswordStrong(String password) {
        // Regex for strong password: minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 digit, 1 special character
        String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return password.matches(passwordPattern);
    }
private void registerUser(final String username, String email, String password) {
    lpb.setVisibility(View.VISIBLE);
    mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        final String userId = mAuth.getCurrentUser().getUid(); // Get UID
                        final FirebaseUser user = mAuth.getCurrentUser(); // Get Firebase user object

                        if (user != null) {
                            // Send email verification
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> emailTask) {
                                    if (emailTask.isSuccessful()) {
                                        // Save user data in database
                                        UserModel userModel = new UserModel(userId, username, email);
                                        saveUserData(userId, userModel);

                                        Toast.makeText(SignUp.this, "Verification email sent! Please verify to complete registration.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignUp.this, "Failed to send verification email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    lpb.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        lpb.setVisibility(View.GONE);
                        Toast.makeText(SignUp.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
}
    private void saveUserData(String userId, UserModel user) {
        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> databaseTask) {
                        if (databaseTask.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Registration successful! Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(SignUp.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUp.this, "Failed to register user data", Toast.LENGTH_SHORT).show();
                        }
                        lpb.setVisibility(View.GONE);
                    }
                });
    }


}