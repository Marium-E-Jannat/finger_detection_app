package com.ubcohci.fingerdetection.tasks;

import static com.ubcohci.fingerdetection.detectors.GestureDetector.Direction;
import static com.ubcohci.fingerdetection.tasks.TaskManager.MotionTask;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Map;

@RunWith(Parameterized.class)
public class ImageBrowsingTaskManagerUnitTest extends TaskManagerUnitTest {
    public static final int repeat = TaskResource.imageURLs.length * 2;
    public final int[] indexes;
    public final Direction horizontalDirection;
    public final Direction verticalDirection;
    public final MotionTask expectedTask;
    public int startImageIndex;

    @Override
    @Before
    public void init() {
        taskManager =  ImageBrowsingTaskManager.getInstance();
    }

    @Override
    @After
    public void dispose() {}

    @Override
    public Map<String, Object> getPostureConfig(int[] indexes) {
        Map<String, Object> postureConfig = super.getPostureConfig(indexes);
        if (indexes.length > 0) {
            postureConfig.put("horizontal_direction", horizontalDirection);
            postureConfig.put("vertical_direction", verticalDirection);
        }
        return postureConfig;
    }

    public ImageBrowsingTaskManagerUnitTest(
            int[] indexes,
            Direction horizontalDirection,
            Direction verticalDirection,
            MotionTask expectedTask,
            int startImageIndex) {
        this.indexes = indexes;
        this.horizontalDirection = horizontalDirection;
        this.verticalDirection = verticalDirection;
        this.expectedTask = expectedTask;
        this.startImageIndex = startImageIndex;
    }

    @Parameterized.Parameters(name = "Case {index}: H={1}, V={2}, Task={3}, StartIndex={4}")
    public static Iterable<Object[]> getTestConfig() {
        return Arrays.asList(new Object[][] {
                {new int[] {0}, Direction.LEFT, Direction.UP, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.LEFT, Direction.DOWN, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.LEFT, Direction.SKIP, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.RIGHT, Direction.UP, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.RIGHT, Direction.DOWN, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.RIGHT, Direction.SKIP, MotionTask.SLIDE_PAGE, 0},
                {new int[] {0}, Direction.SKIP, Direction.SKIP, MotionTask.NONE, 0},
                {new int[] {0}, Direction.SKIP, Direction.UP, MotionTask.NONE, 0},
                {new int[] {0}, Direction.SKIP, Direction.DOWN, MotionTask.NONE, 0},
                {new int[0], null, null, MotionTask.NONE, 0},
        });
    }

    @Test
    public void imageBrowsingTest() {
        // Get the posture configuration
        Map<String, Object> postureConfig = this.getPostureConfig(this.indexes);

        // A work-around for RepeatedTest
        for (int i = 0; i < repeat; i++) {
            // Get the task config
            Map<String, Object> taskConfig = taskManager.getTask(postureConfig);

            // Get fields
            Object task = taskConfig.get("task");
            Object index = taskConfig.get("index");

            // Check task field
            assertNotNull(task);
            assertTrue(task instanceof TaskManager.MotionTask && task == expectedTask);

            if (task == TaskManager.MotionTask.NONE) {
                assertNull(index);
            } else {
                assertEquals(startImageIndex, index);

                switch (this.horizontalDirection) {
                    case LEFT:
                        startImageIndex--;
                        if (startImageIndex < 0) {
                            startImageIndex = TaskResource.imageURLs.length - 1;
                        }
                        break;
                    case RIGHT:
                        startImageIndex = (startImageIndex + 1) % TaskResource.imageURLs.length;
                        break;
                    default:
                }
            }
        }
    }
}
