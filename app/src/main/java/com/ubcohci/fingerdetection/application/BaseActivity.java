package com.ubcohci.fingerdetection.application;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubcohci.fingerdetection.InferenceTracker;
import com.ubcohci.fingerdetection.PermissionManager;
import com.ubcohci.fingerdetection.camera.SingleCameraSource;
import com.ubcohci.fingerdetection.camera.CameraUtils;
import com.ubcohci.fingerdetection.graphics.DetectionGraphic;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;
import com.ubcohci.fingerdetection.graphics.InferenceGraphic;
import com.ubcohci.fingerdetection.network.HttpClient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        SingleCameraSource.AnalyzerListener,
        HttpClient.ResultHandler,
        ValueEventListener {
    // Tags and request codes
    protected static final int PERMISSION_REQUESTS = 1;
    protected static final String TAG = "BaseActivity";

    protected GraphicOverlay graphicOverlay;

    // Camera configuration
    protected SingleCameraSource singleCameraSource;

    // Permission manager
    protected PermissionManager permissionManager;

    // Http client
    protected HttpClient httpClient;
    protected String URL;
    protected static final String suffix = "/process";

    // Firebase Database reference
    protected DatabaseReference mDatabase;

    // Reference to the current frame
    protected ImageProxy currentFrame;

    // A latency tracker
    protected InferenceTracker inferenceTracker;

    // Activity bundle data
    public boolean showDebug;

    // Add a tracker from last time an image is sent
    private long lastSentTimeMarker;

    protected int timeOut = 1000; // Default 1000 ms

    // Keep a holder of current camera selector
    protected CameraSelector cameraSelector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showDebug = getIntent().getBooleanExtra("showDebug", false);

        singleCameraSource = new SingleCameraSource(TAG,this, this);

        // Check for permissions
        permissionManager = new PermissionManager(TAG,this, PERMISSION_REQUESTS);

        // Get the inference tracker
        inferenceTracker = new InferenceTracker();

        // Get a reference to database root
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeServerURL();  // Get the URL to the Google Colab server

        // Http client
        try {
            httpClient = new HttpClient(this);
        } catch (Exception e) {
            Toast.makeText(this, "Http connection is not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    public void initializeServerURL() {
        if (mDatabase == null) {
            throw new NullPointerException("Database pointer is null!");
        }
        // Add a persistence listener
        // for the URL field
        mDatabase.child("url").addValueEventListener(this);
    }

    protected void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object obj = snapshot.getValue();

        if (obj instanceof String && HttpClient.isHttps((String) obj)) {
            this.URL = (String)obj + suffix;
        } else {
            Log.d(TAG, "Invalid value at " + snapshot.getRef() + " Value: " + obj);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.d(TAG, error.getMessage());
    }

    @Override
    protected void onDestroy() {
        this.httpClient.dispose();
        mDatabase.child("url").removeEventListener(this);
        super.onDestroy();
        if (singleCameraSource != null) {
            singleCameraSource.releaseCamera();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void handle(@NonNull ImageProxy image, @NonNull CameraSelector cameraSelector) {
        Log.d(TAG, "Getting image from camera " + (Objects.requireNonNull(cameraSelector.getLensFacing()) == 0? "FRONT" : "BACK"));
        this.currentFrame = image;
        try {
            final long now = System.currentTimeMillis();
            if (lastSentTimeMarker > 0 && (now - lastSentTimeMarker) < timeOut) {
                image.close();
                return;
            }

            Log.d(TAG, "Sending image to backend...");
            this.inferenceTracker.setStartTime(); // Set timer
            // Start sending image
            httpClient.start(
                    URL,
                    "POST",
                    new HashMap<String, String>() {{
                        put("Content-Type", "image/jpeg");
                        put("Accept","application/json");
                    }},
                    CameraUtils.imageProxyToByteArray(image)
            );

            lastSentTimeMarker = now;
            this.cameraSelector = cameraSelector;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            image.close(); // Close the image
        }
    }

    @Override
    public void onResult(Map<String, Object> result) {
        inferenceTracker.setStopTime(); // Stop timer
        Log.d(TAG, String.format("Average RTT: %d ms", (int) inferenceTracker.getLatency()));

        // Clear all graphics
        graphicOverlay.clear();

        // Extract the information
        try {
            JSONObject jsonObject = (JSONObject) Objects.requireNonNull(result.get("data"));

            // Get detection information
            DetectionGraphic.DetectionInfo info = new DetectionGraphic.DetectionInfo(
                    jsonObject.getString("class_name"),
                    String.valueOf(jsonObject.getInt("class_id"))
            );

            // Draw new overlays with new model results
            graphicOverlay.add(new DetectionGraphic(graphicOverlay, info));

            // Draw new overlays with latency information
            if (showDebug) {
                graphicOverlay.add(new InferenceGraphic(graphicOverlay, new InferenceGraphic.Inference(
                        this.inferenceTracker.getLatency(), new int[] { this.currentFrame.getWidth(), this.currentFrame.getHeight()}
                )));
            }

            // Add camera selector in
            jsonObject.put("camera_selector", this.cameraSelector);
            // Handle app-specific tasks
            handleAppTask(jsonObject);
        } catch (JSONException | ClassCastException e) {
            Log.d(TAG, e.getMessage());
        }

        // Notify graphicOverlay that it needs to redraw
        graphicOverlay.postInvalidate();

        // Release the current frame
        this.currentFrame.close();
    }

    protected void handleAppTask(JSONObject data) throws JSONException {}

    @Override
    public void onFailure(Exception e) {
        inferenceTracker.setStopTime(); // Stop timer

        Log.d(TAG, "Average RTT: " + inferenceTracker.getLatency());
        Log.d(TAG, e.getMessage());

        graphicOverlay.clear();
        graphicOverlay.postInvalidate();

        this.currentFrame.close();
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