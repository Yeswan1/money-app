package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GeneralSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_general_setup);

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

        // Income Date Picker
        View incomeDateContainer = findViewById(R.id.date_income_container);
        TextView tvIncomeDate = findViewById(R.id.tv_income_date);
        if (incomeDateContainer != null && tvIncomeDate != null) {
            incomeDateContainer.setOnClickListener(v -> showDatePicker(tvIncomeDate, "Select Income Credit Date"));
        }

        // Proceed to Notification Permission (Step 2 of Setup)
        findViewById(R.id.btn_complete).setOnClickListener(v -> {
            Intent intent = new Intent(GeneralSetupActivity.this, NotificationPermissionActivity.class);
            // Pass along the target dashboard so the Budget screen knows where to go next
            intent.putExtra("target_dashboard", getIntent().getStringExtra("target_dashboard"));
            startActivity(intent);
        });
    }

    private void showDatePicker(TextView textView, String title) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            textView.setText(sdf.format(new Date(selection)));
            textView.setTextColor(getResources().getColor(R.color.white));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }
}
