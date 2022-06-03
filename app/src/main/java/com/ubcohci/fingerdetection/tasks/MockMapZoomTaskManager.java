package com.ubcohci.fingerdetection.tasks;

import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockMapZoomTaskManager implements TaskManager {

    private static MockMapZoomTaskManager _instance;

    public static MockMapZoomTaskManager getInstance() {
        if (_instance == null) {
            _instance = new MockMapZoomTaskManager();
        }
        return _instance;
    }

    @Override
    public Map<String, Object> getTask(Map<String, Object> postureConfig) {
        // Get the postures
        String[] postures = (String[]) Objects.requireNonNull(postureConfig.get("postures"));

        final Map<String, Object> taskConfig = new HashMap<>();
        MotionTask task;
        if (postures.length != 2) {
            task = MotionTask.NONE;
        } else if (isZoomIn(postures)){
            task = MotionTask.ZOOM_IN;
        } else if (isZoomOut(postures)) {
            task = MotionTask.ZOOM_OUT;
        } else {
            task = MotionTask.NONE;
        }

        taskConfig.put("task", task);
        return taskConfig;
    }

    @Override
    public void init() { }

    private boolean isZoomIn(String[] postures) {
        if (postures[0] == null || postures[1] == null) {
            return false;
        }
        return postures[0].equals(BaseDetector.postures[3]) && postures[1].equals(BaseDetector.postures[1]);
    }

    private boolean isZoomOut(String[] postures) {
        if (postures[0] == null || postures[1] == null) {
            return false;
        }
        return postures[0].equals(BaseDetector.postures[1]) && postures[1].equals(BaseDetector.postures[3]);
    }
}
