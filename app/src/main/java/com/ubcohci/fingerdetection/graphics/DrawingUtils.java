package com.ubcohci.fingerdetection.graphics;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DrawingUtils {
    public static float convertFromDpToPx(float sizeInDp, DisplayMetrics metrics) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                sizeInDp,
                metrics
        );
    }

    public static float convertFromSpToPx(float sizeInSp, DisplayMetrics metrics) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sizeInSp,
                metrics
        );
    }
}
