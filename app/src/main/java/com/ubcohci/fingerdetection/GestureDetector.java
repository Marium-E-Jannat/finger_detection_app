package com.ubcohci.fingerdetection;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GestureDetector {
    // An enum defining whether the current posture
    // is a stand-alone or part of a gesture.
    public enum MotionType {POSTURE, GESTURE, WAITING}

    // An enum define all possible task for each detection result
    public enum PostureTask {
        SWITCH_VOLUME,
        SWITCH_VIDEO,
        SWITCH_BRIGHTNESS,
        ADJUST_VOLUME_DOWN,
        ADJUST_VOLUME_UP,
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

    // A integer representing max time-out
    // This is updated on based on newly calculated latency
    private int maxTimeout;

    // Representing the time marker for last detected posture.
    private long lastDetectTime;

    // A flag to check if timeOut situation is reached!
    private boolean isTimeout;

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

        // One finger - one finger – one finger – brightness level 10
        gestures.add(new String[] {postures[0], postures[0], postures[0]});
        gestureToBrightness.put(String.format(Locale.CANADA, "%s_%s_%s", postures[0], postures[0], postures[0]), 10);
        // One finger - one finger – two fingers – brightness level 20
        gestures.add(new String[] {postures[0],postures[0], postures[1]});
        gestureToBrightness.put(String.format(Locale.CANADA, "%s_%s_%s", postures[0], postures[0], postures[1]), 20);
    }

    /**
     * Get the task to perform based on the current posture.
     * @param posture The class name of the current posture.
     * @return A enum representing a task.
     */
    public PostureTask getMotionTask(@NonNull String posture) {
        if (posture.equals(postures[3]) || posture.equals(postures[4])) {
            return PostureTask.SWITCH_VIDEO;
        } else if (posture.equals(postures[0]) || posture.equals(postures[1]) || posture.equals(postures[2])) {
            return PostureTask.SWITCH_VOLUME;
        } else {
            return PostureTask.NONE;
        }
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
     * Get the posture's class name at the current index.
     * @param index The index of the posture
     * @return The posture's class name
     */
    public String getPostureName(int index) {
        return index > postures.length - 1? null: postures[index];
    }

    private boolean isNextHash(@NonNull String className) {
        return className.equals(getPostureName(3));
    }

    private String getNextHash() {
        currentIndex = (currentIndex + 1) % videoHashes.length;
        return videoHashes[currentIndex];
    }

    private String getPreviousHash() {
        currentIndex--; // Decrement counter
        if (currentIndex < 0) {
            currentIndex = videoHashes.length - 1;
        }
        return videoHashes[currentIndex];
    }
}
