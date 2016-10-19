package com.melontech.barchart.lib;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MelonBarChart extends LinearLayout {

    private static final double DEFAULT_SCALE_STEP = 2f;
    private static final double DEFAULT_ABSOLUTE_SCALE_MAX = 24f;
    private static final double DEFAULT_DEFAULT_SCALE_MAX = 12f;
    private static final int DEFAULT_BASELINE = 4;
    private static final int DEFAULT_CHART_WIDTH = 30;

    TextView title;
    FrameLayout frame;
    LinearLayout grid;
    FrameLayout chart;
    FrameLayout labels;
    RecyclerView list;

    private int chartWidth = DEFAULT_CHART_WIDTH;
    private int baseline = DEFAULT_BASELINE;

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
        frame = (FrameLayout) root.findViewById(R.id.frame);
        grid = (LinearLayout) root.findViewById(R.id.grid);
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
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("zxc", "onWindowFocusChanged");
        initializeChart();
    }

    private void initializeChart() {

        Set<Integer> dashedLines = fillFakeDashedLinesSet();

        int initialWidth = frame.getWidth();
        int barWidth = initialWidth / chartWidth;
        int newWidth = chartWidth * barWidth;
        if (initialWidth != newWidth) {
            ViewGroup.LayoutParams layoutParams = chart.getLayoutParams();
            layoutParams.width = newWidth;
            chart.setLayoutParams(layoutParams);
        }

        BarAdapter barAdapter = new BarAdapter(barWidth, chartWidth);

        barAdapter.setScaleStep(DEFAULT_SCALE_STEP);
        barAdapter.setAbsoluteScaleMax(DEFAULT_ABSOLUTE_SCALE_MAX);
        barAdapter.setDefaultScaleMax(DEFAULT_DEFAULT_SCALE_MAX);

        fillFakeData(barAdapter);

        list.setAdapter(barAdapter);

        constructBackgroundGrid(barAdapter.getCurrentScaleMax(), dashedLines);
    }

    private void constructBackgroundGrid(double scaleMax, Set<Integer> dashedLines) {
        int gridLineCount = (int) (scaleMax / DEFAULT_SCALE_STEP);
        View view, subview;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);

        LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, 0, 1);
        LinearLayout.LayoutParams baselineParams = new LinearLayout.LayoutParams(0, LayoutParams
                .MATCH_PARENT, 1);
        baselineParams.setMargins(dpToPx(2), 0, dpToPx(1), 0);

        for (int i = gridLineCount - 1; i >= 0; i--) {
            if (dashedLines.contains(i)) {
                view = inflater.inflate(R.layout.view_grid_line_base, grid, false);
                LinearLayout innerFrame = (LinearLayout) view.findViewById(R.id.inner_frame);
                for (int j = 0; j < chartWidth; j++) {
                    subview = inflater.inflate(R.layout.view_base_line_segment, innerFrame, false);
                    subview.setLayoutParams(baselineParams);
                    innerFrame.addView(subview);
                }

            } else {
                view = inflater.inflate(R.layout.view_grid_line, grid, false);
            }
            view.setLayoutParams(horizontalParams);
            grid.addView(view);
        }
    }

    private Set<Integer> fillFakeDashedLinesSet() {
        Set<Integer> dashedLines = new HashSet<>();
        dashedLines.add(3);
        dashedLines.add(7);

        return dashedLines;
    }

    private void fillFakeData(BarAdapter barAdapter) {
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
        values.add(30d);
        values.add(10d);
        values.add(7d);
        values.add(15d);
        values.add(5d);
        values.add(2d);
        values.add(4d);
        values.add(6d);
        values.add(8d);
        barAdapter.setValues(values);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
