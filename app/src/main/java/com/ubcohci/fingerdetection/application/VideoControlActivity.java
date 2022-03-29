package com.ubcohci.fingerdetection.application;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragmentX;
import com.google.android.youtube.player.YouTubePlayerView;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VideoControlActivity extends BaseActivity implements YouTubePlayer.OnInitializedListener {

    // TAGS
    private static final String TAG = "VideoControlActivity";

    // View binding
    public ActivityVideoControlBinding viewBinding;

    // Video player
    private YouTubePlayer player;

    // A mapping from detection class to volume level
    private static final Map<String, Integer> postureToVolume = new HashMap<>();

    // A tracker of the current volume level
    private int currentLevel = -1;

    static  {
        //Straight index finger to set the sound at 10x
        postureToVolume.put("straight_index", 10);
        //Straight two fingers to set the sound at 20x
        postureToVolume.put("straight_two", 20);
        //Straight all fingers to set the sound at 30x
        postureToVolume.put("straight_all", 30);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.videoGraphicOverlay;

        // Initialize youtube API
        YouTubePlayerSupportFragmentX youtubeFragment = ((YouTubePlayerSupportFragmentX) getSupportFragmentManager().findFragmentById(viewBinding.ytPlayer.getId()));
        if (youtubeFragment != null) {
            youtubeFragment.initialize(BuildConfig.YOUTUBE_API_KEY, this);
        } else {
            Log.d(TAG, "No youtube fragment available!");
            this.finish();
        }

        // Start camera
        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
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
        if (this.player == null) { // Skip and wait till youtube API is initialized
            return;
        }

        super.handleAppTask(data);
        // Get class name
        Integer volumeLevel = postureToVolume.get(data.getString("class_name"));

        if (volumeLevel == null || currentLevel == volumeLevel) {
            Log.d(TAG, "Volume remains the same!");
            return;
        }

        this.currentLevel = volumeLevel;

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
}
