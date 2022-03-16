package com.ubcohci.fingerdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.util.Log;

import com.ubcohci.fingerdetection.camera.CameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    // Tags and request codes
    private static final int PERMISSION_REQUESTS = 1;
    private static final String TAG = "MainActivity";

    // View related objects
    private ActivityMainBinding viewBinding;
    private GraphicOverlay graphicOverlay;
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

        // Set listeners
        viewBinding.imageCaptureButton.setOnClickListener(
                view -> {

                }
        );

        viewBinding.videoCaptureButton.setOnClickListener(
                view -> {

        });

        // Check for permissions
        permissionManager = new PermissionManager(TAG,this, PERMISSION_REQUESTS);

        if (permissionManager.isAllPermissionsGranted()) {
            startCamera();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    public void startCamera() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        startCamera();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}