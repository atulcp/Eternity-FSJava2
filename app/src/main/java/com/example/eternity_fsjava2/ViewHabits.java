package com.example.eternity_fsjava2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ViewHabits extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_habits);
    }

    public void goToSetHabits(View view) {
        startActivity(new Intent(getApplicationContext(), SetAndTrackHabits.class));
    }
}
