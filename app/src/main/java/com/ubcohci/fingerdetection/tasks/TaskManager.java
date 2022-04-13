package com.ubcohci.fingerdetection.tasks;

import android.content.Context;
import java.util.Map;
import java.util.List;

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

    /***
     * Get an singleton instance of classes implementing this interface.
     * @return An instance of classes implementing this interface.
     */
    TaskManager getInstance(Context context);

    /**
     * Get the string representation of the current flushed posture buffer.
     * @return String representation of the posture sequence.
     */
    static String getPostureSeqToString(List<String> postures) {
        final StringBuilder stringBuilder = new StringBuilder();
        String delimiter = "";
        for (String s: postures) {
            stringBuilder.append(delimiter);
            stringBuilder.append(s);
            delimiter = "_";
        }
        return stringBuilder.toString();
    }
}
