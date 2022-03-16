package com.ubcohci.fingerdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;
import com.ubcohci.fingerdetection.camera.CameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    // Tags and request codes
    private static final int PERMISSION_REQUESTS = 1;
    private static final String TAG = "MainActivity";

    // View related objects
    private ActivityMainBinding viewBinding;
    private GraphicOverlay graphicOverlay;

    // Camera configuration
    private CameraSource cameraSource;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor cameraExecutor;

    // Permission manager
    private PermissionManager permissionManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

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
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(
                () -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                        // Preview
                        Preview preview = new Preview.Builder().build();
                        preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                        // Camera selector
                        CameraSelector selector = CameraSelector.DEFAULT_FRONT_CAMERA;

                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases
                        cameraProvider.bindToLifecycle(
                                this,
                                selector,
                                preview
                        );
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                },
                ActivityCompat.getMainExecutor(this) // Running on the main thread
        );
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