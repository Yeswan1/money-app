package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StudentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupBottomNavigation();
        setupCategoryBudgets();
        setupRecentTransactions();
        
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(this, AddTransactionActivity.class));
            });
        }

        FloatingActionButton fabChatbot = findViewById(R.id.fab_chatbot);
        if (fabChatbot != null) {
            fabChatbot.setOnClickListener(v -> {
                Toast.makeText(this, "AI Chatbot coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportsActivity.class));
                return true;
            }
            return id == R.id.nav_home;
        });
    }

    private void setupCategoryBudgets() {
        updateCategoryItem(findViewById(R.id.item_food), "🍔", "Food", "₹800/₹1000", "80%", 80, R.drawable.budget_progress_drawable_yellow, R.color.warning_yellow);
        updateCategoryItem(findViewById(R.id.item_transport), "🚌", "Transport", "₹200/₹500", "40%", 40, R.drawable.budget_progress_drawable_teal, R.color.info_teal);
        updateCategoryItem(findViewById(R.id.item_books), "📚", "Books", "₹100/₹400", "25%", 25, R.drawable.budget_progress_drawable_green, R.color.income_green);
        updateCategoryItem(findViewById(R.id.item_entertainment), "🎮", "Entertainment", "₹300/₹300", "100%", 100, R.drawable.budget_progress_drawable_pink, R.color.expense_red);
    }

    private void updateCategoryItem(View view, String icon, String name, String amount, String percent, int progress, int progressDrawable, int percentColor) {
        ((TextView) view.findViewById(R.id.category_icon_text)).setText(icon);
        ((TextView) view.findViewById(R.id.category_name)).setText(name);
        ((TextView) view.findViewById(R.id.category_amount)).setText(amount);
        TextView tvPercent = view.findViewById(R.id.category_percentage);
        tvPercent.setText(percent);
        tvPercent.setTextColor(getResources().getColor(percentColor));
        ProgressBar pb = view.findViewById(R.id.progressBar);
        pb.setProgress(progress);
        pb.setProgressDrawable(getResources().getDrawable(progressDrawable));
    }

    private void setupRecentTransactions() {
        updateTransactionItem(findViewById(R.id.trans_1), "🍔", "Canteen lunch", "Food • Today", "-₹120", R.color.expense_red);
        updateTransactionItem(findViewById(R.id.trans_2), "🚌", "Bus pass", "Transport • Yesterday", "-₹200", R.color.expense_red);
        updateTransactionItem(findViewById(R.id.trans_3), "💰", "Pocket money", "Income • Mar 1", "+₹5000", R.color.income_green);
        updateTransactionItem(findViewById(R.id.trans_4), "📚", "Notes printing", "Books • Mar 15", "-₹80", R.color.expense_red);
    }

    private void updateTransactionItem(View view, String icon, String title, String subtitle, String amount, int amountColor) {
        ((TextView) view.findViewById(R.id.transaction_icon)).setText(icon);
        ((TextView) view.findViewById(R.id.transaction_title)).setText(title);
        ((TextView) view.findViewById(R.id.transaction_subtitle)).setText(subtitle);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);
        tvAmount.setText(amount);
        tvAmount.setTextColor(getResources().getColor(amountColor));
    }
}
