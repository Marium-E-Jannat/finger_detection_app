package com.ubcohci.fingerdetection.application;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;

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

    // A mapping from detection class to volume level
    private static final Map<String, Integer> postureToVolume = new HashMap<>();

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
        // Get view binding
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());

        // Init graphic overlay
        graphicOverlay = viewBinding.videoGraphicOverlay;

        // Initialize youtube player
        viewBinding.ytPlayer.initialize(
                BuildConfig.YOUTUBE_API_KEY,
                this
        );
        super.onCreate(savedInstanceState);
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
    }

    @Override
    protected void handleAppTask(JSONObject data) throws JSONException {
        super.handleAppTask(data);
        // Get class name
        int volumeLevel = Objects.requireNonNull(postureToVolume.get(data.getString("class_name")));
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
