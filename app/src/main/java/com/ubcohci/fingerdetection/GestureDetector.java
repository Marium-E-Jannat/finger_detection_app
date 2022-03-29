package com.ubcohci.fingerdetection;

import java.util.HashMap;
import java.util.Map;

public class GestureDetector {

    // A mapping from detection class to volume level
    private static final Map<String, Integer> postureToVolume = new HashMap<>();

    // TODO: Add a buffer to check gesture

    static {
        //Straight index finger to set the sound at 10x
        postureToVolume.put("straight_index", 10);
        //Straight two fingers to set the sound at 20x
        postureToVolume.put("straight_two", 20);
        //Straight all fingers to set the sound at 30x
        postureToVolume.put("straight_all", 30);

        // Single finger, then two fingers to increase the volume
        // Two fingers, then a single finger to decrease the volume

        // One finger - one finger – one finger – brightness level 10
        // One finger - one finger – two fingers – brightness level 20
    }

    public GestureDetector() {}

    public Integer findVolumeLevel(String posture) {
        return posture == null? null: postureToVolume.get(posture);
    }
}
