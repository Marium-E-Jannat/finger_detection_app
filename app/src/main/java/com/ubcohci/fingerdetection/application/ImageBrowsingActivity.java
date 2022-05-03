package com.ubcohci.fingerdetection.application;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.ubcohci.fingerdetection.databinding.ActivityImageBrowingBinding;
import com.ubcohci.fingerdetection.detectors.GestureDetector;
import com.ubcohci.fingerdetection.fragments.ImageSlidePageFragment;
import com.ubcohci.fingerdetection.tasks.ImageBrowsingTaskManager;
import com.ubcohci.fingerdetection.tasks.TaskManager;
import com.ubcohci.fingerdetection.tasks.TaskResource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ImageBrowsingActivity extends BaseActivity {

    private ActivityImageBrowingBinding viewBinding;

    private GestureDetector gestureDetector;

    private ImageBrowsingTaskManager imageBrowsingTaskManager;

    private ViewPager2 imagePager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityImageBrowingBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.imageBrowingGraphicOverlay;

        // Initialize a gesture detector
        gestureDetector = new GestureDetector();

        // Add a task manager
        imageBrowsingTaskManager = ImageBrowsingTaskManager.getInstance();
        imageBrowsingTaskManager.init();

        // Set pager
        imagePager = viewBinding.imagePager;
        FragmentStateAdapter pageAdapter = new ImageSlidePagerAdapter(this, TaskResource.imageURLs);
        imagePager.setAdapter(pageAdapter);

        if (permissionManager.isAllPermissionsGranted()) {
            singleCameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        super.handleAppTask(data);

        // Get the class name
        // Get class name
        String className = data.getString("class_name");

        // Extract bounding box of the posture to detect gesture
        Map<String, Integer> coordinates = new HashMap<>();
        coordinates.put("top", data.getInt("y_min"));
        coordinates.put("bottom", data.getInt("y_max"));
        coordinates.put("left", data.getInt("x_min"));
        coordinates.put("right", data.getInt("x_max"));

        // Put through the detector
        final Map<String, Object> gestureConfig = gestureDetector.getMotion(className, coordinates);

        // Get the task configuration
        final Map<String, Object> taskConfig = imageBrowsingTaskManager.getTask(gestureConfig);

        // Check if slidePager should be called
        final TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(taskConfig.get("task"));

        if (task == TaskManager.MotionTask.SLIDE_PAGE) {
            int index = (Integer) Objects.requireNonNull(taskConfig.get("index"));
            slidePager(index);
        }
    }

    /**
     * Slide the image pager to index
     * @param index The index of the image page.
     */
    public void slidePager(int index) {
        imagePager.setCurrentItem(index);
    }

    /**
     * A simple pager adapter that represents imageURLs.length ImageSlidePageFragment objects, in
     * sequence.
     */
    private static class ImageSlidePagerAdapter extends FragmentStateAdapter {
        public final String[] imageURLs;

        public ImageSlidePagerAdapter(FragmentActivity fa, @NonNull String[] imageURLs) {
            super(fa);
            this.imageURLs = imageURLs;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new ImageSlidePageFragment(this.imageURLs[position]);
        }

        @Override
        public int getItemCount() {
            return this.imageURLs.length;
        }
    }
}
