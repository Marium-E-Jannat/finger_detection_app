package com.ubcohci.fingerdetection.application;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.camera.MultiCameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;
import com.ubcohci.fingerdetection.detectors.PostureSeqDetector;
import com.ubcohci.fingerdetection.tasks.TaskExecutor;
import com.ubcohci.fingerdetection.tasks.TaskManager;
import com.ubcohci.fingerdetection.tasks.TaskResource;
import com.ubcohci.fingerdetection.tasks.VideoControlTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VideoControlActivityV2 extends BaseActivity implements YouTubePlayer.OnInitializedListener {

    // TAGS
    private static final String TAG = "VideoControlActivityV2";

    // View binding
    public ActivityVideoControlBinding viewBinding;

    // Video player
    private YouTubePlayer player;

    // A gesture detector
    private PostureSeqDetector frontPostureSeqDetector, backPostureSeqDetector;

    // Task manager
    private VideoControlTaskManager videoControlTaskManager;

    // Use multi-camera video instead
    protected MultiCameraSource multiCameraSource;

    // Secondary owner
    public LifecycleOwner secondaryLifeCycleOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.videoGraphicOverlay;

        secondaryLifeCycleOwner = new SecondaryCameraLifeCycle();

        // Init camera source
        singleCameraSource = null;
        multiCameraSource = new MultiCameraSource(TAG, this, this, secondaryLifeCycleOwner,this,this);

        // Init a gesture detector
        frontPostureSeqDetector = new PostureSeqDetector();
        backPostureSeqDetector = new PostureSeqDetector();

        // Init a task manager
        videoControlTaskManager = (VideoControlTaskManager) VideoControlTaskManager.getInstance(VideoControlTaskManager.ControlVersion.V2);
        videoControlTaskManager.init();


        // Get the max volume of music stream
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        TaskResource.maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // Init youtube API
        initYoutubeAPI();

        // Start camera
        if (permissionManager.isAllPermissionsGranted()) {
            multiCameraSource.startCamera();
            ((SecondaryCameraLifeCycle) this.secondaryLifeCycleOwner).enableAlternating();
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
        ((SecondaryCameraLifeCycle) secondaryLifeCycleOwner).setStop();
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

        // Get the camera id
        CameraSelector cameraSelector = (CameraSelector) data.get("camera_selector");

        // Get class name
        String className = data.getString("class_name");

        // Extract bounding box of the posture to detect gesture
        Map<String, Integer> coordinates = new HashMap<>();
        coordinates.put("top", data.getInt("y_max"));
        coordinates.put("bottom", data.getInt("y_min"));
        coordinates.put("left", data.getInt("x_min"));
        coordinates.put("right", data.getInt("x_max"));

        Map<String, Object> postureConfig = (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)?
                backPostureSeqDetector.getMotion(className, coordinates): frontPostureSeqDetector.getMotion(className, coordinates);

        // Get the motion task
        Map<String, Object> motionTask = videoControlTaskManager.getTask(postureConfig);

        // Get the task
        TaskManager.MotionTask task = (TaskManager.MotionTask) Objects.requireNonNull(motionTask.get("task"));

        // Get the task to perform based on posture
        switch (task) {
            case SWITCH_VOLUME:
                TaskExecutor.switchVolume(this, (Integer) Objects.requireNonNull(motionTask.get("volume")));
                break;
            case SWITCH_BRIGHTNESS:
                TaskExecutor.switchBrightness(this, (Integer) Objects.requireNonNull(motionTask.get("brightness")));
                break;
            case SWITCH_VIDEO:
                TaskExecutor.switchVideo(this.player, (String) Objects.requireNonNull(motionTask.get("url")));
                break;
            case SWITCH_VIDEO_FRAME:
                // TODO: Call switch video frame
                break;
            default:
        }
    }
}
