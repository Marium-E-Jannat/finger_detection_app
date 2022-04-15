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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ImageBrowsingActivity extends BaseActivity {

    private ActivityImageBrowingBinding viewBinding;

    private GestureDetector gestureDetector;

    private ViewPager2 imagePager;

    private static final String[] imageURLs = new String[] {
            "https://news.ok.ubc.ca/wp-content/uploads/2019/03/housing-770.jpg",
            "https://finance.cms.ok.ubc.ca/wp-content/uploads/sites/73/2021/04/UBC-Okanagan-Engineering-6-research.jpg",
            "https://students.cms.ok.ubc.ca/wp-content/uploads/sites/90/2019/05/aerial-campus-okanagan-study-abroad-at-ubc-and-exchange-research-abroad-and-1170-wide.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityImageBrowingBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.imageBrowingGraphicOverlay;

        // Initialize a gesture detector
        gestureDetector = new GestureDetector();

        // Set pager
        imagePager = viewBinding.imagePager;
        FragmentStateAdapter pageAdapter = new ImageSlidePagerAdapter(this, ImageBrowsingActivity.imageURLs);
        imagePager.setAdapter(pageAdapter);

        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
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
        final Map<String, Object> gesture = gestureDetector.getMotion(className, coordinates);

        // Get the task configuration
        final Map<String, Object> taskConfig = null;

        // TODO: Add PageManager

        // TODO: Check if slidePager should be called
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
