package com.ubcohci.fingerdetection.application;

import android.os.Bundle;

import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;
import com.ubcohci.fingerdetection.databinding.ActivityOpenAppBinding;
import com.ubcohci.fingerdetection.tasks.OpenAppTaskManager;
import com.ubcohci.fingerdetection.tasks.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenAppActivity extends BaseActivity {

    // View binding
    private ActivityOpenAppBinding viewBinding;

    // Gesture detector
    private PostureSeqDetector postureSeqDetector;

    // Task manager
    private OpenAppTaskManager openAppTaskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view
        viewBinding = ActivityOpenAppBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.webviewOverlay;

        // Init a gesture detector
        postureSeqDetector = new PostureSeqDetector();

        // Init a task manager
        openAppTaskManager = (OpenAppTaskManager) OpenAppTaskManager.getInstance(this);

        // Start camera
        if (permissionManager.isAllPermissionsGranted()) {
            singleCameraSource.startCamera();
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

        // Get motion configurations
        final Map<String, Object> postureConfig = postureSeqDetector.getMotion(className, coordinates);

        // Get the motion task
        Map<String, Object> motionTask = openAppTaskManager.getTask(postureConfig);

        // Get the task
        TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(motionTask.get("task"));
        if (task == TaskManager.MotionTask.OPEN_APP) {
            String url = (String) Objects.requireNonNull(motionTask.get("url"));
            viewBinding.webView.loadUrl(url);
        }
    }
}
