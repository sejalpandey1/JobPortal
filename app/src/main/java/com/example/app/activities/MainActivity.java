package com.jobportal.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jobportal.app.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyWindowInsets(findViewById(android.R.id.content));
    }
}