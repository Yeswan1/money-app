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

public class EmployeeSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_setup);

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

        // Salary Credit Date Picker
        View salaryDateContainer = findViewById(R.id.date_salary_container);
        TextView tvSalaryDate = findViewById(R.id.tv_salary_date);
        if (salaryDateContainer != null && tvSalaryDate != null) {
            salaryDateContainer.setOnClickListener(v -> showDatePicker(tvSalaryDate, "Select Salary Credit Date"));
        }

        // Next button functionality - Proceed to Notification Permission
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeSetupActivity.this, NotificationPermissionActivity.class);
            intent.putExtra("target_dashboard", "employee");
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