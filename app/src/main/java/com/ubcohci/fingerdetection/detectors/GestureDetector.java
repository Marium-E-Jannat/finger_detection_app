package com.ubcohci.fingerdetection.detectors;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class GestureDetector implements BaseDetector {
    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        SKIP
    }

    // Keep a record of the previous coordinates
    private Map<String, Integer> previousCoordinates = null;

    // Keep a buffer of postures
    private String[] postureBuffer = new String[1]; // Only need 1 slot

    // Define threshold
    private static final int pixelThreshold = 50; // 100 px

    @Override
    public Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates) {
        Map<String, Object> postureConfig = new HashMap<>();

        // Check x-axis
        if (previousCoordinates == null) {
            postureConfig.put("postures", new String[0]);
        } else {

            postureConfig.put("horizontal_axis", new String[0]);
            postureConfig.put("vertical_axis", new String[0]);
        }

        previousCoordinates = coordinates;
        return postureConfig;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void dispose() {

    }
}
