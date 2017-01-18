package com.markfeldman.sunshine.Activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.markfeldman.sunshine.R;

public class MySettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
