package com.ubcohci.fingerdetection.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.Log;

import java.util.Locale;

public class DetectionGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = "DetectionGraphic";
    public static class DetectionInfo {
        public String className;
        public String classId;
        public DetectionInfo(String className, String classId) {
            this.className = className;
            this.classId = classId;
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

        // Get canvas width
        int width = canvas.getWidth();

        // Set paint
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);

        // Draw background
        canvas.drawRect(
                new Rect(width * 2 / 3, 10, width - 10,
                        (int) DrawingUtils.convertFromDpToPx(100f, context.getResources().getDisplayMetrics())),
                backgroundPaint
        );

        // Draw text
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(DrawingUtils.convertFromSpToPx(24f, context.getResources().getDisplayMetrics()));
        canvas.drawText(
                String.format(Locale.CANADA, "Name: %s\nID: %s", info.className, info.classId),
                width * 2 / 3f + 10 ,
                DrawingUtils.convertFromDpToPx(50f, context.getResources().getDisplayMetrics()),
                textPaint
        );
    }
}
