package com.ubcohci.fingerdetection.graphics;

import android.graphics.Canvas;

public class InferenceGraphic extends GraphicOverlay.Graphic {
    private final Inference inference;

    public InferenceGraphic(GraphicOverlay graphicOverlay, Inference inference) {
        super(graphicOverlay);
        this.inference = inference;
    }

    @Override
    public void draw(Canvas canvas) {
        if (this.inference != null) {
            DrawingUtils.drawInferenceResult(canvas, this.inference);
        }
    }

    public static class Inference {
        public final long latency; // Latency
        public final int[] imageDim; // Image dimension
        public Inference(long latency, int[] imageDim) {
            this.latency = latency;
            this.imageDim = imageDim;
        }
    }
}
