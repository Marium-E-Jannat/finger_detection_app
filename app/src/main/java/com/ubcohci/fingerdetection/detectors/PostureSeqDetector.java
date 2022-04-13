package com.ubcohci.fingerdetection.detectors;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to buffer and detect posture/gesture.
 */
public class PostureSeqDetector implements BaseDetector {
    private static final String TAG = "GestureDetector";

    // A buffer to check for a gesture
    private final List<String> gestureBuffer = new ArrayList<>();

    // Max value of tolerant count
    private static final int maxTolerantCount = 0;

    // A tolerance count
    private int toleranceCount = 1;

    /**
     * Constructor
     */
    public PostureSeqDetector() {}

    @SuppressWarnings("unused")
    @Override
    public Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates) {
        String[] postureSeq;
        if (isPostureExist(posture)) { // If the posture exists
            addToBuffer(posture); // Add the new posture to buffer
            postureSeq = new String[0];
            // Reset tolerant count
            toleranceCount = 0;
        } else { // Server cannot recognize a posture
            // Check tolerance count
            if (toleranceCount < maxTolerantCount) {
                toleranceCount++;
                postureSeq = new String[0];
            } else {
                // Keep a holder of all postures in buffer
                postureSeq = gestureBuffer.toArray(new String[0]);

                gestureBuffer.clear(); // Clear buffer

                // Reset tolerant count
                toleranceCount = 0;
            }
        }
        Log.d(TAG, "Posture Sequence: " + Arrays.toString(postureSeq));
        Map<String, Object> postureConfig = new HashMap<>();
        postureConfig.put("postures", postureSeq);
        return postureConfig;
    }

    /**
     * Check if a posture is supported.
     * @param posture The posture's class name.
     * @return Whether a posture is supported.
     */
    private boolean isPostureExist(String posture) {
        if (posture != null) {
            for (String _posture: BaseDetector.postures) {
                if (posture.equals(_posture)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add a posture to an internal buffer.
     * @param posture The posture's class name.
     */
    private void addToBuffer(String posture) {
        // Check if the last element is the same as posture
        // If so, don't add
        if (gestureBuffer.isEmpty() || !gestureBuffer.get(gestureBuffer.size() - 1).equals(posture)) {
            gestureBuffer.add(posture);
        }
    }
}
