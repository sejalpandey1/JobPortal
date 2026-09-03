package com.jobportal.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.jobportal.app.R;
import com.jobportal.app.adapters.OnboardingAdapter;

public class OnboardingActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    Button btnNext;
    TextView btnSkip, btnGetStarted;
    LinearLayout dotsLayout;
    OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        dotsLayout = findViewById(R.id.dotsLayout);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        setupDots(0);

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        setupDots(position);
                        if (position == 2) {
                            btnNext.setVisibility(View.GONE);
                            btnSkip.setVisibility(View.GONE);
                            btnGetStarted.setVisibility(View.VISIBLE);
                        } else {
                            btnNext.setVisibility(View.VISIBLE);
                            btnSkip.setVisibility(View.VISIBLE);
                            btnGetStarted.setVisibility(View.GONE);
                        }
                    }
                });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < 2) viewPager.setCurrentItem(current + 1);
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnGetStarted.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDots(int position) {
        dotsLayout.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == position
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(20, 20);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dotsLayout.addView(dot);
        }
    }

    private void finishOnboarding() {
        getSharedPreferences("onboarding", MODE_PRIVATE)
                .edit().putBoolean("completed", true).apply();
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}