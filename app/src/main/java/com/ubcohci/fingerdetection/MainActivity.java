package com.ubcohci.fingerdetection;

import android.os.Bundle;
import android.util.Log;

import com.ubcohci.fingerdetection.application.BaseActivity;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {
    // Tags and request codes
    private static final String TAG = "MainActivity";

    // View related objects
    public ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Get the graphic overlay
        graphicOverlay = viewBinding.graphicOverlay;

        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        Log.d(TAG, "Do something...");
    }
}