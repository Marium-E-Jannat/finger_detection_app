package com.ubcohci.fingerdetection.tasks;

import com.google.android.gms.tasks.Task;
import com.ubcohci.fingerdetection.application.ImageBrowsingActivity;
import com.ubcohci.fingerdetection.detectors.GestureDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageBrowsingTaskManager implements TaskManager {
    // Number of pages in the slider
    private final int numOfPages;

    // Current page index
    private int currentIndex = 0;

    public ImageBrowsingTaskManager() {
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
            // NOTE: We don't use vertical direction for now for now.
            GestureDetector.Direction horizontalDirection = (GestureDetector.Direction) Objects.requireNonNull(postureConfig.get("horizontal_direction"));

            switch (horizontalDirection) {
                case SKIP: task = MotionTask.NONE; break;
                case LEFT: taskConfig.put("index", getPreviousPageIndex()); break;
                case RIGHT: taskConfig.put("index", getNextPageIndex()); break;
            }
        }
        // Add task
        taskConfig.put("task", task);
        return taskConfig;
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
