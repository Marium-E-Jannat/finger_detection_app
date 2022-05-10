package com.ubcohci.fingerdetection.detectors;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class to buffer and detect posture/gesture.
 */
public class PostureSeqDetector implements BaseDetector, ValueEventListener {
    private static final String TAG = "GestureDetector";

    // A buffer to check for a gesture
    private final List<String> gestureBuffer = new ArrayList<>();

    // Max value of tolerant count
    private int maxTolerance;

    // A tolerance count
    private int toleranceCount = 0;

    // Ref to database
    private DatabaseReference mDatabase;

    /**
     * Constructor
     */
    public PostureSeqDetector() {
        initialize();
    }

    @Override
    public void initialize() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("maxTolerance").addValueEventListener(this);
    }

    @Override
    public void dispose() {
        mDatabase.child("maxTolerance").removeEventListener(this);
    }

    @SuppressWarnings("unused")
    @Override
    public Map<String, Object> getMotion(@NonNull String posture, @NonNull Map<String, Integer> coordinates) {
        String[] postureSeq;
        if (isPostureExist(posture)) { // If the posture exists
            addToBuffer(posture); // Add the new posture to buffer
            postureSeq = new String[0];
            // Reset tolerant count
            toleranceCount = 0;
        } else { // Server cannot recognize a posture
            // Check tolerance count
            if (toleranceCount < maxTolerance) {
                toleranceCount++;
                postureSeq = new String[0];
            } else {
                // Keep a holder of all postures in buffer
                postureSeq = gestureBuffer.toArray(new String[0]);

                gestureBuffer.clear(); // Clear buffer

                // Reset tolerant count
                toleranceCount = 0;
            }
        }
        Log.d(TAG, "Posture Sequence: " + Arrays.toString(postureSeq));
        Map<String, Object> postureConfig = new HashMap<>();
        postureConfig.put("postures", postureSeq);
        return postureConfig;
    }

    /**
     * Add a posture to an internal buffer.
     * @param posture The posture's class name.
     */
    protected void addToBuffer(String posture) {
        // Check if the last element is the same as posture
        // If so, don't add
        if (gestureBuffer.isEmpty() || !gestureBuffer.get(gestureBuffer.size() - 1).equals(posture)) {
            gestureBuffer.add(posture);
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        maxTolerance = ((Long) Objects.requireNonNull(snapshot.getValue())).intValue();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, error.toString());
    }
}
