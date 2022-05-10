package com.ubcohci.fingerdetection.detector;

import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.Arrays;

public abstract class DetectorUnitTest {
    protected BaseDetector detector;

    /**
     * Set up resources for each test case.
     */
    public abstract void init();

    /**
     * Release resources for each test case.
     */
    public abstract void dispose();

    /**
     * Convert an array of posture indexes to posture names.
     * @param indexes The array of posture indexes.
     * @return An array of posture names.
     */
    public String[] postureIndexesToNames(int[] indexes) {
        final String[] names = new String[indexes.length];
        int i = 0;
        for (int index: indexes) {
            String name = (index < 0 || index >= BaseDetector.maxNumOfPostures)? "trying...": BaseDetector.postures[index];
            names[i++] = name;
        }
        return names;
    }

    protected boolean checkEqualArrays(String[] a1, String[] a2) {
        return Arrays.equals(a1, a2);
    }
}
