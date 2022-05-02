package com.ubcohci.fingerdetection.detector;

import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PostureSeqDetectorTest {
    public PostureSeqDetector detector;

    @Before
    public void init() {
        detector = new PostureSeqDetector();
        detector.initialize();
    }

    @After
    public void tearDown() {
        detector.dispose();
    }

    @Test
    public void testPostureExist() {}

    @Test
    public void testSinglePostureVolume() {}

    @Test
    public void testSinglePostureBrightness() {}

    @Test
    public void testSinglePostureVideoSwitch() {}
}
