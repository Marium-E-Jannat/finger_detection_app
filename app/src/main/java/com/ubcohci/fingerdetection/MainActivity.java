package com.ubcohci.fingerdetection;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;

import com.ubcohci.fingerdetection.camera.CameraSource;
import com.ubcohci.fingerdetection.camera.CameraUtils;
import com.ubcohci.fingerdetection.databinding.ActivityMainBinding;
import com.ubcohci.fingerdetection.graphics.GraphicOverlay;
import com.ubcohci.fingerdetection.network.HttpClient;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        CameraSource.AnalyzerListener,
        HttpClient.ResultHandler {
    // Tags and request codes
    private static final int PERMISSION_REQUESTS = 1;
    private static final String TAG = "MainActivity";
    private static  final String URL = "https://0722-24-71-238-132.ngrok.io" + "/process";

    // View related objects
    public ActivityMainBinding viewBinding;
    public GraphicOverlay graphicOverlay;

    // Camera configuration
    private CameraSource cameraSource;

    // Permission manager
    private PermissionManager permissionManager;

    // Http client
    private HttpClient httpClient;

    // Save the image for closing
    private ImageProxy image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());


        // Get the graphic overlay
        graphicOverlay = viewBinding.graphicOverlay;
        cameraSource = new CameraSource(TAG, graphicOverlay, this, this);

        // Check for permissions
        permissionManager = new PermissionManager(TAG,this, PERMISSION_REQUESTS);

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

    @Override
    protected void onDestroy() {
        this.httpClient.dispose();
        this.cameraSource.release();
        super.onDestroy();
    }

    @Override
    public void handle(@NonNull ImageProxy image) {
        // Save image
        this.image = image;

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
            image.close();
        }
    }

    @Override
    public void onResult(Map<String, Object> result) {
        image.close();
        Log.d(TAG, "Data: " + Objects.requireNonNull(result.get("data")));
    }

    @Override
    public void onFailure(Exception e) {
        image.close();
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