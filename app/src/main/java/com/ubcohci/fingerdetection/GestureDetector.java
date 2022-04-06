package com.ubcohci.fingerdetection;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class to buffer and detect posture/gesture.
 */
public class GestureDetector {
    private static final String TAG = "GestureDetector";

    // An enum define all possible task for each detection result
    public enum MotionTask {
        SWITCH_VOLUME,
        SWITCH_VIDEO,
        SWITCH_BRIGHTNESS,
        ADJUST_VOLUME,
        ADVANCE_FRAME_LEFT,
        ADVANCE_FRAME_RIGHT,
        WAITING,
        NONE
    }

    // A list of possible videos to switch between
    private static final String[] videoHashes = new String[] {
            "HzeK7g8cD0Y",
            "UwxatzcYf9Q",
            "-QuVe-hjMs0"
    };


    // A list of all possible postures
    private static final String[] postures = new String[] {
            "straight_index",
            "straight_two",
            "straight_all",
            "V",
            "hook",
            "one_palm",
            "two_palm",
            "all_palm"
    };

    // A list of possible sequences of postures
    private static final List<String[]> gestures = new ArrayList<>();

    // A mapping from posture to volume level
    private static final Map<String, Integer> postureToVolume = new HashMap<>();

    // A mapping from gestures to volume level
    private static final Map<String, Integer> gestureToBrightness = new HashMap<>();

    // Internal track for the current video
    private int currentIndex = -1;

    // A buffer to check for a gesture
    private final List<String> gestureBuffer = new ArrayList<>();

    // An array holding the flushed buffer
    private String[] _currentGesture;

    // A integer representing max time-out
    // This is updated on based on newly calculated latency
    private int maxTimeout = 0;

    // Representing the time marker for last detected posture.
    private long lastDetectTime;

    // Max value of tolerant count
    private static final int maxTolerantCount = 3;

    // A tolerance count
    private int toleranceCount = 0;

    // A buffer for bounding boxes
    private Map<String, Integer> previousBoundingBox;

    // Bounding box change threshold
    private static final int pixelThreshold = 100; // dp

    /**
     * Constructor
     */
    public GestureDetector() {}

    // Static initializer
    static {
        //Straight index finger to set the sound at 10x
        postureToVolume.put(postures[0], 10);
        //Straight two fingers to set the sound at 20x
        postureToVolume.put(postures[1], 20);
        //Straight all fingers to set the sound at 30x
        postureToVolume.put(postures[2], 30);

        // V to switch to next video --> Use getNextHash()
        // Hook to switch to previous video --> Use getPreviousHash()

        // Single finger, then two fingers to increase the volume
        gestures.add(new String[] {postures[0], postures[1]});
        // Two fingers, then a single finger to decrease the volume
        gestures.add(new String[] {postures[1], postures[0]});

        // One finger - one finger – one finger – brightness level 40
        gestures.add(new String[] {postures[0], postures[0], postures[0]});
        gestureToBrightness.put(String.format(Locale.CANADA, "%s_%s_%s", postures[0], postures[0], postures[0]), 40);
        // One finger - one finger – two fingers – brightness level 80
        gestures.add(new String[] {postures[0],postures[0], postures[1]});
        gestureToBrightness.put(String.format(Locale.CANADA, "%s_%s_%s", postures[0], postures[0], postures[1]), 80);
    }

    /**
     * Get the task to perform based on the current posture.
     * @param posture The class name of the current posture (exists or not exists)
     * @return A enum representing a task.
     */
    public MotionTask getMotionTask(@NonNull String posture, Map<String, Integer> coordinates) {
        MotionTask task;
        if (isPostureExist(posture)) { // If the posture exists
            if (addToBuffer(posture)) { // Add the new posture to buffer
                // Check if timeout is true
                final long now = System.currentTimeMillis();
                // If there is a timeout, do nothing and clear buffer
                // Only check if in-between posture sequence
                if (gestureBuffer.size() > 0 && (now - lastDetectTime > maxTimeout)) {
                    gestureBuffer.clear(); // Clear all postures in buffer
                    task = MotionTask.NONE; // Set as none
                } else {
                    task = MotionTask.WAITING;
                }
                // Update lastDetectTime
                lastDetectTime = now;
            } else {
                // Add check for gesture (moving posture) using bounding box
                // Only check if there is only 1 posture in buffer
                if (gestureBuffer.size() > 1) {
                    task = MotionTask.WAITING;
                } else {
                    // Check if the change is significant
                    // Note: We focus on left sides
                    // as finger movements have its based mostly fixed
                    // And we only consider horizontal movements

                    // Get left
                    int newLeft = Objects.requireNonNull(coordinates.get("left"));
                    int oldLeft = Objects.requireNonNull(previousBoundingBox.get("left"));

                    if (Math.abs(newLeft - oldLeft) > pixelThreshold) {
                        task = newLeft > oldLeft? MotionTask.ADVANCE_FRAME_RIGHT: MotionTask.ADVANCE_FRAME_LEFT;
                    } else {
                        task = MotionTask.WAITING;
                    }
                }
            }

            // Set bounding box buffer to new coordinates
            previousBoundingBox = coordinates;

            // Reset tolerant count
            toleranceCount = 0;
        } else { // Server cannot recognize a posture
            // Check tolerance count
            if (toleranceCount < maxTolerantCount) {
                toleranceCount++;
                task = MotionTask.WAITING;
            } else {
                // Keep a holder of all postures in buffer
                final String[] gesture = gestureBuffer.toArray(new String[0]);

                // This means we will flush the buffer
                switch (gestureBuffer.size()) {
                    case 1: task = getPostureTask(gestureBuffer.get(0)); break; // Executing current posture
                    case 2:
                    case 3: task = getGestureTask(gesture); break; // Execute current gesture
                    default: task = MotionTask.NONE; break; // If there are more than 3 postures or no at all in buffer
                }

                // Update buffer holder
                _currentGesture = gesture;
                Log.d(TAG, Arrays.toString(_currentGesture));

                gestureBuffer.clear(); // Clear buffer

                // Reset tolerant count
                toleranceCount = 0;

                // Reset bounding box
                previousBoundingBox = null;
            }
        }
        Log.d(TAG, task.toString());
        return task;
    }

