package com.ubcohci.fingerdetection.tasks;

import android.content.Context;

import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenAppTaskManager implements TaskManager {

    public static final String[] urls = {
            "https://www.youtube.com/",
            "https://www.facebook.com/"
    };

    private static TaskManager _instance;

    public static TaskManager getInstance() {
        if (_instance == null) {
            _instance = new OpenAppTaskManager();
        }
        return _instance;
    }

    private OpenAppTaskManager() {}

    @Override
    public Map<String, Object> getTask(Map<String, Object> postureConfig) {
        // Get the posture
        String[] postures = (String[]) Objects.requireNonNull(postureConfig.get("postures"));

        // Construct a task configuration
        final Map<String, Object> taskConfig = new HashMap<>();

        // For open app, only one posture is accepted
        if (postures.length != 1) {
            taskConfig.put("task", TaskManager.MotionTask.NONE);
        } else {
            // Get the posture
            String posture = postures[0];

            MotionTask task = MotionTask.OPEN_APP;
            // Check posture
            if (posture.equals(BaseDetector.postures[0])) {
                taskConfig.put("url", urls[0]);
            } else if (posture.equals(BaseDetector.postures[1])) {
                taskConfig.put("url", urls[1]);
            } else {
                task = MotionTask.NONE;
            }

            // Save task to task config
            taskConfig.put("task", task);
        }
        return taskConfig;
    }
}
