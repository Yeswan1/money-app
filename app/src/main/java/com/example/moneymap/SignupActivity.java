package com.example.moneymap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView termsText = findViewById(R.id.terms_text);
        setupTermsAndConditions(termsText);

        findViewById(R.id.sign_in_text).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.signup_button).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, OnboardingActivity.class));
        });
    }

    private void setupTermsAndConditions(TextView textView) {
        String part1 = getString(R.string.terms_part1);
        String part2 = getString(R.string.terms_part2);
        String part3 = getString(R.string.terms_part3);
        String part4 = getString(R.string.terms_part4);

        SpannableString spannableString = new SpannableString(part1 + part2 + part3 + part4);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle Terms & Conditions click
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.accent_green));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Handle Privacy Policy click
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.accent_green));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        int startPart2 = part1.length();
        int endPart2 = startPart2 + part2.length();
        int startPart4 = endPart2 + part3.length();
        int endPart4 = startPart4 + part4.length();

        spannableString.setSpan(termsSpan, startPart2, endPart2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacySpan, startPart4, endPart4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
    }
}