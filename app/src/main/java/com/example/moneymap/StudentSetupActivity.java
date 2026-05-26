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

public class StudentSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_setup);


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

        // Pocket Money Date Picker
        View pocketMoneyContainer = findViewById(R.id.date_pocket_money_container);
        TextView tvPocketMoneyDate = findViewById(R.id.tv_pocket_money_date);
        if (pocketMoneyContainer != null && tvPocketMoneyDate != null) {
            pocketMoneyContainer.setOnClickListener(v -> showDatePicker(tvPocketMoneyDate, "Select Pocket Money Date"));
        }

        // Next Semester Due Date Picker
        View dueDateContainer = findViewById(R.id.date_due_date_container);
        TextView tvDueDate = findViewById(R.id.tv_due_date);
        if (dueDateContainer != null && tvDueDate != null) {
            dueDateContainer.setOnClickListener(v -> showDatePicker(tvDueDate, "Select Semester Due Date"));
        }

        // Next button functionality - Proceed to Notification Permission
        findViewById(R.id.btn_next).setOnClickListener(v -> {
            Intent intent = new Intent(StudentSetupActivity.this, NotificationPermissionActivity.class);
            intent.putExtra("target_dashboard", "student");
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
