package com.ubcohci.fingerdetection.camera;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;

import com.ubcohci.fingerdetection.MainActivity;
import com.ubcohci.fingerdetection.MainActivityV2;

public interface CameraSource {
    /**
     * Start the camera.
     */
    void startCamera();

    /**
     * Close the camera and release held resources.
     */
    void releaseCamera();

    /**
     * Get SurfaceProvider from activity view binding.
     * @param context The owner of the current camera source instance.
     * @return An array of SurfaceProviders, where first element is front camera and second is back camera (if any).
     */
   default Preview.SurfaceProvider[] getSurfaceProvider(@NonNull Context context) {
        Preview.SurfaceProvider[] surfaceProvider = null;
        if (context instanceof MainActivity) {
            surfaceProvider = new Preview.SurfaceProvider[] {
                    ((MainActivity) context).viewBinding.viewFinder.getSurfaceProvider()
            };
        } else if (context instanceof MainActivityV2) {
            surfaceProvider = new Preview.SurfaceProvider[] {
                    ((MainActivityV2) context).viewBinding.viewFinderFront.getSurfaceProvider(),
                    ((MainActivityV2) context).viewBinding.viewFinderBack.getSurfaceProvider()
            };
        }
        return surfaceProvider;
   }


    /**
     * Analyzer to receive image frames.
     */
    final class Analyzer implements ImageAnalysis.Analyzer {
        private final AnalyzerListener listener;
        private final CameraSelector cameraSelector;
        public Analyzer(@NonNull AnalyzerListener listener, @NonNull CameraSelector cameraSelector) {
            this.listener = listener;
            this.cameraSelector = cameraSelector;
        }
        @Override
        public void analyze(@NonNull ImageProxy image) {
            listener.handle(image, cameraSelector); // Run a separate thread pool (cameraExecutor)
        }
    }

    interface AnalyzerListener {
        void handle(@NonNull ImageProxy image, @NonNull CameraSelector cameraSelector);
    }
}
