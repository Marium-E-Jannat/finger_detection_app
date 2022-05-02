package com.ubcohci.fingerdetection.tasks;

import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class TaskManagerUnitTest {
    protected TaskManager taskManager;

    /**
     * Set up resources for each test case.
     */
    public abstract void init();

    /**
     * Release resources for each test case.
     */
    public abstract void dispose();

    /**
     * Create a sample posture configuration.
     * @param indexes The indexes of posture to add in the configuration map.
     * @return The posture configuration map.
     */
    public static Map<String, Object> getPostureConfig(int[] indexes) {
        Map<String, Object> postureConfig = new HashMap<>();

        ArrayList<String> postures = new ArrayList<>();

        for (int index: indexes) {
            postures.add(BaseDetector.postures[index]);
        }

        postureConfig.put("postures", postures.toArray(new String[0]));
        return postureConfig;
    }
}