    /**
     * Find the volume level for the current posture.
     * @param posture The posture's class name
     * @return Volume level
     */
    public Integer findVolumeLevel(@NonNull String posture) {
        return postureToVolume.get(posture);
    }

    /**
     * Get the next video's hash based on the class name.
     * @param posture The class name of the current posture.
     * @return The hash for the corresponding video.
     */
    public String getAnotherHash(@NonNull String posture) {
        return isNextHash(posture)? getNextHash(): getPreviousHash();
    }

    /**
     * Get the brightness level for the current gesture
     * @param gesture The gesture string.
     * @return The brightness level.
     */
    public Integer getBrightnessLevel(@NonNull String gesture) {
        return gestureToBrightness.get(gesture);
    }

    /**
     * Update maxTimeout value (i.e. based on new average RTT.)
     * @param maxTimeout The new maxTimeout value.
     */
    public void setMaxTimeOut(int maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    /**
     * Get the posture's class name at the current index.
     * @param index The index of the posture
     * @return The posture's class name
     */
    public String getPostureName(int index) {
        return index > postures.length - 1? null: postures[index];
    }

    /**
     * Check whether the posture initiate forward or backward jump for the cursor
     * of the video list.s
     * @param className The posture's class name.
     * @return Whether the cursor should move forward or backward.
     */
    private boolean isNextHash(@NonNull String className) {
        return className.equals(getPostureName(3));
    }

    /**
     * Get the next hash in line for the next video.
     * @return The hash value of the next video.
     */
    private String getNextHash() {
        currentIndex = (currentIndex + 1) % videoHashes.length;
        return videoHashes[currentIndex];
    }

    /**
     * Get the next hash in line for the next video.
     * @return The hash value of the next video.
     */
    private String getPreviousHash() {
        currentIndex--; // Decrement counter
        if (currentIndex < 0) {
            currentIndex = videoHashes.length - 1;
        }
        return videoHashes[currentIndex];
    }

    /**
     * Check if a posture is supported.
     * @param posture The posture's class name.
     * @return Whether a posture is supported.
     */
    private boolean isPostureExist(String posture) {
        if (posture != null) {
            for (String _posture: postures) {
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
     * @return Whether the add operation is successful.
     */
    private boolean addToBuffer(String posture) {
        // Check if the last element is the same as posture
        // If so, don't add
        if (gestureBuffer.size() > 1 && gestureBuffer.get(gestureBuffer.size() - 1).equals(posture)) {
            return false;
        } else {
            gestureBuffer.add(posture);
            return true;
        }
    }

    /**
     * Get the MotionTask based on the posture.
     * @param posture The posture's class name.
     * @return The task corresponding to the posture.
     */
    private MotionTask getPostureTask(@NonNull String posture) {
        if (posture.equals(postures[3]) || posture.equals(postures[4])) {
            return MotionTask.SWITCH_VIDEO;
        } else if (posture.equals(postures[0]) || posture.equals(postures[1]) || posture.equals(postures[2])) {
            return MotionTask.SWITCH_VOLUME;
        } else {
            return MotionTask.NONE;
        }
    }

    /**
     * Get the MotionTask based on a list of postures (gesture)
     * @param gesture The list of posture's class name.
     * @return The task corresponding to the gesture.
     */
    private MotionTask getGestureTask(@NonNull String[] gesture) {
        for (String[] _gesture: gestures) {
            if (Arrays.equals(_gesture, gesture)) {
                return gesture.length == 2? MotionTask.ADJUST_VOLUME: MotionTask.SWITCH_BRIGHTNESS;
            }
        }
        return MotionTask.NONE;
    }

    /**
     * Get the string representation of the current flushed gesture from buffer.
     * @return String representation of the gesture.
     */
    public String getCurrentGestureInString() {
        final StringBuilder stringBuilder = new StringBuilder();
        String delimiter = "";
        for (String s: _currentGesture) {
            stringBuilder.append(delimiter);
            stringBuilder.append(s);
            delimiter = "_";
        }
        return stringBuilder.toString();
    }
}
