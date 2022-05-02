package com.ubcohci.fingerdetection.tasks;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


import java.util.Arrays;
import java.util.Map;

@RunWith(Parameterized.class)
public class OpenAppTaskManagerUnitTest extends TaskManagerUnitTest {
    public final int index;
    public final String url;

    public OpenAppTaskManagerUnitTest(int index, String url) {
        this.index = index;
        this.url = url;
    }

    @Parameterized.Parameters(name = "Case {index}: [id: {0}, url: {1}]}")
    public static Iterable<Object[]> getTestConfig() {
        return Arrays.asList(new Object[][] {
                {0, OpenAppTaskManager.urls[0]}, {1, OpenAppTaskManager.urls[1]}
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
        Map<String, Object> postureConfig = TaskManagerUnitTest.getPostureConfig(new int[] {this.index});

        // Get the task
        Map<String, Object> taskConfig = taskManager.getTask(postureConfig);

        // Get the url
        Object actualUrl = taskConfig.get("url");

        // Check if the return field is in the right format
        assertNotNull(actualUrl);

        // Check if the field is an instance of string
        assertTrue(actualUrl instanceof String);

        // Check if the url matches expected result
        assertEquals(this.url, (String) actualUrl);
    }
}
