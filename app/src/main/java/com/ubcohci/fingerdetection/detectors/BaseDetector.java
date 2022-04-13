package com.ubcohci.fingerdetection.detectors;

import androidx.annotation.NonNull;

import java.util.Map;

public interface BaseDetector {
    // A list of all possible postures
    String[] postures = new String[] {
        "straight_index", "straight_two", "straight_all", "V", "hook index", "one on bop", "two on bop", "all on bop"
    };

    /**
     * Get a motion configurations
     * @param posture The current detected posture.
     * @param coordinates The bounding box of the current posture.
     * @return The motion configurations in map: {"postures": [], ...}
     */
    Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates);
}
