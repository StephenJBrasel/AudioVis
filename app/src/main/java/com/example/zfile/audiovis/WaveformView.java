package com.example.zfile.audiovis;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zfile.audiovis.renderer.SimpleWaveformRenderer;
import com.example.zfile.audiovis.renderer.WaveformRenderer;

import java.util.Arrays;

public class WaveformView extends View {
    private static final String LOG_TAG_WAVE = WaveformView.class.getSimpleName();
    private byte[] waveform;
    private WaveformRenderer renderer;
    private double pause_start_time = System.nanoTime();
    private double ask_time = 3E9;

    public WaveformView(Context context) {
        super(context);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAskTime(double time) {
        this.ask_time = time;
    }

    public double getAskTime() {
        return this.ask_time;
    }

    public void setStartTime(double time) {
        this.pause_start_time = time;
    }

    public double getStartTime() {
        return this.pause_start_time;
    }

    public void setRenderer(WaveformRenderer renderer) {
        this.renderer = renderer;
    }

    public void setRenderColor(@ColorInt int hue) {
        if (renderer != null) {
            if (renderer.getClass() == SimpleWaveformRenderer.class) {
                SimpleWaveformRenderer rAlt = (SimpleWaveformRenderer) renderer;
                rAlt.mixAndSetForegroundPaint(hue);
                renderer = rAlt;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (renderer != null) {
            renderer.render(canvas, waveform);
            if (renderer.getClass() == SimpleWaveformRenderer.class) {
                SimpleWaveformRenderer rAlt = (SimpleWaveformRenderer) renderer;
                double pause_elapsed_time = rAlt.pause_end_time - pause_start_time;
                if (pause_elapsed_time > ask_time) {
                    pause_start_time = System.nanoTime();
                    ask_time = 30E9;
                    playMusic();
                }
            }
        }
    }

    public void setWaveform(byte[] waveform) {
        this.waveform = Arrays.copyOf(waveform, waveform.length);
        invalidate();
    }

    public void playMusic() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*");
        intent.putExtra(
                SearchManager.QUERY, "");
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            //getContext().startActivityForResult(intent, 0);
            getContext().startActivity(intent);
        } else {
            Log.d("ImplicitIntents", "Can't handle this intent!");
            Toast.makeText(getContext(), "This device needs to play music in order to access these features.", Toast.LENGTH_LONG);
        }
    }
}