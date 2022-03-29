package com.ubcohci.fingerdetection.camera;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.ubcohci.fingerdetection.MainActivity;
import com.ubcohci.fingerdetection.application.VideoControlActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraSource {
    // TAGs
    private final String TAG;

    // Camera settings
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // Owner
    private final Context context;
    private final AnalyzerListener imageHandler;

    /**
     * Constructor
     * @param tag TAG of the owner.
     * @param context The owner (must be instance of LifecycleOwner)
     */
    public CameraSource(String tag, Context context, AnalyzerListener imageHandler) {
        this.TAG = "CameraSource_" + tag;
        this.context = context;
        this.imageHandler = imageHandler;
    }

    /**
     * Start camera.
     * Currently only supports MainActivity.
     */
    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.context);
        cameraProviderFuture.addListener(
                () -> {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                        // Preview
                        Preview preview =  null;
                        Preview.SurfaceProvider surfaceProvider = getSurfaceProvider(this.context);
                        if (surfaceProvider != null) {
                            preview = new Preview.Builder().build();
                            preview.setSurfaceProvider(surfaceProvider);
                        }


                        // Camera selector
                        CameraSelector selector = CameraSelector.DEFAULT_FRONT_CAMERA;

                        // Analyser
                        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
                        imageAnalysis.setAnalyzer(
                                ActivityCompat.getMainExecutor(this.context),
                                new Analyzer(imageHandler)
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

    public void release() { }

    private Preview.SurfaceProvider getSurfaceProvider(Context context) {
        Preview.SurfaceProvider surfaceProvider = null;
        if (context instanceof MainActivity) {
            surfaceProvider = ((MainActivity) context).viewBinding.viewFinder.getSurfaceProvider();
        }
        return surfaceProvider;
    }

    public interface AnalyzerListener {
        void handle(@NonNull ImageProxy image);
    }

    /**
     * Analyzer to receive image frames.
     */
    private static final class Analyzer implements ImageAnalysis.Analyzer {
        private final AnalyzerListener listener;
        public Analyzer(AnalyzerListener listener) {
            this.listener = listener;
        }
        @Override
        public void analyze(@NonNull ImageProxy image) {
            listener.handle(image); // Run a separate thread pool (cameraExecutor)
        }
    }
}