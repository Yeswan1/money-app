package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports);

        // Using android.R.id.content for the inset listener is more robust
        View contentFrame = findViewById(android.R.id.content);
        if (contentFrame != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentFrame, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        setupBottomNavigation();
        setupCategoryReports();
        setupFab();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    finish();
                    return true;
                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(this, TransactionHistoryActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_reports) {
                    return true;
                }
                return false;
            });
            // Select reports item
            bottomNav.setSelectedItemId(R.id.nav_reports);
        }
    }

    private void setupFab() {
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(this, AddTransactionActivity.class));
            });
        }
    }

    private void setupCategoryReports() {
        View food = findViewById(R.id.report_food);
        if (food != null) updateCategoryItem(food, "🍔", "Food", "₹1,200", "45%", 45, R.drawable.budget_progress_drawable_yellow, R.color.warning_yellow);
        
        View transport = findViewById(R.id.report_transport);
        if (transport != null) updateCategoryItem(transport, "🚌", "Transport", "₹450", "15%", 15, R.drawable.budget_progress_drawable_teal, R.color.info_teal);
        
        View entertainment = findViewById(R.id.report_entertainment);
        if (entertainment != null) updateCategoryItem(entertainment, "🎮", "Entertainment", "₹850", "30%", 30, R.drawable.budget_progress_drawable_pink, R.color.expense_red);
    }

    private void updateCategoryItem(View view, String icon, String name, String amount, String percent, int progress, int progressDrawable, int percentColor) {
        if (view == null) return;

        TextView tvIcon = view.findViewById(R.id.category_icon_text);
        if (tvIcon != null) tvIcon.setText(icon);
        
        TextView tvName = view.findViewById(R.id.category_name);
        if (tvName != null) tvName.setText(name);
        
        TextView tvAmount = view.findViewById(R.id.category_amount);
        if (tvAmount != null) tvAmount.setText(amount);
        
        TextView tvPercent = view.findViewById(R.id.category_percentage);
        if (tvPercent != null) {
            tvPercent.setText(percent);
            tvPercent.setTextColor(ContextCompat.getColor(this, percentColor));
        }
        
        ProgressBar pb = view.findViewById(R.id.progressBar);
        if (pb != null) {
            pb.setProgress(progress);
            pb.setProgressDrawable(ContextCompat.getDrawable(this, progressDrawable));
        }
    }
}
