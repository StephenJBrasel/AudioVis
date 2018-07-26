package com.example.zfile.audiovis.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class SimpleWaveformRenderer implements WaveformRenderer {
    private static final int Y_FACTOR = 0xFF;
    private static final float HALF_FACTOR = 0.5f;
    @ColorInt
    private final int backgroundColour;
    private Paint foregroundPaint;
    private final Path waveformPath;
    public boolean waveformIsBlank = true;
    public double pause_end_time = 0;

    SimpleWaveformRenderer(@ColorInt int backgroundColour, Paint foregroundPaint, Path waveformPath) {
        this.backgroundColour = backgroundColour;
        setForegroundPaint(foregroundPaint);
        this.waveformPath = waveformPath;
    }

    static SimpleWaveformRenderer newInstance(@ColorInt int backgroundColour, @ColorInt int foregroundColour) {
        Paint paint = mixPaint(foregroundColour);
        Path waveformPath = new Path();
        return new SimpleWaveformRenderer(backgroundColour, paint, waveformPath);
    }

    @NonNull
    private static Paint mixPaint(@ColorInt int foregroundColour) {
        Paint paint = new Paint();
        paint.setColor(foregroundColour);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    public void mixAndSetForegroundPaint(@ColorInt int color) {
        Paint newPaint = mixPaint(color);
        setForegroundPaint(newPaint);
    }

    public void setForegroundPaint(Paint paint) {
        this.foregroundPaint = paint;
    }

    public void SetHSV(float hue) {
        if (hue == -1) {
            hue = 255;
        }
        float[] hsv = new float[3];
        hsv[0] = hue; //hue
        hsv[1] = 1; //saturation
        hsv[2] = 1; //brightness
        int newColor = Color.HSVToColor(hsv);
        Paint paint = mixPaint(newColor);
        setForegroundPaint(paint);
    }

    @Override
    public void render(Canvas canvas, byte[] waveform) {
        canvas.drawColor(backgroundColour);
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        waveformPath.reset();
        waveformIsBlank = true;
        if (waveform != null) {
            byte firstVal = waveform[0];
            for (int i = 0; i < waveform.length; i++) {
                if (firstVal != waveform[i]) {
                    waveformIsBlank = false;
                }
            }
            if (!waveformIsBlank){
                renderWaveformAsLine(waveform, width, height);
            } else {
                renderBlank(width, height);
            }
        } else {
            renderBlank(width, height);
        }
        canvas.drawPath(waveformPath, foregroundPaint);
    }

    private void renderWaveformAsLine(byte[] waveform, float width, float height) {
        float xIncrement = width / (float) (waveform.length);
        float yIncrement = height / Y_FACTOR;
        int halfHeight = (int) (height * HALF_FACTOR);
        waveformPath.moveTo(0, halfHeight);
        for (int i = 1; i < waveform.length; i++) {
            float yPosition = waveform[i] > 0 ? height - (yIncrement * waveform[i]) : -(yIncrement * waveform[i]);
            waveformPath.lineTo(xIncrement * i, yPosition);
        }
        waveformPath.lineTo(width, halfHeight);
    }

    private void renderBlank(float width, float height) {
        //Change this to drawtText("Music must be playing from another program to see Visualization.")
        int y = (int) (height * HALF_FACTOR);
        waveformPath.moveTo(0, y);
        waveformPath.lineTo(width, y);
        pause_end_time = System.nanoTime();
    }

}
