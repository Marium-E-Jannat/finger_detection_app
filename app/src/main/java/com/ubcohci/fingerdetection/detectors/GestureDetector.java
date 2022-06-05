package com.ubcohci.fingerdetection.detectors;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GestureDetector implements BaseDetector, ValueEventListener {
    // TAG
    private static final String TAG = "GestureDetector";

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        SKIP
    }

    // Keep a record of the previous coordinates
    private Map<String, Integer> previousCoordinates = null;

    // Keep a buffer of postures
    private final String[] postureBuffer = new String[1]; // Only need 1 slot

    // Define threshold (retrieve from firebase)
    private int pixelThreshold;

    // Database Ref
    private DatabaseReference mDatabase;

    @Override
    public Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates) {
        Map<String, Object> postureConfig = new HashMap<>();

        if (postureBuffer[0] == null) { // Add and wait if starting gesture
            postureBuffer[0] = posture;
            previousCoordinates = coordinates;
            postureConfig.put("postures", new String[0]);
        } else if (postureBuffer[0].equals(posture)) {
            // Get the posture
            postureConfig.put("postures", postureBuffer.clone());

            Direction verticalDirection = Direction.SKIP, horizontalDirection = Direction.SKIP;

            // Check x-axis
            int oldLeft = Objects.requireNonNull(previousCoordinates.get("left"));
            int newLeft = Objects.requireNonNull(coordinates.get("left"));

            if (Math.abs(newLeft - oldLeft) > pixelThreshold) {
                horizontalDirection = newLeft > oldLeft? Direction.RIGHT: Direction.LEFT;
            }

            // Check y-axis
            int oldTop = Objects.requireNonNull(previousCoordinates.get("top"));
            int newTop = Objects.requireNonNull(coordinates.get("top"));

            if (Math.abs(newTop - oldTop) > pixelThreshold) {
                verticalDirection = newTop > oldTop? Direction.UP: Direction.DOWN;
            }

            postureConfig.put("horizontal_direction", horizontalDirection);
            postureConfig.put("vertical_direction", verticalDirection);
            previousCoordinates = coordinates;
        } else { // No match, reset buffer
            postureBuffer[0] = null;
            previousCoordinates = null;
            postureConfig.put("postures", new String[0]);
        }

        return postureConfig;
    }

    @Override
    public void initialize() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("pixelThreshold").addValueEventListener(this);
    }

    @Override
    public void dispose() {
        mDatabase.child("pixelThreshold").removeEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        pixelThreshold = ((Long) Objects.requireNonNull(snapshot.getValue())).intValue();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, error.toString());
    }
}
