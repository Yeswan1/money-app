package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.Manifest;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;

public class NotificationPermissionActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                proceedToNextStep();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification_permission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton btnAllow = findViewById(R.id.btn_allow);
        TextView btnNotNow = findViewById(R.id.btn_not_now);

        btnAllow.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                proceedToNextStep();
            }
        });
        btnNotNow.setOnClickListener(v -> proceedToNextStep());
    }

    private void proceedToNextStep() {
        String target = getIntent().getStringExtra("target_dashboard");
        Intent intent = new Intent(NotificationPermissionActivity.this, BudgetSetupActivity.class);
        intent.putExtra("target_dashboard", target);
        startActivity(intent);
        finish();
    }
}
