package com.ubcohci.fingerdetection.application;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.GestureDetector;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoControlActivity extends BaseActivity implements YouTubePlayer.OnInitializedListener {

    // TAGS
    private static final String TAG = "VideoControlActivity";

    // View binding
    public ActivityVideoControlBinding viewBinding;

    // Video player
    private YouTubePlayer player;

    // A tracker of the current volume level
    private int currentLevel = -1;

    // A gesture detector
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.videoGraphicOverlay;

        // Init a gesture detector
        gestureDetector = new GestureDetector();

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

        // Get the task to perform based on posture
        switch (gestureDetector.getPostureTask(className)) {
            case SWITCH_VOLUME:
                Integer volumeLevel = gestureDetector.findVolumeLevel(className);
                // If the posture is not found or the same posture is detected
                if (volumeLevel == null || currentLevel == volumeLevel) {
                    Log.d(TAG, "Volume remains the same!");
                    return;
                }
                switchVolume(volumeLevel);
                this.currentLevel = volumeLevel;
                break;
            case SWITCH_VIDEO:
                switchVideo(gestureDetector.getAnotherHash(className));
                break;
            default: // Do nothing if there is no posture detected
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

    public void switchVideo(String newHash) {
        this.player.loadVideo(newHash);
    }
}
