package com.example.zfile.audiovis.ColorPicker;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.zfile.audiovis.R;
import com.example.zfile.audiovis.ColorPicker.ColorEnvelope;
import com.example.zfile.audiovis.ColorPicker.FlagView;

/**
 * Developed by skydoves on 2018-02-11.
 * Copyright (c) 2018 skydoves rights reserved.
 */

public class CustomFlag extends FlagView {

    private TextView textView;
    private View view;

    /**
     * onBind Views
     * @param context context
     * @param layout custom flagView's layout
     */
    public CustomFlag(Context context, int layout) {
        super(context, layout);
        textView = findViewById(R.id.flag_color_code);
        view = findViewById(R.id.flag_color_layout);
    }

    /**
     * invoked when selector moved
     * @param colorEnvelope provide color, htmlCode, rgb
     */
    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
        textView.setText("#" + colorEnvelope.getColorHtml());
        view.setBackgroundColor(colorEnvelope.getColor());
    }
}

