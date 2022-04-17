package com.ubcohci.fingerdetection.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.NonNull;

import java.util.Locale;

public class DrawingUtils {

    public static Paint backgroundPaint;
    public static Paint textPaint;

    static {
        // Set paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);


        // Draw text
        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
    }
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


    public static void drawModelResult(@NonNull Canvas canvas, @NonNull  DisplayMetrics displayMetrics, DetectionGraphic.DetectionInfo info) {

        // Set text size here since we don't have access to display_metrics at compile time.
        textPaint.setTextSize(DrawingUtils.convertFromSpToPx(18f, displayMetrics));

        // Get canvas width
        int width = canvas.getWidth();

        // Define margins and paddings
        float margin = convertFromDpToPx(10f, displayMetrics); // 10dp
        float padding = convertFromDpToPx(5, displayMetrics); // 5dp

        // Define rectangle size
        float rectWidth = width / 2f;
        float rectHeight = (textPaint.getTextSize() + padding) * 3 + padding;


        // Draw background
        canvas.drawRect(
                new Rect(width - (int)rectWidth - (int) margin,
                        (int) margin,
                        width - (int) margin,
                        (int) (margin + rectHeight)),
                backgroundPaint
        );

        // Draw class Name
        canvas.drawText(
                String.format(Locale.CANADA, "Name: %s", info.className),
                width - rectWidth -  margin + padding,
                margin + padding + textPaint.getTextSize(),
                textPaint
        );
        canvas.save();
        canvas.translate(0, margin + padding + textPaint.getTextSize());

        // Draw id name
        canvas.drawText(
                String.format(Locale.CANADA, "ID: %s", info.classId),
                width - rectWidth -  margin + padding,
                padding + textPaint.getTextSize(),
                textPaint
        );
        canvas.translate(0, padding + textPaint.getTextSize());

        // Draw camera selector
        canvas.drawText(
                String.format(Locale.CANADA, "Camera: %s", info.cameraDirection),
                width - rectWidth -  margin + padding,
                padding + textPaint.getTextSize(),
                textPaint
        );

        canvas.restore();


    }

    public static void drawInferenceResult(@NonNull Canvas canvas, @NonNull InferenceGraphic.Inference inference) {
        
    }
}
