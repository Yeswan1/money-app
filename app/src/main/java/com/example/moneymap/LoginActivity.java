package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import com.example.moneymap.data.api.MoneyMapApiClient;
import com.example.moneymap.data.model.GoogleSignInRequest;
import com.example.moneymap.data.session.AuthSession;

import java.util.concurrent.Executors;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Allow screenshots on this screen
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);

        credentialManager = CredentialManager.create(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.sign_up_text).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            // Simplified login logic
            startActivity(new Intent(LoginActivity.this, OnboardingActivity.class));
            finish();
        });

        // Password visibility toggle
        android.widget.EditText passwordInput = findViewById(R.id.password_input);
        android.widget.ImageView passwordToggle = findViewById(R.id.password_toggle);
        passwordToggle.setOnClickListener(v -> {
            if (passwordInput.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye_off);
                passwordToggle.setContentDescription("Hide password");
            } else {
                // Hide password
                passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_eye);
                passwordToggle.setContentDescription("Show password");
            }
            // Keep cursor at end
            passwordInput.setSelection(passwordInput.getText().length());
        });

        findViewById(R.id.google_button).setOnClickListener(v -> {
            signInWithGoogle();
        });

        findViewById(R.id.facebook_button).setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Sign In coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.forgot_password_text).setOnClickListener(v -> {
            Toast.makeText(this, "Reset password link sent", Toast.LENGTH_SHORT).show();
        });
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null, // CancellationSignal
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignInResult(result);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Google Sign-In failed", e);
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this,
                                    "Google Sign-In cancelled or failed. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void handleGoogleSignInResult(GetCredentialResponse response) {
        Credential credential = response.getCredential();

        if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            GoogleIdTokenCredential googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.getData());
            String idToken = googleCredential.getIdToken();

            // Send the ID token to the backend on a background thread
            sendIdTokenToBackend(idToken);
        } else {
            runOnUiThread(() ->
                    Toast.makeText(this, "Unexpected credential type", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void sendIdTokenToBackend(String idToken) {
        // Use the GoogleSignInHelper (Kotlin) to call the suspend function
        GoogleSignInHelper.INSTANCE.signIn(this, idToken, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Signed in with Google!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, RoleSelectionActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Sign-in failed: " + message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}