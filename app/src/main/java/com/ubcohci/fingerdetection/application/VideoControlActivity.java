package com.ubcohci.fingerdetection.application;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.ubcohci.fingerdetection.BuildConfig;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;

public class VideoControlActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {

    // TAGS
    private static final String TAG = "VideoControlActivity";

    // View binding
    ActivityVideoControlBinding viewBinding;

    // Video player
    private YouTubePlayerView youTubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get view binding
        viewBinding = ActivityVideoControlBinding.inflate(getLayoutInflater());

        // Get the video player
        youTubePlayerView = viewBinding.ytPlayer;

        // Initialize youtube player
        youTubePlayerView.initialize(
                BuildConfig.YOUTUBE_API_KEY,
                this
        );
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        Log.d(TAG, "Video player is initialized!");
        youTubePlayer.loadVideo("HzeK7g8cD0Y");
        youTubePlayer.play();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Log.d(TAG, "Video player failed to be initialized!");
    }
}
