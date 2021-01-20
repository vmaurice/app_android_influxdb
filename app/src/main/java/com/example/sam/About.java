package com.example.sam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.Theme_Sam_Dark);
        else
            setTheme(R.style.Theme_Sam);

        setTitle("About");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}