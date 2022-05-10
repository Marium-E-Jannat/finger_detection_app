package com.ubcohci.fingerdetection.detector;

import static com.ubcohci.fingerdetection.detectors.GestureDetector.Direction;

import com.ubcohci.fingerdetection.detectors.BaseDetector;
import com.ubcohci.fingerdetection.detectors.GestureDetector;

import org.junit.After;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class GestureDetectorUnitTest {
    @Test
    public void runTestWithGestureDetector() {
        JUnitCore.runClasses(GestureDetectorSubTest.class);
    }

    @RunWith(Parameterized.class)
    public static class GestureDetectorSubTest extends DetectorUnitTest {
        public final int[] sequence, expectedPosture;
        public final int[][] coordinates;
        public final Direction expectedDirection;

        @Before
        @Override
        public void init() {
            detector = new GestureDetector();
        }

        @After
        @Override
        public void dispose() {

        }

        @Parameterized.Parameters
        public static Iterable<Object[]> getTestConfig() {
            // x-min, x-max, y-min, y-max
            return Arrays.asList(new Object[][] {
                    {new int[] {0, 0}, new int[][] {{0, 50, 0, 50}, {51, 101, 0, 50}}, new int[] {0}, Direction.LEFT},
                    {new int[] {1, 1}, new int[][] {{0, 50, 0, 50}, {51, 101, 0, 50}}, new int[] {1}, Direction.LEFT},
                    {new int[] {0, 0}, new int[][] {{51, 101, 0, 50}, {0, 50, 0, 50}}, new int[] {0}, Direction.RIGHT},
                    {new int[] {1, 1}, new int[][] {{51, 101, 0, 50}, {0, 50, 0, 50}}, new int[] {1}, Direction.RIGHT},
                    {new int[] {0, 1}, new int[][] {{51, 101, 0, 50}, {0, 50, 0, 50}}, new int[0], Direction.SKIP},
            });
        }

        public GestureDetectorSubTest(int[] sequence, int[][] coordinates, int[] expectedPosture, Direction expectedDirection) {
            this.sequence = sequence;
            this.coordinates = coordinates;
            this.expectedPosture = expectedPosture;
            this.expectedDirection = expectedDirection;
        }

        @Test
        public void testGestureOutput() {
            Map<String, Object> postureConfig = null;
            for (int i = 0; i < this.sequence.length; i++) {
                // Create a coordinate map
                Map<String, Integer> coordinates = new HashMap<>();
                coordinates.put("x-min", this.coordinates[i][0]);
                coordinates.put("x-max", this.coordinates[i][1]);
                coordinates.put("y-min", this.coordinates[i][2]);
                coordinates.put("y-max", this.coordinates[i][3]);

                // Pass through the detector
                postureConfig = detector.getMotion(BaseDetector.postures[sequence[i]], coordinates);
            }

            if (postureConfig == null) {
                postureConfig = new HashMap<>();
                postureConfig.put("postures", new String[0]);
            }

            String[] postures = (String[]) postureConfig.get("postures");
            Direction horizontalDirection = (Direction) postureConfig.get("horizontal_direction");
            Direction verticalDirection = (Direction) postureConfig.get("vertical_direction");

            // Posture must not be null
            assertNotNull(postures);

            // Length of posture must be either 0 or 1
            // And as expected
            assertTrue(checkEqualArrays(postures, super.postureIndexesToNames(this.expectedPosture)));

            if (postures.length == 0) {
                assertNull(horizontalDirection);
                assertNull(verticalDirection);
            } else {
                assertNotNull(horizontalDirection);
                assertNotNull(verticalDirection);
                assertEquals(this.expectedDirection, horizontalDirection);
            }
        }
    }

}
