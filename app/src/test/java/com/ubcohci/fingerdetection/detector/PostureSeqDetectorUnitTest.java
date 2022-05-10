package com.ubcohci.fingerdetection.detector;

import com.google.firebase.database.FirebaseDatabase;
import com.ubcohci.fingerdetection.detectors.BaseDetector;
import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class PostureSeqDetectorUnitTest  {

    @Test
    public void runTestWithPostureSeqDetector() {
        JUnitCore.runClasses(PostureSeqSubTest.class);
    }

    @RunWith(Parameterized.class)
    public static class PostureSeqSubTest extends DetectorUnitTest {

        public final int[] sequence, expectedSequence;

        @Before
        @Override
        public void init() {
            detector = new PostureSeqDetector(); // Do not test initialize method here
        }

        @After
        @Override
        public void dispose() { }

        public PostureSeqSubTest(int[] sequence, int[] expectedSequence) {
            this.sequence = sequence;
            this.expectedSequence = expectedSequence;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> getTestConfig() {
            return Arrays.asList(new Object[][] {
                    {new int[] {0, 1, 2, -1}, new int[] {0, 1, 2}},
                    {new int[] {0, -1, 2, 1}, new int[] {2, 1}},
                    {new int[] {0, 0, 1, 1, -1}, new int[] {0, 1}},
                    {new int[] {0, 1}, new int[0]},
                    {new int[] {-1}, new int[0]},
                    {new int[0], new int[0]}
            });
        }

        @Test
        public void testPostureConfigOutput() {
            // Create an empty coordinate map
            Map<String, Integer> coordinates = new HashMap<>();

            // Define a posture config
            Map<String, Object> postureConfig = null;

            // Sequentially pass each class name to the detector
            for (int index: this.sequence) {
                postureConfig = detector.getMotion(BaseDetector.postures[index], coordinates);
            }

            // If the test case has no item
            if (postureConfig == null) {
                postureConfig = new HashMap<>();
                postureConfig.put("postures", new String[0]);
            }

            assertTrue(checkEqualArrays(
                    (String[]) postureConfig.get("posture"),
                    super.postureIndexesToNames(this.expectedSequence)
            ));
        }

        protected boolean checkEqualArrays(String[] a1, String[] a2) {
            return Arrays.equals(a1, a2);
        }
    }
}
