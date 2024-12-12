package com.example.text.MainActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.text.HomeActivity.Home;
import com.example.text.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    TextView forgotPassword;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;
    RelativeLayout google_login_button;
    ProgressDialog progressDialog;
    LinearProgressIndicator lpb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lpb = findViewById(R.id.lpb);
        mAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.login_email);
        forgotPassword = findViewById(R.id.forgotPassword);
        editTextPassword = findViewById(R.id.login_password);
        buttonLogin = findViewById(R.id.google_sign_in_button);
        TextView log = findViewById(R.id.log);
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
        TextView signup = findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
        TextView signUpRedirectText = findViewById(R.id.signUpRedirectText);
        signUpRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve email and password from EditText fields
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check if email is empty
                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Email is required");
                    editTextEmail.requestFocus();
                    return;
                }

                // Check if password is empty
                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("Password is required");
                    editTextPassword.requestFocus();
                    return;
                }

                // Proceed with user login
                lpb.setVisibility(View.VISIBLE);
                loginUser(email, password);
            }
        });

        TextView textView = findViewById(R.id.signUpRedirectText);
        String fullText = "Not yet registered? Sign Up";
        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf("Sign Up"); // makuha ang index kung saan nagsisimula ang "Sign Up"
        int end = start + "Sign Up".length(); // makuha ang index ng dulo ng "Sign Up"

        // Set ng ibang kulay para sa "Sign Up" na bahagi
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        // Call the method to check user login status
        checkUserLoginStatus();

        google_login_button = findViewById(R.id.google_login);

        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Creating account");
        progressDialog.setMessage("we are creating your account");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        google_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();
            }
        });
    }
    int RC_SIGN_IN = 40;

    private void signIn() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuth(account.getIdToken());

            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        lpb.setVisibility(View.VISIBLE); // Show loading indicator

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            // Check if the user already exists in the database
                            DatabaseReference userRef = database.getReference().child("users").child(user.getUid());
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // If the user does not exist, add them to the database
                                    if (!dataSnapshot.exists()) {
                                        UserModel users = new UserModel();
                                        users.setUid(user.getUid());
                                        users.setUsername(user.getDisplayName());
                                        users.setEmail(user.getEmail());

                                        // Save user object to the database
                                        userRef.setValue(users);
                                    }

                                    // Redirect to the main activity after either login or saving the user
                                    Intent intent = new Intent(MainActivity.this, Home.class);
                                    startActivity(intent);
                                    finish(); // Optionally finish the current activity
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    lpb.setVisibility(View.GONE); // Hide loading indicator
                                    Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            lpb.setVisibility(View.GONE); // Hide loading indicator
                            Toast.makeText(MainActivity.this, "Error signing in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkUserLoginStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                // User is logged in and email is verified
                startActivity(new Intent(MainActivity.this, Home.class));
                finish(); // Prevent returning to login activity
            } else {
                // User is logged in but email is not verified
                Toast.makeText(this, "Please verify your email before accessing the app.", Toast.LENGTH_LONG).show();

                // Optional: Resend verification email
                currentUser.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email resent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to resend verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Optionally log out the user
                FirebaseAuth.getInstance().signOut();
            }
        }
    }



    private void loginUser(String email, String password) {
    lpb.setVisibility(View.VISIBLE);
    mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    lpb.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            if (user.isEmailVerified()) {
                                // Email is verified
                                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, Home.class));
                                finish();
                            } else {
                                // Email is not verified
                                Toast.makeText(MainActivity.this, "Please verify your email to login.", Toast.LENGTH_LONG).show();

                                // Optional: Resend email verification
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> resendTask) {
                                        if (resendTask.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Verification email resent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Failed to resend verification email: " + resendTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        // Login failed
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}