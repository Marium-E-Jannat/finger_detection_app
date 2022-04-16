package com.ubcohci.fingerdetection.camera;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;

import com.ubcohci.fingerdetection.MainActivity;

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
     * @return The SurfaceProvider if there is any else null.
     */
   default Preview.SurfaceProvider getSurfaceProvider(Context context) {
        Preview.SurfaceProvider surfaceProvider = null;
        if (context instanceof MainActivity) {
            surfaceProvider = ((MainActivity) context).viewBinding.viewFinder.getSurfaceProvider();
        }
        return surfaceProvider;
   }


    /**
     * Analyzer to receive image frames.
     */
    final class Analyzer implements ImageAnalysis.Analyzer {
        private final AnalyzerListener listener;
        public Analyzer(AnalyzerListener listener) {
            this.listener = listener;
        }
        @Override
        public void analyze(@NonNull ImageProxy image) {
            listener.handle(image); // Run a separate thread pool (cameraExecutor)
        }
    }

    interface AnalyzerListener {
        void handle(@NonNull ImageProxy image);
    }
}
