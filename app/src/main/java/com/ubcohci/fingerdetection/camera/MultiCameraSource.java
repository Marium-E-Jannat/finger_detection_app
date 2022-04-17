package com.ubcohci.fingerdetection.camera;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

public class MultiCameraSource implements CameraSource {
    // Log tag
    private final String TAG;

    // Context
    private final Context context;

    // Lifecycle Owners
    private final LifecycleOwner backCameraLifeCycleOwner, frontCameraLifeCycleOwner;

    // Listeners
    private final AnalyzerListener frontListener;
    private final AnalyzerListener backListener;

    // Camera provider
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;

    /**
     * Constructor
     * @param tag TAG of the owner.
     * @param context The owner (must be instance of LifecycleOwner)
     * @param frontCameraLifeCycleOwner The lifecycle owner for the front camera
     * @param backCameraLifeCycleOwner The lifecycle owner for the back camera
     * @param frontListener The image listener for front camera
     * @param backListener The image listener for back camera
     */
    public MultiCameraSource(
            String tag, Context context,
            LifecycleOwner frontCameraLifeCycleOwner,
            LifecycleOwner backCameraLifeCycleOwner,
            AnalyzerListener frontListener,
            AnalyzerListener backListener ) {
        TAG = "MultiCameraSource_" + tag;
        this.context = context;
        this.backCameraLifeCycleOwner = backCameraLifeCycleOwner;
        this.frontCameraLifeCycleOwner = frontCameraLifeCycleOwner;
        this.frontListener = frontListener;
        this.backListener = backListener;
    }


    @Override
    public void startCamera() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this.context);
        cameraProviderListenableFuture.addListener(
                () -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();

                        // Unbind all cases
                        cameraProvider.unbindAll();

                        CameraSelector frontSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                        // Set up for front camera analyzer
                        ImageAnalysis frontAnalysis = new ImageAnalysis.Builder().build();
                        frontAnalysis.setAnalyzer(
                                ActivityCompat.getMainExecutor(this.context),
                                new Analyzer(frontListener, frontSelector)
                        );

                        CameraSelector backSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                        // Set up for back camera analyzer
                        ImageAnalysis backAnalysis = new ImageAnalysis.Builder().build();
                        backAnalysis.setAnalyzer(
                                ActivityCompat.getMainExecutor(this.context),
                                new Analyzer(backListener, backSelector)
                        );

                        // Get surface providers
                        Preview.SurfaceProvider[] surfaceProviders = getSurfaceProvider(this.context);

                        if (surfaceProviders != null) {
                            // Bind cases with preview
                            Preview frontPreview = new Preview.Builder().build();
                            frontPreview.setSurfaceProvider(surfaceProviders[0]);
                            cameraProvider.bindToLifecycle(
                                    this.frontCameraLifeCycleOwner,
                                    frontSelector,
                                    frontPreview,
                                    frontAnalysis
                            );

                            Preview backPreview = new Preview.Builder().build();
                            backPreview.setSurfaceProvider(surfaceProviders[1]);
                            cameraProvider.bindToLifecycle(
                                    this.backCameraLifeCycleOwner,
                                    backSelector,
                                    backPreview,
                                    backAnalysis
                            );
                        } else {
                            // Bind cases
                            cameraProvider.bindToLifecycle(
                                    this.frontCameraLifeCycleOwner,
                                    frontSelector,
                                    frontAnalysis
                            );

                            cameraProvider.bindToLifecycle(
                                    this.backCameraLifeCycleOwner,
                                    backSelector,
                                    backAnalysis
                            );
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                },
                ActivityCompat.getMainExecutor(this.context)
        );
    }

    @Override
    public void releaseCamera() {

    }
}
