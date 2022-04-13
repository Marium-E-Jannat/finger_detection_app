package com.ubcohci.fingerdetection.application;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;
import com.ubcohci.fingerdetection.tasks.TaskManager;
import com.ubcohci.fingerdetection.tasks.VideoControlTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VideoControlActivity extends BaseActivity implements YouTubePlayer.OnInitializedListener {

    // TAGS
    private static final String TAG = "VideoControlActivity";

    // View binding
    public ActivityVideoControlBinding viewBinding;

    // Video player
    private YouTubePlayer player;

    // A gesture detector
    private PostureSeqDetector postureSeqDetector;

    // Task manager
    private VideoControlTaskManager videoControlTaskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.videoGraphicOverlay;

        // Init a gesture detector
        postureSeqDetector = new PostureSeqDetector();

        // Init a task manager
        videoControlTaskManager = (VideoControlTaskManager) VideoControlTaskManager.getInstance(this);

        // Init youtube API
        initYoutubeAPI();

        // Start camera
        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    public void initYoutubeAPI() {
        // Initialize youtube API
        YouTubePlayerSupportFragmentX youtubeFragment = ((YouTubePlayerSupportFragmentX) getSupportFragmentManager().findFragmentById(viewBinding.ytPlayer.getId()));
        if (youtubeFragment != null) {
            youtubeFragment.initialize(BuildConfig.YOUTUBE_API_KEY, this);
        } else {
            Log.d(TAG, "No youtube fragment available!");
            this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            this.player.release();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        Log.d(TAG, "Video player is initialized!");
        // Get a hold of the youtube player
        this.player = youTubePlayer;

        // Play the the first video
        youTubePlayer.loadVideo("HzeK7g8cD0Y");
        youTubePlayer.play();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Log.d(TAG, "Video player failed to be initialized!");
        Log.d(TAG, youTubeInitializationResult.toString());
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        // Skip and wait when youtube API is not initialized
        // or when the video is paused
        if (this.player == null || this.isFinishing() || !this.player.isPlaying()) {
            return;
        }

        super.handleAppTask(data);
        // Get class name
        String className = data.getString("class_name");

        // Extract bounding box of the posture to detect gesture
        Map<String, Integer> coordinates = new HashMap<>();
        coordinates.put("top", data.getInt("y_min"));
        coordinates.put("bottom", data.getInt("y_max"));
        coordinates.put("left", data.getInt("x_min"));
        coordinates.put("right", data.getInt("x_max"));

        final String[] motion = postureSeqDetector.getMotion(className, coordinates);

        if (motion.length < 1) { // Do nothing if there is detected motion yet
            return;
        }

        // Construct a posture configuration
        Map<String, Object> postureConfig = new HashMap<>();
        postureConfig.put("postures", motion);

        // Get the motion task
        Map<String, Object> motionTask = videoControlTaskManager.getTask(postureConfig);

        // Get the task
        TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(motionTask.get("task"));

        // Get the task to perform based on posture
        switch (task) {
            case SWITCH_VOLUME:
                switchVolume((Integer) Objects.requireNonNull(motionTask.get("volume")));
                break;
            case SWITCH_BRIGHTNESS:
                switchBrightness((Integer) Objects.requireNonNull(motionTask.get("brightness")));
                break;
            case SWITCH_VIDEO:
                switchVideo((String) Objects.requireNonNull(motionTask.get("url")));
                break;
            case SWITCH_VIDEO_FRAME:
                // TODO: Call switch video frame
                break;
            default:
        }
    }

    public void switchVolume(int volumeLevel) {
        Log.d(TAG, "Setting volume to " + volumeLevel);

        // Use audio service to change volume
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isVolumeFixed()) {
            Toast.makeText(this, "Volume is in fixed mode!", Toast.LENGTH_SHORT).show();
        } else {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    volumeLevel,
                    AudioManager.FLAG_SHOW_UI
            );
        }
    }

    public void switchVideo(@NonNull String newHash) {
        this.player.loadVideo(newHash);
    }

    public void switchBrightness(int brightness) {
        Log.d(TAG, "Brightness change to: " + brightness);

        // Set current screen brightness
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness / 100f;
        getWindow().setAttributes(layoutParams);
    }

    public void advanceVideoFrame(boolean forward) {
        if (this.player == null) return;
        this.player.seekRelativeMillis(forward? 2000: -2000);
    }
}
