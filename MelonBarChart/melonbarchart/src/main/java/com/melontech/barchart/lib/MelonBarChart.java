package com.melontech.barchart.lib;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MelonBarChart extends LinearLayout {

    private static final double DEFAULT_SCALE_STEP = 2f;
    private static final double DEFAULT_ABSOLUTE_SCALE_MAX = 24f;
    private static final double DEFAULT_DEFAULT_SCALE_MAX = 12f;
    private static final int DEFAULT_CHART_DATASET_SIZE = 30;
    private static final int DEFAULT_LABEL_MARGIN_BOTTOM = 2;
    private static final String DEFAULT_LABEL_FORMAT = "%.2fh";


    private static final double DEFAULT_SCALE_PADDING = 0.2;

    private static final int DASHED_LINE_MARGIN_LEFT = 2;
    private static final int DASHED_LINE_MARGIN_RIGHT = 1;
    private static final int SIDE_BAR_MARGIN = 1;

    TextView title;
    LinearLayout grid;
    FrameLayout chart;
    FrameLayout labels;
    RecyclerView list;

    private int chartDataSetSize;
    private Set<Integer> dashedLines;
    private double scaleStep;
    private double absoluteScaleMax;
    private double defaultScaleMax;
    private int labelMarginBottom;
    private String labelFormat;
    Set<Integer> highlightedBars = new HashSet<>();
    Set<Integer> labeledBars = new HashSet<>();
    private List<Double> values;

    private double min, max;
    private int minPos, maxPos;
    private double currentScaleMax;
    private int barWidth;

    BarAdapter adapter;

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

        chart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    chart.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    chart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                initializeChart();
                constructBackgroundGrid();
            }
        });
    }

    private void initializeChart() {

        initializeData();

        barWidth = calculateBarWidth();
        resizeChart(barWidth);


        //TODO
        fillFakeData();

        addZeroPadding();
        trimDataSize();
        getMinMax();
        calculateScale();
        //TODO

        adapter = new BarAdapter(barWidth, currentScaleMax);

        adapter.setAnimationListener(new BarAdapter.AnimationListener() {
            @Override
            public void onAnimationStaring() {
                clearLabels();
            }

            @Override
            public void onAminationComplete() {
                constructLabels();
            }
        });

        adapter.setValues(values);

        highlightedBars.add(minPos);
        highlightedBars.add(maxPos);

        labeledBars.add(minPos);
        labeledBars.add(maxPos);

        adapter.setHighlightedBars(highlightedBars);

        list.setAdapter(adapter);
    }

    private void initializeData() {
        chartDataSetSize = DEFAULT_CHART_DATASET_SIZE;
        scaleStep = DEFAULT_SCALE_STEP;
        absoluteScaleMax = DEFAULT_ABSOLUTE_SCALE_MAX;
        defaultScaleMax = DEFAULT_DEFAULT_SCALE_MAX;
        labelMarginBottom = DEFAULT_LABEL_MARGIN_BOTTOM;
        labelFormat = DEFAULT_LABEL_FORMAT;
        dashedLines = fillFakeDashedLinesSet();
    }

    private int calculateBarWidth() {
        return chart.getWidth() / chartDataSetSize;
    }

    private void resizeChart(int barWidth) {
        int newChartWidth = chartDataSetSize * barWidth;
        if (chart.getWidth() != newChartWidth) {
            ViewGroup.LayoutParams layoutParams = chart.getLayoutParams();
            layoutParams.width = newChartWidth;
            chart.setLayoutParams(layoutParams);
        }
    }

    private void addZeroPadding() {
        if (chartDataSetSize > 0) {
            while (this.values.size() < chartDataSetSize) {
                this.values.add(0, 0d);
            }
        }
    }

    private void trimDataSize() {
        if (chartDataSetSize > 0) {
            while (this.values.size() > chartDataSetSize) {
                this.values.remove(0);
            }
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

    private void calculateScale() {
        currentScaleMax = scaleStep != 0 ? ceil(max, scaleStep) : max * (1 + DEFAULT_SCALE_PADDING);
        if (absoluteScaleMax != 0 && currentScaleMax > absoluteScaleMax) {
            currentScaleMax = absoluteScaleMax;
        }
        if (defaultScaleMax != 0 && currentScaleMax < defaultScaleMax) {
            currentScaleMax = defaultScaleMax;
        }
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
        baselineParams.setMargins(dpToPx(DASHED_LINE_MARGIN_LEFT), 0, dpToPx
                (DASHED_LINE_MARGIN_RIGHT), 0);

        grid.removeAllViews();

        for (int i = gridLineCount - 1; i >= 0; i--) {
            if (dashedLines.contains(i)) {
                view = inflater.inflate(R.layout.view_grid_line_base, grid, false);
                LinearLayout innerFrame = (LinearLayout) view.findViewById(R.id.inner_frame);
                for (int j = 0; j < chartDataSetSize; j++) {
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

    private void constructLabels() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        TextView textView;
        for (int position : labeledBars) {
            textView = (TextView) inflater.inflate(R.layout.view_label, labels, false);
            textView.setText(String.format(Locale.getDefault(), labelFormat, values.get(position)));
            textView.measure(0, 0);
            int textViewWidth = textView.getMeasuredWidth();
            int textViewHeight = textView.getMeasuredHeight();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(position * barWidth + dpToPx(SIDE_BAR_MARGIN) + Math.round
                    (barWidth / 2f) - Math.round(textViewWidth / 2f), chart.getMeasuredHeight() -
                    adapter.getBarHeight(position) - textViewHeight - dpToPx(labelMarginBottom),
                    0, 0);
            textView.setLayoutParams(layoutParams);
            labels.addView(textView);
        }
    }

    private void clearLabels() {
        labels.removeAllViews();
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

    public void animateBars() {
        adapter.animate();
    }

}
