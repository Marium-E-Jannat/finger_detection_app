package com.ubcohci.fingerdetection.tasks;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.util.Arrays;
import java.util.Map;

@RunWith(Parameterized.class)
public class OpenAppTaskManagerUnitTest extends TaskManagerUnitTest {
    public final int[] indexes;
    public final String url;

    public OpenAppTaskManagerUnitTest(int[] indexes, String url) {
        this.indexes = indexes;
        this.url = url;
    }

    @Parameterized.Parameters(name = "Case {index}: [id: {0}, url: {1}]}")
    public static Iterable<Object[]> getTestConfig() {
        return Arrays.asList(new Object[][] {
                {new int[] {0}, OpenAppTaskManager.urls[0]},
                {new int[] {1}, OpenAppTaskManager.urls[1]},
                {new int[] {3}, null},
                {new int[] {1, 2}, null},
                {new int[] {0, 1}, null}
        });
    }

    @Override
    @Before
    public void init() {
        taskManager = OpenAppTaskManager.getInstance();
    }

    @Override
    @Before
    public void dispose() {}

    @Test
    public void openAppTest() {
        // Construct a sample posture information map
        Map<String, Object> postureConfig = getPostureConfig(this.indexes);

        // Get the task
        Map<String, Object> taskConfig = taskManager.getTask(postureConfig);

        // Get the task config
        Object actualUrl = taskConfig.get("url");
        Object actualTask = taskConfig.get("task");

        // Check if the return field is in the right format
        assertNotNull(actualTask);
        assertTrue(actualTask instanceof TaskManager.MotionTask);

        if (actualTask == TaskManager.MotionTask.NONE) {
            assertNull(actualUrl);
        } else if (actualTask == TaskManager.MotionTask.OPEN_APP) {
            // Check if the url matches expected result
            assertNotNull(actualUrl);
            assertEquals(this.url, (String) actualUrl);
        } else {
            fail("Expected OPEN_APP but given " + actualTask);
        }
    }
}
