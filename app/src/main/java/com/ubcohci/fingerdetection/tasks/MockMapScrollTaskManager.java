package com.ubcohci.fingerdetection.tasks;

import com.ubcohci.fingerdetection.detectors.GestureDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockMapScrollTaskManager implements TaskManager {

    private static MockMapScrollTaskManager _instance;
    private int currentIndex;
    private final int numOfPages;

    public static TaskManager getInstance() {
        if (_instance == null) {
            _instance = new MockMapScrollTaskManager();
        }
        return _instance;
    }

    private MockMapScrollTaskManager() {
        this.numOfPages = TaskResource.imageURLs.length;
    }

    @Override
    public Map<String, Object> getTask(Map<String, Object> postureConfig) {
        // Get the postures
        String[] postures = (String[]) Objects.requireNonNull(postureConfig.get("postures"));

        // Construct a task configuration
        final Map<String, Object> taskConfig = new HashMap<>();

        MotionTask task;
        if (postures.length != 1) {
            task = MotionTask.NONE;
        } else {
            task = MotionTask.SLIDE_PAGE;

            // Get the horizontal direction
            // NOTE: Might need to change depending on the camera
            GestureDetector.Direction horizontalDirection = (GestureDetector.Direction) Objects.requireNonNull(postureConfig.get("horizontal_direction"));

            switch (horizontalDirection) {
                case SKIP: task = MotionTask.NONE; break;
                case LEFT: taskConfig.put("index", getNextPageIndex()); break;
                case RIGHT: taskConfig.put("index", getPreviousPageIndex()); break;
            }
        }
        // Add task
        taskConfig.put("task", task);
        return taskConfig;
    }

    @Override
    public void init() {
        this.currentIndex = 0;
    }

    private int getNextPageIndex() {
        return currentIndex = (currentIndex + 1) % numOfPages;
    }

    private int getPreviousPageIndex() {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = numOfPages - 1;
        }
        return currentIndex;
    }
}
