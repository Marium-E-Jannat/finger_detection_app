package com.ubcohci.fingerdetection;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // A mapping from detection class to volume level
    private static final Map<String, Integer> postureToVolume = new HashMap<>();

    // A buffer to check for a gesture
    private final List<String> gestureBuffer = new ArrayList<>();

    // A list of possible videos to switch between
    private static final String[] videoHashes = new String[] {
            "HzeK7g8cD0Y",
            "UwxatzcYf9Q",
            "-QuVe-hjMs0"
    };

    // Internal track for the current video
    private int currentIndex = -1;

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
        gestureToBrightness.put(postures[0] + postures[0]+  postures[0], 10);
        // One finger - one finger – two fingers – brightness level 20
        gestures.add(new String[] {postures[0],postures[0], postures[1]});
        gestureToBrightness.put(postures[0] + postures[0]+  postures[1], 20);
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

    public Integer findVolumeLevel(@NonNull String posture) {
        return postureToVolume.get(posture);
    }

    public boolean isNextHash(@NonNull String className) {
        return className.equals(getPostureName(3));
    }

    public String getAnotherHash(@NonNull String className) {
        return isNextHash(className)? getNextHash(): getPreviousHash();
    }

    public String getNextHash() {
        currentIndex = (currentIndex + 1) % videoHashes.length;
        return videoHashes[currentIndex];
    }

    public String getPreviousHash() {
        currentIndex--; // Decrement counter
        if (currentIndex < 0) {
            currentIndex = videoHashes.length - 1;
        }
        return videoHashes[currentIndex];
    }

    public String getPostureName(int index) {
        return index > postures.length - 1? null: postures[index];
    }
}
