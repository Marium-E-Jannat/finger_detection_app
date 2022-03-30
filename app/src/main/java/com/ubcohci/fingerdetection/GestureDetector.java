package com.ubcohci.fingerdetection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestureDetector {

    public enum PostureTask {
        SWITCH_VOLUME,
        SWITCH_VIDEO,
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
        // Two fingers, then a single finger to decrease the volume

        // One finger - one finger – one finger – brightness level 10
        // One finger - one finger – two fingers – brightness level 20

    }

    public GestureDetector() {}

    public PostureTask getPostureTask(String posture) {
        if (posture.equals(postures[3]) || posture.equals(postures[4])) {
            return PostureTask.SWITCH_VIDEO;
        } else if (posture.equals(postures[0]) || posture.equals(postures[1]) || posture.equals(postures[2])) {
            return PostureTask.SWITCH_VOLUME;
        } else {
            return PostureTask.NONE;
        }
    }

    public Integer findVolumeLevel(String posture) {
        return posture == null? null: postureToVolume.get(posture);
    }

    public boolean isNextHash(String className) {
        return className.equals(getPostureName(3));
    }
    public String getAnotherHash(String className) {
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
