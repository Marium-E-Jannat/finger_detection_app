package com.ubcohci.fingerdetection.application;

import android.os.Bundle;
import android.util.Log;

import com.ubcohci.fingerdetection.GestureDetector;
import com.ubcohci.fingerdetection.databinding.ActivityOpenAppBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OpenAppActivity extends BaseActivity {

    // URLS to switch web views
    private static final String[] urls = {
            "https://www.youtube.com/",
            "https://www.facebook.com/"
    };

    // View binding
    private ActivityOpenAppBinding viewBinding;

    // Gesture detector
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view
        viewBinding = ActivityOpenAppBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.webviewOverlay;

        // Get web view
        viewBinding.webView.loadUrl(urls[0]);

        // Init a gesture detector
        gestureDetector = new GestureDetector();

        // Start camera
        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        super.handleAppTask(data);

        // Get class name
        String className = data.getString("class_name");
        // Extract bounding box of the posture to detect gesture
        Map<String, Integer> coordinates = new HashMap<>();
        coordinates.put("top", data.getInt("y_min"));
        coordinates.put("bottom", data.getInt("y_max"));
        coordinates.put("left", data.getInt("x_min"));
        coordinates.put("right", data.getInt("x_max"));

        GestureDetector.MotionTask motionTask = gestureDetector.getMotionTask(className, coordinates);
        Log.d(TAG, motionTask.toString());

        if (motionTask == GestureDetector.MotionTask.SWITCH_VOLUME) {
            // Get which volume mapping (10->facebook, 20->youtube)
            int volumeLevel = gestureDetector.findVolumeLevel(gestureDetector.getCurrentGestureInString());
            String url = urls[volumeLevel == 10? 0:1];
            viewBinding.webView.loadUrl(url);
        }
    }
}
