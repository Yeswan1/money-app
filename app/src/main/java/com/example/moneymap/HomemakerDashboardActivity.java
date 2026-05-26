package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomemakerDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homemaker_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupBottomNavigation();
        setupChatbot();
        setupAddExpense();
        setupRecentTransactions();
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

    private void setupChatbot() {
        FloatingActionButton fabChatbot = findViewById(R.id.fab_chatbot);
        if (fabChatbot != null) {
            fabChatbot.setOnClickListener(v -> {
                Toast.makeText(this, "AI Home Assistant coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupAddExpense() {
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(this, AddTransactionActivity.class));
            });
        }
    }

    private void setupRecentTransactions() {
        android.widget.LinearLayout container = findViewById(R.id.transaction_list);
        if (container != null) {
            updateTransactionItem(container.getChildAt(0), "🥦", "Weekly Groceries", "Food • Today", "-₹2,450", R.color.expense_red);
            updateTransactionItem(container.getChildAt(1), "⚡", "Electricity Bill", "Utilities • Yesterday", "-₹1,200", R.color.expense_red);
            updateTransactionItem(container.getChildAt(2), "🥛", "Daily Milk", "Food • Yesterday", "-₹65", R.color.expense_red);
        }
    }

    private void updateTransactionItem(View view, String icon, String title, String subtitle, String amount, int amountColor) {
        if (view == null) return;
        ((TextView) view.findViewById(R.id.transaction_icon)).setText(icon);
        ((TextView) view.findViewById(R.id.transaction_title)).setText(title);
        ((TextView) view.findViewById(R.id.transaction_subtitle)).setText(subtitle);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);
        tvAmount.setText(amount);
        tvAmount.setTextColor(getResources().getColor(amountColor));
    }
}