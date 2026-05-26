package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private MaterialButton nextButton;
    private TextView skipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nextButton = findViewById(R.id.continue_button);
        skipButton = findViewById(R.id.btn_skip);

        nextButton.setOnClickListener(v -> {
            // Navigate to Onboarding 2
            Intent intent = new Intent(OnboardingActivity.this, Onboarding2Activity.class);
            startActivity(intent);
            finish();
        });

        skipButton.setOnClickListener(v -> {
            // Skipping intro goes straight to Role Selection
            Intent intent = new Intent(OnboardingActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }
}