package com.example.mapbox;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GetArray extends AppCompatActivity {

    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_array);

        String[] array = getIntent().getStringArrayExtra("PASS_DATA");

        text = findViewById(R.id.passed);
        text.setText("Passed data: " + array[1] +", " +  array[2]);

    }
}