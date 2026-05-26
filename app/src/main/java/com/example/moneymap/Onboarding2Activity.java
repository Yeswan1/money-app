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

public class Onboarding2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton nextButton = findViewById(R.id.continue_button);
        TextView skipButton = findViewById(R.id.btn_skip);

        nextButton.setOnClickListener(v -> {
            // Navigate to Role Selection (or Onboarding 3 if you add it later)
            Intent intent = new Intent(Onboarding2Activity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });

        skipButton.setOnClickListener(v -> {
            // Skipping intro goes straight to Role Selection
            Intent intent = new Intent(Onboarding2Activity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
        });
    }
}