package com.melontech.barchart.lib;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MelonBarChart extends LinearLayout {

    TextView title;
    FrameLayout grid;
    FrameLayout chart;
    FrameLayout labels;
    RecyclerView list;

    private int chartWidth = 30;

    public MelonBarChart(Context context) {
        super(context);
        init();
    }

    public MelonBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MelonBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        View root = inflate(getContext(), R.layout.view_bar_chart, this);

        title = (TextView) root.findViewById(R.id.title);
        grid = (FrameLayout) root.findViewById(R.id.grid);
        chart = (FrameLayout) root.findViewById(R.id.chart);
        labels = (FrameLayout) root.findViewById(R.id.labels);
        list = (RecyclerView) root.findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        list.setLayoutManager(layoutManager);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        Log.d("zxc", "onWindowFocusChanged");

        int initialWidth = chart.getWidth();
        int barWidth = initialWidth / chartWidth;
        int newWidth = chartWidth * barWidth;
        if (initialWidth != newWidth) {
            ViewGroup.LayoutParams layoutParams = list.getLayoutParams();
            layoutParams.width = newWidth;
            list.setLayoutParams(layoutParams);
        }
        Log.d("zxc", "initial: "+initialWidth);
        Log.d("zxc", "new: "+newWidth);
        Log.d("zxc", "bar: "+barWidth);


        super.onWindowFocusChanged(hasWindowFocus);
        final List<Double> values = new ArrayList<>();

        values.add(8d);
        values.add(7d);
        values.add(0d);
        values.add(13d);
        values.add(14d);
        values.add(18d);
        values.add(3d);
        values.add(12d);
        values.add(15d);
        values.add(5d);
        values.add(2d);
        values.add(4d);
        values.add(6d);
        values.add(8d);
        values.add(8d);
        values.add(7d);
        values.add(0d);
        values.add(13d);
        values.add(10d);
        values.add(7d);
        values.add(15d);
        values.add(5d);
        values.add(2d);
        values.add(4d);
        values.add(6d);
        values.add(8d);


        BarAdapter barAdapter = new BarAdapter(values, barWidth, chartWidth);
        Log.d("zxc", "width: " + list.getWidth());
        list.setAdapter(barAdapter);
    }
}
