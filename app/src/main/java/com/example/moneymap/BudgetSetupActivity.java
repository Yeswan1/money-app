package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BudgetSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back button functionality
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Finish button functionality
        findViewById(R.id.btn_finish).setOnClickListener(v -> {
            String target = getIntent().getStringExtra("target_dashboard");
            Intent intent;

            if ("student".equals(target)) {
                intent = new Intent(BudgetSetupActivity.this, StudentDashboardActivity.class);
            } else if ("employee".equals(target)) {
                intent = new Intent(BudgetSetupActivity.this, EmployeeDashboardActivity.class);
            } else if ("homemaker".equals(target)) {
                intent = new Intent(BudgetSetupActivity.this, HomemakerDashboardActivity.class);
            } else {
                // Default to general dashboard
                intent = new Intent(BudgetSetupActivity.this, DashboardActivity.class);
            }

            startActivity(intent);
            finishAffinity(); // Clear stack and go to the dashboard
        });
    }
}
