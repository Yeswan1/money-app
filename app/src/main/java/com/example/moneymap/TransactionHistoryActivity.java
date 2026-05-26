package com.example.moneymap;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TransactionHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_history);

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

        setupHistoryItems();
    }

    private void setupHistoryItems() {
        // Today
        updateTransactionItem(findViewById(R.id.item_today_1), "🍔", "Canteen Lunch", "Food • 12:30 PM", "₹120", R.color.expense_red);

        // Yesterday
        updateTransactionItem(findViewById(R.id.item_yesterday_1), "🚌", "Bus Pass", "Transport • 8:00 AM", "₹200", R.color.expense_red);
        updateTransactionItem(findViewById(R.id.item_yesterday_2), "🎬", "Movie Ticket", "Entertainment • 7:00 PM", "₹250", R.color.expense_red);

        // March 15
        updateTransactionItem(findViewById(R.id.item_mar15_1), "📚", "Notes Printing", "Books • 2:00 PM", "₹80", R.color.expense_red);
        updateTransactionItem(findViewById(R.id.item_mar15_2), "☕", "Coffee", "Food • 4:30 PM", "₹50", R.color.expense_red);

        // March 1
        updateTransactionItem(findViewById(R.id.item_mar1_1), "💰", "Pocket Money", "Income • 9:00 AM", "+₹5000", R.color.income_green);
    }

    private void updateTransactionItem(View view, String icon, String title, String subtitle, String amount, int colorRes) {
        if (view == null) return;
        TextView tvIcon = view.findViewById(R.id.transaction_icon);
        TextView tvTitle = view.findViewById(R.id.transaction_title);
        TextView tvSubtitle = view.findViewById(R.id.transaction_subtitle);
        TextView tvAmount = view.findViewById(R.id.transaction_amount);

        if (tvIcon != null) tvIcon.setText(icon);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) tvSubtitle.setText(subtitle);
        if (tvAmount != null) {
            tvAmount.setText(amount);
            tvAmount.setTextColor(ContextCompat.getColor(this, colorRes));
        }
    }
}