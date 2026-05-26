package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView amountText;
    private TextView decimalText;
    private TextView btnExpense, btnIncome;
    private String currentAmount = "0";
    private boolean isExpense = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_transaction);

        View contentFrame = findViewById(android.R.id.content);
        if (contentFrame != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentFrame, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        amountText = findViewById(R.id.amount_text);
        decimalText = findViewById(R.id.decimal_text);
        btnExpense = findViewById(R.id.btn_expense);
        btnIncome = findViewById(R.id.btn_income);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        setupToggle();
        setupKeypad();
        setupCategoryGrid();

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionSuccessActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupToggle() {
        btnExpense.setOnClickListener(v -> {
            isExpense = true;
            btnExpense.setBackgroundResource(R.drawable.button_bg_primary);
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.bg_dark));
            btnIncome.setBackground(null);
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        });

        btnIncome.setOnClickListener(v -> {
            isExpense = false;
            btnIncome.setBackgroundResource(R.drawable.button_bg_primary);
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.bg_dark));
            btnExpense.setBackground(null);
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
        });
    }

    private void setupKeypad() {
        View.OnClickListener listener = v -> {
            String val = ((TextView) v).getText().toString();
            if (val.equals(".")) {
                // Ignore dot for now or implement decimal logic if needed
                return;
            }
            if (currentAmount.equals("0")) {
                currentAmount = val;
            } else if (currentAmount.length() < 9) {
                currentAmount += val;
            }
            amountText.setText(currentAmount);
        };

        GridLayout grid = findViewById(R.id.keypad);
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            if (child instanceof TextView && !((TextView) child).getText().toString().isEmpty()) {
                child.setOnClickListener(listener);
            }
        }

        findViewById(R.id.btn_backspace).setOnClickListener(v -> {
            if (currentAmount.length() > 1) {
                currentAmount = currentAmount.substring(0, currentAmount.length() - 1);
            } else {
                currentAmount = "0";
            }
            amountText.setText(currentAmount);
        });
    }

    private void setupCategoryGrid() {
        GridLayout grid = findViewById(R.id.category_grid);
        grid.removeAllViews();

        List<CategoryItem> categories = new ArrayList<>();
        categories.add(new CategoryItem("Rent/EMI", "🏠"));
        categories.add(new CategoryItem("Food", "🍱"));
        categories.add(new CategoryItem("Transport", "🚗"));
        categories.add(new CategoryItem("Entertainment", "🎬"));
        categories.add(new CategoryItem("Health", "💊"));
        categories.add(new CategoryItem("Bills", "📱"));
        categories.add(new CategoryItem("Shopping", "👕"));
        categories.add(new CategoryItem("Investment", "📈"));
        categories.add(new CategoryItem("Gifts", "🎁"));
        categories.add(new CategoryItem("Travel", "✈️"));
        categories.add(new CategoryItem("Education", "🎓"));
        categories.add(new CategoryItem("Other", "📦"));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CategoryItem item : categories) {
            View view = inflater.inflate(R.layout.item_category_grid, grid, false);
            TextView icon = view.findViewById(R.id.category_icon);
            TextView name = view.findViewById(R.id.category_name);
            icon.setText(item.icon);
            name.setText(item.name);
            
            view.setOnClickListener(v -> {
                // Reset all other items background if needed
                for(int i=0; i<grid.getChildCount(); i++) {
                    grid.getChildAt(i).setBackgroundResource(R.drawable.button_bg_secondary);
                }
                view.setBackgroundResource(R.drawable.period_selected_bg);
            });
            
            grid.addView(view);
        }
    }

    private static class CategoryItem {
        String name;
        String icon;

        CategoryItem(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }
}
