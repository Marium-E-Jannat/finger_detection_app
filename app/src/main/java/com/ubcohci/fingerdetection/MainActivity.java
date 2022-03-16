package com.ubcohci.fingerdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.common.util.concurrent.ListenableFuture;
import com.ubcohci.fingerdetection.camera.CameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, CameraSource.AnalyzerListener  {
    // Tags and request codes
    private static final int PERMISSION_REQUESTS = 1;
    private static final String TAG = "MainActivity";

    // View related objects
    public ActivityMainBinding viewBinding;
    public GraphicOverlay graphicOverlay;

    // Camera configuration
    private CameraSource cameraSource;

    // Permission manager
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());


        // Get the graphic overlay
        graphicOverlay = viewBinding.graphicOverlay;
        cameraSource = new CameraSource(TAG, graphicOverlay, this, this);

        // Check for permissions
        permissionManager = new PermissionManager(TAG,this, PERMISSION_REQUESTS);

        if (permissionManager.isAllPermissionsGranted()) {
                cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUESTS) {
            if (permissionManager.isAllPermissionsGranted()) {
                Log.i(TAG, "Permission Granted");
            } else {
                Log.i(TAG, "Permissions Not Granted. Trying again...");
                permissionManager.getRuntimePermissions();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void handle(@NonNull ImageProxy image) {
        image.getPlanes()[0].getBuffer();

        Log.d(TAG, String.format("Height: %d\tWidth:%d", image.getHeight(), image.getWidth()));
        image.close();
    }
}