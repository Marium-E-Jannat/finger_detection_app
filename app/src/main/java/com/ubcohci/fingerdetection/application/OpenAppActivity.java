package com.ubcohci.fingerdetection.application;

import android.os.Bundle;
import android.util.Log;

import com.ubcohci.fingerdetection.PostureSequenceDetector;
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
    private PostureSequenceDetector postureSequenceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view
        viewBinding = ActivityOpenAppBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.webviewOverlay;

        // Init a gesture detector
        postureSequenceDetector = new PostureSequenceDetector();

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

        PostureSequenceDetector.MotionTask motionTask = postureSequenceDetector.getMotionTask(className, coordinates);
        Log.d(TAG, motionTask.toString());

        if (motionTask == PostureSequenceDetector.MotionTask.SWITCH_VOLUME) {
            // Get which volume mapping (10->facebook, 20->youtube)
            int volumeLevel = postureSequenceDetector.findVolumeLevel(postureSequenceDetector.getCurrentGestureInString());
            String url = urls[volumeLevel == 10? 0:1];
            viewBinding.webView.loadUrl(url);
        }
    }
}
