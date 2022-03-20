package com.ubcohci.fingerdetection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.ubcohci.fingerdetection.databinding.ActivityLaunchBinding;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        ActivityLaunchBinding launchBinding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(launchBinding.getRoot());

        // Set listener for the start button
        launchBinding.startButton.setOnClickListener(this);

        FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
