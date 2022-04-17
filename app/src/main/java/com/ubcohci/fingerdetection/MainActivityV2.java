package com.ubcohci.fingerdetection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.ubcohci.fingerdetection.application.BaseActivity;
import com.ubcohci.fingerdetection.camera.MultiCameraSource;
import com.ubcohci.fingerdetection.databinding.ActivityMainv2Binding;

public class MainActivityV2 extends BaseActivity {
    // Log tag
    private static final String TAG = "MainActivityV2";

    public ActivityMainv2Binding viewBinding;

    public MultiCameraSource cameraSource;

    public LifecycleOwner secondaryLifeCycleOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get view binding
        viewBinding = ActivityMainv2Binding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set graphic overlay
        this.graphicOverlay = viewBinding.graphicOverlayV2;

        // Create a secondary lifecycle owner
        secondaryLifeCycleOwner = new SecondaryCameraLifeCycle();

        // Set camera
        this.singleCameraSource = null;
        this.cameraSource = new MultiCameraSource(TAG, this, this, secondaryLifeCycleOwner, this, this);

        if (permissionManager.isAllPermissionsGranted()) {
            cameraSource.startCamera();
            ((SecondaryCameraLifeCycle) this.secondaryLifeCycleOwner).enableAlternating();
        } else {
            permissionManager.getRuntimePermissions();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((SecondaryCameraLifeCycle) secondaryLifeCycleOwner).setStop();
    }
}
