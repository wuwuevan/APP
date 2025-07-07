package com.example.myapplication.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.myapplication.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final SimpleDateFormat dateFormat;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tv_content);
        dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        String date = dateFormat.format(new Date((long) e.getX()));
        String value = String.format(Locale.getDefault(), "%.1f", e.getY());
        tvContent.setText(String.format("%s\n心率: %s", date, value));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
} 