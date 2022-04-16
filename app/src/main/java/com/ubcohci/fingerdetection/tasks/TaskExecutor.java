package com.ubcohci.fingerdetection.tasks;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.youtube.player.YouTubePlayer;

public class TaskExecutor {
    private static final String TAG = "TaskExecutor";

    public static void switchVolume(Activity context, int volumeLevel) {
        Log.d(TAG, "Setting volume to " + volumeLevel);

        // Use audio service to change volume
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isVolumeFixed()) {
            Toast.makeText(context, "Volume is in fixed mode!", Toast.LENGTH_SHORT).show();
        } else {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    volumeLevel,
                    AudioManager.FLAG_SHOW_UI
            );
        }
    }

    public static void switchVideo(@NonNull YouTubePlayer player, @NonNull String newHash) {
        player.loadVideo(newHash);
    }

    public static void switchBrightness(Activity context, int brightness) {
        Log.d(TAG, "Brightness change to: " + brightness);

        // Get the current window
        Window window = context.getWindow();

        // Set current screen brightness
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness / 100f;
        window.setAttributes(layoutParams);
    }

    public static void advanceVideoFrame(@NonNull YouTubePlayer player, boolean forward) {
        player.seekRelativeMillis(forward? 2000: -2000);
    }
}
