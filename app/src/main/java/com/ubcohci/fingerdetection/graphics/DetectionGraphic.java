package com.ubcohci.fingerdetection.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.Log;

import androidx.camera.core.CameraSelector;

import java.util.Locale;

public class DetectionGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = "DetectionGraphic";
    public static class DetectionInfo {
        public String className;
        public String classId;
        public String cameraDirection;
        public DetectionInfo(String className, String classId, CameraSelector cameraSelector) {
            this.className = className;
            this.classId = classId;
            this.cameraDirection = cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA? "BACK": "FRONT";
        }
    }

    private final DetectionInfo info;
    private final Context context;

    public DetectionGraphic(GraphicOverlay graphicOverlay, DetectionInfo info) {
        super(graphicOverlay);
        this.info = info;
        this.context = getApplicationContext();
    }

    /**
     *
     * @param canvas drawing canvas
     */
    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, String.format(Locale.CANADA, "name: %s\tid: %s", info.className, info.classId));
        DrawingUtils.drawModelResult(canvas, context.getResources().getDisplayMetrics(), info);
    }
}
