package com.ubcohci.fingerdetection;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ubcohci.fingerdetection.camera.CameraSource;
import com.ubcohci.fingerdetection.camera.CameraUtils;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;
import com.ubcohci.fingerdetection.graphics.DetectionGraphic;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;
import com.ubcohci.fingerdetection.network.HttpClient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        CameraSource.AnalyzerListener,
        HttpClient.ResultHandler,
        ValueEventListener {
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

    // Http client
    private HttpClient httpClient;
    private String URL;
    private static final String suffix = "/process";

    // Firebase Database reference
    private DatabaseReference mDatabase;

    // Timer
    private long startTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Get the graphic overlay
        graphicOverlay = viewBinding.graphicOverlay;
        cameraSource = new CameraSource(TAG,this, this);

        // Check for permissions
        permissionManager = new PermissionManager(TAG,this, PERMISSION_REQUESTS);

        // Get a reference to database root
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initializeServerURL();  // Get the URL to the Google Colab server

        // Http client
        try {
            httpClient = new HttpClient(this);
        } catch (Exception e) {
            Toast.makeText(this, "Http connection is not initialized", Toast.LENGTH_SHORT).show();
        }

        if (permissionManager.isAllPermissionsGranted()) {
                cameraSource.startCamera();
        } else {
            permissionManager.getRuntimePermissions();
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

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object obj = snapshot.getValue();

        if (obj instanceof String) {
            this.URL = snapshot.getValue() + suffix;
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
    }

    @Override
    public void handle(@NonNull ImageProxy image) {
        // Checking timing && url availability
        if ((startTime > 0 && (System.currentTimeMillis() - startTime) / 1000 < 1) || URL == null) {
            Log.d(TAG, "Timer does not expire yet!");
            image.close();
            return;
        }

        startTime = System.currentTimeMillis(); // Set new start time

        try {
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
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        image.close(); // Close the image
    }

    @Override
    public void onResult(Map<String, Object> result) {
        JSONObject jsonObject = (JSONObject) Objects.requireNonNull(result.get("data"));
        Log.d(TAG, "Data: " + jsonObject);
        // Extract the information
        try {
            DetectionGraphic.DetectionInfo info = new DetectionGraphic.DetectionInfo(
                    jsonObject.getString("class_name"),
                    String.valueOf(jsonObject.getInt("class_id"))
            );

            // Draw new overlays with new model results
            graphicOverlay.clear();
            graphicOverlay.add(new DetectionGraphic(graphicOverlay, info));
            graphicOverlay.postInvalidate();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onFailure(Exception e) {
        graphicOverlay.clear();
        graphicOverlay.postInvalidate();
        Log.d(TAG, e.getMessage());
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