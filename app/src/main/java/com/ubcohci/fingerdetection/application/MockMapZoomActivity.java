package com.ubcohci.fingerdetection.application;

import android.os.Bundle;
import android.widget.ImageView;

import com.ubcohci.fingerdetection.databinding.ActivityMockMapZoomBinding;
import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;
import com.ubcohci.fingerdetection.tasks.MockMapZoomTaskManager;
import com.ubcohci.fingerdetection.tasks.TaskExecutor;
import com.ubcohci.fingerdetection.tasks.TaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockMapZoomActivity extends  BaseActivity {
    private static final String TAG = MockMapZoomActivity.class.getName();

    PostureSeqDetector postureSeqDetector;
    MockMapZoomTaskManager taskManager;
    ActivityMockMapZoomBinding viewBinding;
    ImageView mockMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMockMapZoomBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        this.graphicOverlay = viewBinding.mockMapZoomOverlay;
        this.mockMap = viewBinding.mockMap;

        this.postureSeqDetector = new PostureSeqDetector();
        postureSeqDetector.initialize();

        taskManager = (MockMapZoomTaskManager) MockMapZoomTaskManager.getInstance();
        taskManager.init();

        // Load the image
        TaskExecutor.loadImage(this, mockMap);

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
        final Map<String, Object> gestureConfig = postureSeqDetector.getMotion(className, coordinates);

        // Get the task
        final Map<String, Object> taskConfig = taskManager.getTask(gestureConfig);

        final TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(taskConfig.get("task"));

        switch(task) {
            case ZOOM_IN:
                TaskExecutor.zoomImage(mockMap, TaskExecutor.ZOOM_IN);
                break;
            case ZOOM_OUT:
                TaskExecutor.zoomImage(mockMap, TaskExecutor.ZOOM_OUT);
            default:
        }
    }
}
