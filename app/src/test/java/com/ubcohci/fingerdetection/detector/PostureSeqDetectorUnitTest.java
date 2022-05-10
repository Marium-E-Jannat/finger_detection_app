package com.ubcohci.fingerdetection.detector;

import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class PostureSeqDetectorUnitTest extends DetectorUnitTest {

    @Before
    @Override
    public void init() {
        detector = new PostureSeqDetector();
        detector.initialize();
    }

    public PostureSeqDetectorUnitTest() {

    }


    @Parameterized.Parameters
    public static Iterable<Object[]> getTestConfig() {
        return Arrays.asList(new Object[][] {
                {new int[] {0, 1, 2, -1}}
        });
    }


    @After
    @Override
    public void dispose() {
        detector.dispose();
    }

    @Test
    public void testPostureExist() {}
}
