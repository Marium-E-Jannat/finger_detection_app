package com.ubcohci.fingerdetection.application;

import android.os.Bundle;

import androidx.viewpager2.widget.ViewPager2;

import com.ubcohci.fingerdetection.databinding.ActivityMockMapScrollingBinding;
import com.ubcohci.fingerdetection.detectors.GestureDetector;
import com.ubcohci.fingerdetection.tasks.MockMapScrollTaskManager;
import com.ubcohci.fingerdetection.tasks.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockMapScrollingActivity extends BaseActivity {
    // Tag
    private static final String TAG = MockMapScrollingActivity.class.getName();

    // View binding
    ActivityMockMapScrollingBinding viewBinding;

    // Posture detector
    GestureDetector gestureDetector;

    // Task manager
    MockMapScrollTaskManager taskManager;

    private ViewPager2 imagePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMockMapScrollingBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        this.graphicOverlay = viewBinding.mockMapOverlay;
        this.imagePager = viewBinding.imageViewPager;

        gestureDetector = new GestureDetector();
        gestureDetector.initialize();

        taskManager = (MockMapScrollTaskManager) MockMapScrollTaskManager.getInstance();
        taskManager.init();

        if (permissionManager.isAllPermissionsGranted()) {
            singleCameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        super.handleAppTask(data);

        // Get the class name
        // Get class name
        String className = data.getString("class_name");

        // Extract bounding box of the posture to detect gesture
        Map<String, Integer> coordinates = new HashMap<>();
        coordinates.put("top", data.getInt("y_max"));
        coordinates.put("bottom", data.getInt("y_min"));
        coordinates.put("left", data.getInt("x_min"));
        coordinates.put("right", data.getInt("x_max"));

        // Get the posture
        final Map<String, Object> gestureConfig = gestureDetector.getMotion(className, coordinates);

        // Get the task
        final Map<String, Object> taskConfig = taskManager.getTask(gestureConfig);

        final TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(taskConfig.get("task"));

        if (task == TaskManager.MotionTask.SLIDE_PAGE) {
            int index = (Integer) Objects.requireNonNull(taskConfig.get("index"));
            slidePager(index);
        }
    }

    private void slidePager(int index) {
        imagePager.setCurrentItem(index);
    }
}
