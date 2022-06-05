package com.ubcohci.fingerdetection;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ubcohci.fingerdetection.application.ImageBrowsingActivity;
import com.ubcohci.fingerdetection.application.MockMapScrollingActivity;
import com.ubcohci.fingerdetection.application.MockMapZoomActivity;
import com.ubcohci.fingerdetection.application.OpenAppActivity;
import com.ubcohci.fingerdetection.application.VideoControlActivity;
import com.ubcohci.fingerdetection.application.VideoControlActivityV2;
import com.ubcohci.fingerdetection.databinding.ActivityLaunchBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchActivity extends AppCompatActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

    private static final Map<String, Class<? extends AppCompatActivity>> appNameToClass = new HashMap<>();
    private static final String TAG = "LaunchActivity";

    static {
        appNameToClass.put("Video Control", VideoControlActivity.class);
        appNameToClass.put("Open App", OpenAppActivity.class);
        appNameToClass.put("Image Browsing", ImageBrowsingActivity.class);
        appNameToClass.put("Video Control V2", VideoControlActivityV2.class);
        appNameToClass.put("Detection Preview", MainActivity.class);
        appNameToClass.put("Detection Preview V2", MainActivityV2.class);
        appNameToClass.put("Map Scrolling", MockMapScrollingActivity.class);
        appNameToClass.put("Map Zooming", MockMapZoomActivity.class);
    }

    private String selectedAppName;
    private boolean showDebug;

    private ActivityLaunchBinding launchBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        launchBinding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(launchBinding.getRoot());

        // Set listener for the start button
        launchBinding.startButton.setOnClickListener(this);

        // Set checkBox
        showDebug = launchBinding.debugCheckbox.isChecked();
        launchBinding.debugCheckbox.setOnCheckedChangeListener(this);

        // Set spinner data and listener
        initAppSelections();
    }

    public void initAppSelections() {
        launchBinding.appSelectionSpinner.setOnItemSelectedListener(this);
        // Create a list of app name
        List<String> appNames = new ArrayList<>(appNameToClass.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, appNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        launchBinding.appSelectionSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Starting " + selectedAppName + " with showDebug: " + showDebug);
        Intent intent = new Intent(this, appNameToClass.get(selectedAppName));
        intent.putExtra("showDebug", showDebug);
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        showDebug = isChecked;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        if (parent.getId() == R.id.app_selection_spinner) {
           selectedAppName =  (String) parent.getItemAtPosition(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
    }
}