package com.ubcohci.fingerdetection;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PermissionManager {
    private int requestCode;
    private final String TAG;
    private final Context context;

    public PermissionManager(String tag, Context context, int requestCode) {
        this.requestCode = requestCode;
        this.context = context;
        this.TAG = tag + "_PermissionManager";
        Log.i(TAG, "Initialized");
    }

    public final void getRuntimePermissions() {
        if (context instanceof  ActivityCompat.OnRequestPermissionsResultCallback) {
            throw new RuntimeException(String.format(
                    Locale.CANADA,
                    "%s must extends ActivityCompat.OnRequestPermissionsResultCallback.", context.getClass().getSimpleName()));
        }

        List<String> neededPermissions = new ArrayList<>();
        for (String ps: getRequiredPermissions()) {
            if (!isPermissionGranted(ps)) {
                neededPermissions.add(ps);
            }
        }

        if (neededPermissions.size() > 0) {
            ActivityCompat.requestPermissions((Activity) this.context, neededPermissions.toArray(new String[0]), requestCode);
        }
    }

    public final String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.context.getPackageManager()
                            .getPackageInfo(this.context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    public boolean isPermissionGranted(String permission) {
        if (ContextCompat.checkSelfPermission(this.context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission not granted: " + permission);
        return false;
    }

    public final boolean isAllPermissionsGranted() {
        for (String ps: getRequiredPermissions()) {
            if (!isPermissionGranted(ps)) {
                return false;
            }
        }
        return true;
    }
}
