package com.ubcohci.fingerdetection.detectors;

import androidx.annotation.NonNull;

import java.util.Map;

public interface BaseDetector {
    // A list of all possible postures
    String[] postures = new String[] {
        "straight_index", "straight_two", "straight_all", "V", "hook index", "one on bop", "two on bop", "all on bop"
    };

    /**
     * Get the task to perform based on the current posture.
     * @param posture The class name of the current posture (exists or not exists)
     * @return A enum representing a task.
     */
    String[] getMotionTask(@NonNull String posture, @NonNull Map<String, Integer> coordinates);
}
