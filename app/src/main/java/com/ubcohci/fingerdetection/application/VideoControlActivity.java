package com.ubcohci.fingerdetection.application;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.youtube.player.YouTubePlayerView;
import com.ubcohci.fingerdetection.databinding.ActivityVideoControlBinding;

public class VideoControlActivity extends AppCompatActivity {

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

    }

}
