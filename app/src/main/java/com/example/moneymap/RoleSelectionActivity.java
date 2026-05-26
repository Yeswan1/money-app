package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class RoleSelectionActivity extends AppCompatActivity {

    private String selectedRole = ""; // Track role as string for better reliability
    private View selectedView = null;
    private MaterialButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        View mainView = findViewById(android.R.id.content);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        continueButton = findViewById(R.id.continue_button);
        continueButton.setEnabled(false); // Disable until selection is made
        
        continueButton.setOnClickListener(v -> {
            if (!selectedRole.isEmpty()) {
                Intent intent;
                
                switch (selectedRole) {
                    case "student":
                        intent = new Intent(RoleSelectionActivity.this, StudentSetupActivity.class);
                        break;
                    case "employee":
                        intent = new Intent(RoleSelectionActivity.this, EmployeeSetupActivity.class);
                        break;
                    case "homemaker":
                        intent = new Intent(RoleSelectionActivity.this, GeneralSetupActivity.class);
                        break;
                    case "freelancer":
                        intent = new Intent(RoleSelectionActivity.this, GeneralSetupActivity.class);
                        break;
                    default:
                        intent = new Intent(RoleSelectionActivity.this, GeneralSetupActivity.class);
                        break;
                }
                
                intent.putExtra("target_dashboard", selectedRole);
                startActivity(intent);
                // We keep finish() commented out for debugging. 
                // Once it works, you can uncomment it.
                // finish(); 
            }
        });
        
        setupOption(findViewById(R.id.btn_student), "student");
        setupOption(findViewById(R.id.btn_employee), "employee");
        setupOption(findViewById(R.id.btn_homemaker), "homemaker");
        setupOption(findViewById(R.id.btn_freelancer), "freelancer");
        setupOption(findViewById(R.id.btn_general), "general");
    }

    private void setupOption(View view, final String role) {
        if (view == null) return;
        
        view.setOnClickListener(v -> {
            if (selectedView != null) {
                selectedView.setSelected(false);
            }
            v.setSelected(true);
            selectedView = v;
            selectedRole = role;
            
            // Enable and style the continue button
            continueButton.setEnabled(true);
            continueButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.accent_lime));
            continueButton.setTextColor(ContextCompat.getColor(this, R.color.bg_dark));
        });
    }
}
