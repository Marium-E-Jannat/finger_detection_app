package com.ubcohci.fingerdetection.detectors;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class GestureDetector implements BaseDetector {

    @Override
    public Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates) {
        Map<String, Object> postureConfig = new HashMap<>();
        postureConfig.put("postures", new String[0]);
        return postureConfig;
    }
}
