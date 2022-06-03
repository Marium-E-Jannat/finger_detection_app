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

public class SingleCameraSource implements CameraSource{
    // TAGs
    private final String TAG;

    // Camera settings
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // Owner
    private final Context context;
    private final AnalyzerListener imageHandler;

    private final CameraSelector cameraSelector;

    /**
     * Constructor
     * @param tag TAG of the owner.
     * @param context The owner (must be instance of LifecycleOwner)
     */
    public SingleCameraSource(String tag, Context context, AnalyzerListener imageHandler) {
        this.TAG = "SingleCameraSource_" + tag;
        this.context = context;
        this.imageHandler = imageHandler;
        this.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
    }

    public SingleCameraSource(CameraSelector cameraSelector, String tag, Context context, AnalyzerListener imageHandler) {
        this.TAG = "SingleCameraSource_" + tag;
        this.context = context;
        this.imageHandler = imageHandler;
        this.cameraSelector = cameraSelector;
    }

    /**
     * Start camera.
     * Currently only supports MainActivity.
     */
    @Override
    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.context);
        cameraProviderFuture.addListener(
                () -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                        // Preview
                        Preview preview =  null;
                        Preview.SurfaceProvider[] surfaceProviders = getSurfaceProvider(this.context); // Array of all surface providers
                        if (surfaceProviders != null) {
                            preview = new Preview.Builder().build();
                            preview.setSurfaceProvider(surfaceProviders[0]);
                        }

                        // Camera selector
                        CameraSelector selector = this.cameraSelector;

                        // Analyser
                        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                        imageAnalysis.setAnalyzer(
                                ActivityCompat.getMainExecutor(this.context),
                                new Analyzer(imageHandler, selector)
                        );

                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases
                        if (preview == null) {
                            cameraProvider.bindToLifecycle(
                                    (LifecycleOwner) this.context,
                                    selector,
                                    imageAnalysis
                            );
                        } else {
                            cameraProvider.bindToLifecycle(
                                    (LifecycleOwner) this.context,
                                    selector,
                                    preview,
                                    imageAnalysis
                            );
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                },
                ActivityCompat.getMainExecutor(this.context) // Running on the main thread
        );
    }

    @Override
    public void releaseCamera() { }
}