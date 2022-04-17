package com.ubcohci.fingerdetection;

import android.os.Bundle;
import android.util.Log;

import com.ubcohci.fingerdetection.application.BaseActivity;
import com.ubcohci.fingerdetection.camera.MultiCameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityMainv2Binding;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivityV2 extends BaseActivity {
    // Log tag
    private static final String TAG = "MainActivityV2";

    public ActivityMainv2Binding viewBinding;

    public MultiCameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get view binding
        viewBinding = ActivityMainv2Binding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.graphicOverlayV2;

        // Set camera
        this.singleCameraSource = null;
        this.cameraSource = new MultiCameraSource(TAG, this, this, this);

        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        Log.d(MainActivityV2.TAG, "Do something...");
    }
}
