package com.ubcohci.fingerdetection.tasks;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;

import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubePlayer;
import com.ubcohci.fingerdetection.R;

import java.net.URI;

public class TaskExecutor {
    private static final String TAG = "TaskExecutor";
    public static final int ZOOM_IN = 0;
    public static final int ZOOM_OUT = 1;

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

    /**
     *
     * @param context The current app context
     * @param imageView The image view to host the image
     */
    public static void loadImage(@NonNull Context context, @NonNull ImageView imageView) {
        Glide.with(context)
                .load(TaskResource.mockMapImageURL)
                .placeholder(R.drawable.image_place_holder)
                .into(imageView);
    }

    /**
     * @param
     * @param imageView The image view to host the image
     * @param zoomDirection The zoom direction (0 -> ZOOM_IN, 1 -> ZOOM_OUT)
     *
     */
    public static void zoomImage(Context context, @NonNull ImageView imageView, int zoomDirection) {
        Animation animationZoom = AnimationUtils.loadAnimation(context,
                zoomDirection == ZOOM_IN? R.anim.zoom_in: R.anim.zoom_out);
        imageView.startAnimation(animationZoom);
    }
}
