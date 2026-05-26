package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
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

        findViewById(R.id.google_button).setOnClickListener(v -> {
            Toast.makeText(this, "Google Sign In coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.facebook_button).setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Sign In coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.forgot_password_text).setOnClickListener(v -> {
            Toast.makeText(this, "Reset password link sent", Toast.LENGTH_SHORT).show();
        });
    }
}