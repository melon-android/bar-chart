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
    private static final int DEFAULT_CHART_WIDTH = 30;

    private static final double DEFAULT_SCALE_PADDING = 0.2;


    TextView title;
    FrameLayout frame;
    LinearLayout grid;
    FrameLayout chart;
    FrameLayout labels;
    RecyclerView list;

    private int chartWidth;
    private Set<Integer> dashedLines;
    private double scaleStep;
    private double absoluteScaleMax;
    private double defaultScaleMax;
    private double currentScaleMax;

    private double min, max;
    private int minPos, maxPos;

    private List<Double> values;

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

        initializeData();

        Set<Integer> dashedLines = fillFakeDashedLinesSet();

        int initialWidth = frame.getWidth();
        int barWidth = initialWidth / chartWidth;
        int newWidth = chartWidth * barWidth;
        if (initialWidth != newWidth) {
            ViewGroup.LayoutParams layoutParams = chart.getLayoutParams();
            layoutParams.width = newWidth;
            chart.setLayoutParams(layoutParams);
        }


        fillFakeData();

        addZeroPadding();
        trimDataSize();
        getMinMax();
        calculateScale();

        BarAdapter barAdapter = new BarAdapter(barWidth, currentScaleMax);

        barAdapter.setValues(values);

        Set<Integer> highlightedBars = new HashSet<>();
        highlightedBars.add(minPos);
        highlightedBars.add(maxPos);

        barAdapter.setHighlightedBars(highlightedBars);

        list.setAdapter(barAdapter);

        constructBackgroundGrid();
    }

    private void constructBackgroundGrid() {
        int gridLineCount = (int) (currentScaleMax / scaleStep);
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

    private void getMinMax() {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        minPos = -1;
        maxPos = -1;

        double val;
        for (int i = 0; i < values.size(); i++) {
            val = values.get(i);
            if (val > 0 && val < min) {
                min = val;
                minPos = i;
            }
            if (val > max) {
                max = val;
                maxPos = i;
            }
        }
    }

    private void addZeroPadding() {
        if (chartWidth > 0) {
            while (this.values.size() < chartWidth) {
                this.values.add(0, 0d);
            }
        }
    }

    private void trimDataSize() {
        if (chartWidth > 0) {
            while (this.values.size() > chartWidth) {
                this.values.remove(0);
            }
        }
    }

    private void calculateScale() {
        currentScaleMax = scaleStep != 0 ? ceil(max, scaleStep) : max * (1 + DEFAULT_SCALE_PADDING);
        if (absoluteScaleMax != 0 && currentScaleMax > absoluteScaleMax) {
            currentScaleMax = absoluteScaleMax;
        }
        if (defaultScaleMax != 0 && currentScaleMax < defaultScaleMax) {
            currentScaleMax = defaultScaleMax;
        }
        Log.d("zxc", "currentScaleMax: " + currentScaleMax);
    }

    private void initializeData() {
        chartWidth = DEFAULT_CHART_WIDTH;
        scaleStep = DEFAULT_SCALE_STEP;
        absoluteScaleMax = DEFAULT_ABSOLUTE_SCALE_MAX;
        defaultScaleMax = DEFAULT_DEFAULT_SCALE_MAX;
        dashedLines = fillFakeDashedLinesSet();
    }

    private Set<Integer> fillFakeDashedLinesSet() {
        Set<Integer> dashedLines = new HashSet<>();
        dashedLines.add(3);
        dashedLines.add(7);

        return dashedLines;
    }

    private void fillFakeData() {
        values = new ArrayList<>();

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
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private double ceil(double input, double step) {
        return Math.ceil(input / step) * step;
    }

}
