package com.ubcohci.fingerdetection.tasks;

import android.content.Context;
import android.media.AudioManager;

import com.ubcohci.fingerdetection.application.VideoControlActivityV2;
import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class VideoControlTaskManager implements TaskManager {
    public enum ControlVersion {
        V1, V2
    }

    // A list of possible videos to switch between
    private static final String[] videoHashes = new String[] {
            "HzeK7g8cD0Y",
            "UwxatzcYf9Q",
            "-QuVe-hjMs0"
    };

    private static final int FORWARDS = 0;
    private static final int BACKWARDS = 1;

    // Internal track for the current video
    private int currentIndex = -1;

    // Version of the control activity
    private ControlVersion version;

    // A mapping from posture to volume level
    private final Map<String, Integer> postureToVolume = new HashMap<>();

    // A mapping from sequence of postures to volume level
    private final Map<String, Integer> postureSeqToBrightness = new HashMap<>();

    // A mapping from posture to video switch direction
    private final Map<String, Integer> postureToDirection = new HashMap<>();

    private static TaskManager _instance;

    public static TaskManager getInstance(ControlVersion version) {
        if (_instance == null) {
            _instance = new VideoControlTaskManager(version);
        }
        return _instance;
    }

    private VideoControlTaskManager(ControlVersion version) {
        this.version = version;

        // Get max audio value
        int maxVolume = TaskResource.maxVolume;

        // Straight index finger to set the sound at 10%
        postureToVolume.put(BaseDetector.postures[0], maxVolume / 10);

        // Straight two fingers to set the sound at 50%
        postureToVolume.put(BaseDetector.postures[1], maxVolume / 2);

        // Straight all fingers to set the sound at 100%
        postureToVolume.put(BaseDetector.postures[2], maxVolume);

        // all-hook-all – brightness level 90
        postureSeqToBrightness.put(
                String.format(Locale.CANADA, "%s_%s_%s", BaseDetector.postures[4], BaseDetector.postures[2], BaseDetector.postures[4]), 90);

        // hook-all-hook – brightness level 2
        postureSeqToBrightness.put(
                String.format(Locale.CANADA, "%s_%s_%s", BaseDetector.postures[2], BaseDetector.postures[4], BaseDetector.postures[2]), 2);

        // V forward
        postureToDirection.put(BaseDetector.postures[3], FORWARDS);

        // Hook backwards
        postureToDirection.put(BaseDetector.postures[4], BACKWARDS);
    }

    @Override
    public Map<String, Object> getTask(Map<String, Object> postureConfig) {
        // Get the postures
        String[] postures = (String[]) Objects.requireNonNull(postureConfig.get("postures"));

        // Construct a task configuration
        final Map<String, Object> taskConfig = new HashMap<>();

        MotionTask task;
        // Check posture
        switch (postures.length) {
            case 1:
                // Get posture
                String posture = postures[0];
                if (isSwitchingVideo(posture)) {
                    task = MotionTask.SWITCH_VIDEO;
                    taskConfig.put("url", Objects.requireNonNull(postureToDirection.get(posture)) == FORWARDS?
                            getNextHash(): getPreviousHash());
                } else {
                    // One is for volume
                    task = MotionTask.SWITCH_VOLUME;

                    // Try to get volume level
                    Integer volumeLevel = postureToVolume.get(posture);

                    if (volumeLevel == null) {
                        task = MotionTask.NONE;
                    } else {
                        taskConfig.put("volume", volumeLevel);
                    }
                }
                break;
            case 2:
                task = MotionTask.SWITCH_VIDEO_FRAME;
                // TODO: Add implementation for switching video frames
                break;
            case 3:
                task = MotionTask.SWITCH_BRIGHTNESS;

                // Get posture sequence in string
                String postureSqStr = TaskManager.getPostureSeqToString(
                        Arrays.asList(postures)
                );
                // Try to get brightness
                Integer brightness = postureSeqToBrightness.get(postureSqStr);
                if (brightness == null) {
                    task = MotionTask.NONE;
                } else {
                    taskConfig.put("brightness", brightness);
                }
                break;
            default: task = MotionTask.NONE;
        }

        taskConfig.put("task", task);
        return taskConfig;
    }

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

    private boolean isSwitchingVideo(String posture) {
        if (version == ControlVersion.V2) {
            return posture.equals(BaseDetector.postures[BaseDetector.maxNumOfPostures - 1])
                    || posture.equals(BaseDetector.postures[BaseDetector.maxNumOfPostures - 2]);
        } else {
            return posture.equals(BaseDetector.postures[3]) || posture.equals(BaseDetector.postures[4]);
        }
    }
}
