package com.ubcohci.fingerdetection.tasks;

import java.util.Map;

public interface TaskManager {
    // An enum define all possible task for each detection result
    enum MotionTask {
        SWITCH_VOLUME,
        SWITCH_VIDEO,
        SWITCH_BRIGHTNESS,
        SWITCH_VIDEO_FRAME,
        OPEN_APP,
        WAITING,
        NONE
    }

    /**
     * Get the task configuration base on posture configurations
     * @param postureConfig A posture configuration. Must be of form {"postures": [], ...}
     * @return A task configuration
     */
    Map<String, Object> getTask(Map<String, Object> postureConfig);
}
