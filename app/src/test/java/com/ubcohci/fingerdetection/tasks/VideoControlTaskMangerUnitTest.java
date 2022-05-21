package com.ubcohci.fingerdetection.tasks;

import static com.ubcohci.fingerdetection.tasks.TaskManager.MotionTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ubcohci.fingerdetection.detectors.BaseDetector;

import java.util.Arrays;
import java.util.Map;

@RunWith(Parameterized.class)
public class VideoControlTaskMangerUnitTest extends TaskManagerUnitTest {
    public static final int repeat = VideoControlTaskManager.videoHashes.length * 2;
    public final int[] indexes;
    public final MotionTask expectedTask;
    public Object expectedChange;
    public static final int maxVolume = TaskResource.maxVolume;

    @Override
    @Before
    public void init() {
        taskManager = VideoControlTaskManager.getInstance(VideoControlTaskManager.ControlVersion.V2);
        taskManager.init();
    }

    @Override
    @After
    public void dispose() {}

    @Parameterized.Parameters(name = "Case {index}: Task={1}, Value={2}")
    public static Iterable<Object[]> getTestConfig() {
        return Arrays.asList(new Object[][] {
                {new int[] {0}, MotionTask.SWITCH_VOLUME, maxVolume / 10},
                {new int[] {1}, MotionTask.SWITCH_VOLUME, maxVolume / 2},
                {new int[] {2}, MotionTask.SWITCH_VOLUME, maxVolume},
                {new int[] {4, 2, 4}, MotionTask.SWITCH_BRIGHTNESS, 90},
                {new int[] {2, 4, 2}, MotionTask.SWITCH_BRIGHTNESS, 2},
                {new int[] {BaseDetector.maxNumOfPostures - 1}, MotionTask.SWITCH_VIDEO, -1},
                {new int[] {BaseDetector.maxNumOfPostures - 2}, MotionTask.SWITCH_VIDEO, -1},
        });
    }

    public VideoControlTaskMangerUnitTest(
            int[] indexes,
            MotionTask expectedTask,
            Object expectedChange) {
        this.indexes = indexes;
        this.expectedTask = expectedTask;
        this.expectedChange = expectedChange;
    }

    @Override
    public Map<String, Object> getPostureConfig(int[] indexes) {
       return super.getPostureConfig(indexes);
    }

    @Test
    public void testVideoControl() {
        // Get the posture configuration
        Map<String, Object> postureConfig = this.getPostureConfig(this.indexes);

        // Get task
        Map<String, Object> taskConfig;
        if (expectedTask == MotionTask.SWITCH_VIDEO) {
            for (int i = 0; i < repeat; i++) {
                taskConfig = taskManager.getTask(postureConfig);
                // Check task
                Object actualTask = taskConfig.get("task");
                Object actualUrl = taskConfig.get("url");
                Object actualVolume = taskConfig.get("volume");
                Object actualBrightness = taskConfig.get("brightness");

                assertNotNull(actualTask);
                assertTrue(actualTask instanceof MotionTask && actualTask == expectedTask);
                assertNotNull(actualUrl);
                assertNull(actualVolume);
                assertNull(actualBrightness);

                if (indexes[0] == BaseDetector.maxNumOfPostures - 1 || indexes[0] == 3) {
                    expectedChange = ((Integer) expectedChange + 1) % VideoControlTaskManager.videoHashes.length;
                } else if (indexes[0] == BaseDetector.maxNumOfPostures - 2 || indexes[0] == 4) {
                    expectedChange = (Integer) expectedChange - 1;
                    if ((Integer) expectedChange < 0) {
                        expectedChange = VideoControlTaskManager.videoHashes.length - 1;
                    }
                }
                final String expectedUrl = VideoControlTaskManager.videoHashes[(Integer) expectedChange];
                assertEquals(expectedUrl, actualUrl);
            }

        } else {
            taskConfig = taskManager.getTask(postureConfig);

            // Check task
            Object actualTask = taskConfig.get("task");
            Object actualUrl = taskConfig.get("url");
            Object actualVolume = taskConfig.get("volume");
            Object actualBrightness = taskConfig.get("brightness");

            // Check task name
            assertNotNull(actualTask);
            assertTrue(actualTask instanceof MotionTask && actualTask == expectedTask);

            switch (expectedTask) {
                case NONE:
                    assertNull(actualUrl);
                    assertNull(actualVolume);
                    assertNull(actualBrightness);
                    break;
                case SWITCH_VOLUME:
                    assertNull(actualUrl);
                    assertNull(actualBrightness);
                    assertEquals(expectedChange, actualVolume);
                    break;
                case SWITCH_BRIGHTNESS:
                    assertNull(actualUrl);
                    assertNull(actualVolume);
                    assertEquals(expectedChange,actualBrightness);
                    break;
                default:
            }
        }
    }
}
