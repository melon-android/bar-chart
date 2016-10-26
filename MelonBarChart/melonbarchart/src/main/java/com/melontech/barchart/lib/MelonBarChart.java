package com.melontech.barchart.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MelonBarChart extends LinearLayout {

    private static final int DEFAULT_FAKE_DATA_SET_SIZE = 10;
    private static final double DEFAULT_FAKE_DATA_SET_MAX = 100;

    private static final long DEFAULT_BAR_ANIMATION_TIME = 1000;
    private static final long DEFAULT_LABEL_ANIMATION_TIME = 500;

    private static final double DEFAULT_SCALE_PADDING = 0.2;

    private static final int DASHED_LINE_MARGIN_LEFT = 2;
    private static final int DASHED_LINE_MARGIN_RIGHT = 1;
    private static final int SIDE_BAR_MARGIN = 1;

    LinearLayout chartRoot;
    TextView title;
    LinearLayout grid;
    FrameLayout chart;
    FrameLayout frame;
    FrameLayout labels;
    RecyclerView list;
    View blank;
    TextView overlay;

    private Parameters params;
    private List<Double> values = new ArrayList<>();

    private double maxValue;
    private double scaleMax;
    private int barWidth;
    private boolean initialized = false;
    private boolean fakeData = true;
    private Animation.AnimationListener animationListener;

    BarAdapter adapter;

    public MelonBarChart(Context context) {
        super(context);
        resetChart(context, null);
    }

    public MelonBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        resetChart(context, attrs);
    }

    public MelonBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        resetChart(context, attrs);
    }

    private void resetChart(Context context, AttributeSet attributeSet) {
        View root = inflate(getContext(), R.layout.view_bar_chart, this);

        chartRoot = (LinearLayout) root.findViewById(R.id.chart_root);
        title = (TextView) root.findViewById(R.id.title);
        grid = (LinearLayout) root.findViewById(R.id.grid);
        chart = (FrameLayout) root.findViewById(R.id.chart);
        frame = (FrameLayout) root.findViewById(R.id.frame);
        labels = (FrameLayout) root.findViewById(R.id.labels);
        list = (RecyclerView) root.findViewById(R.id.list);
        blank = root.findViewById(R.id.blank);
        overlay = (TextView) root.findViewById(R.id.overlay);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        list.setLayoutManager(layoutManager);

        params = attributeSet != null ? getAtttributeParameters(context, attributeSet) :
                getDefaultParameters();

        chart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    chart.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    chart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                initialized = true;
                fillInFakeData();
                resetChart();
            }
        });
    }

    private void resetChart() {
        animationListener = null;
        grid.removeAllViews();
        labels.removeAllViews();

        if (values.size() > 0 && initialized) {
            setTitle();
            setEmptyOverlay();
            animationListener = new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (animationListener == this) {
                        constructLabels();
                        animationListener = null;
                    }
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            };
            constructBars(animationListener);
            constructBackgroundGrid();
        }
    }

    private void setTitle() {
        title.setText(params.title);
        title.setTextColor(params.titleColor);
    }

    private void setEmptyOverlay() {
        if (fakeData) {
            overlay.setVisibility(VISIBLE);
            overlay.setText(params.overlayText);
            overlay.setTextColor(params.overlayTextColor);
            overlay.setBackgroundColor(Color.argb((int) (255 * params.overlayBackgroundOpacity),
                    Color.red(params.overlayBackgroundColor), Color.green(params
                            .overlayBackgroundColor), Color.blue(params.overlayBackgroundColor)));
        } else {
            overlay.setVisibility(INVISIBLE);
        }
    }

    private void constructBars(Animation.AnimationListener animationListener) {

        barWidth = calculateBarWidth();
        resizeChart(barWidth);

        adapter = new BarAdapter(barWidth, scaleMax);

        if (params.barAccentColor != -1) {
            adapter.setAccentColor(params.barAccentColor);
        }
        if (params.barAccentHeight != -1) {
            adapter.setAccentHeight(params.barAccentHeight);
        }
        if (params.barBackground != -1) {
            adapter.setBarBackground(params.barBackground);
        }
        if (params.highlightedBarBackground != -1) {
            adapter.setHighligtedBarBackground(params.highlightedBarBackground);
        }

        adapter.setValues(values);
        adapter.setHighlightedBars(params.highlightedBars);
        list.setAdapter(adapter);
        if (fakeData) {
            blank.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            list.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            animateBars(DEFAULT_BAR_ANIMATION_TIME, grid.getMeasuredHeight(), animationListener);
        }
    }

    private int calculateBarWidth() {
        return values.size() != 0 ? (chartRoot.getWidth() - ((FrameLayout.LayoutParams) frame
                .getLayoutParams()).leftMargin - ((FrameLayout.LayoutParams) frame
                .getLayoutParams()).rightMargin) / values.size() : 0;
    }

    private void resizeChart(int barWidth) {
        int newChartWidth = values.size() * barWidth;
        chart.getLayoutParams().width = newChartWidth + ((FrameLayout.LayoutParams) frame
                .getLayoutParams()).leftMargin + ((FrameLayout.LayoutParams) frame
                .getLayoutParams()).rightMargin;
    }

    private void animateBars(long duration, final int height, Animation.AnimationListener
            animationListener) {

        list.getLayoutParams().height = 1;
        list.setVisibility(View.VISIBLE);

        Animation listAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    list.getLayoutParams().height = LayoutParams.MATCH_PARENT;
                } else {
                    list.getLayoutParams().height = (int) (height * interpolatedTime);
                    list.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        listAnimation.setDuration(duration);
        list.startAnimation(listAnimation);

        blank.setVisibility(VISIBLE);
        blank.getLayoutParams().height = height;

        Animation blankAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    blank.setVisibility(View.GONE);
                } else {
                    blank.getLayoutParams().height = height - (int) (height * interpolatedTime);
                    blank.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        blankAnimation.setDuration(duration);
        blank.startAnimation(blankAnimation);

        blankAnimation.setAnimationListener(animationListener);
    }

    private void constructBackgroundGrid() {
        if (params.scaleStep > 0 && scaleMax != 0) {
            int gridLineCount = (int) (scaleMax / params.scaleStep);

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                    .LAYOUT_INFLATER_SERVICE);

            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
            LinearLayout.LayoutParams dashedLineParams = new LinearLayout.LayoutParams(0,
                    LayoutParams.MATCH_PARENT, 1);
            dashedLineParams.setMargins(Util.dpToPx(DASHED_LINE_MARGIN_LEFT), 0, Util.dpToPx
                    (DASHED_LINE_MARGIN_RIGHT), 0);

            View view, subview;
            for (int i = gridLineCount - 1; i >= 0; i--) {
                if (params.dashedLines.contains(i)) {
                    view = inflater.inflate(R.layout.view_grid_line_dashed, grid, false);
                    LinearLayout innerFrame = (LinearLayout) view.findViewById(R.id.inner_frame);
                    for (int j = 0; j < params.fixedDataSetSize; j++) {
                        subview = inflater.inflate(R.layout.view_dashed_line_segment, innerFrame,
                                false);
                        subview.setLayoutParams(dashedLineParams);
                        subview.setBackgroundColor(params.gridLineDashedColor);
                        innerFrame.addView(subview);
                    }

                } else {
                    view = inflater.inflate(R.layout.view_grid_line, grid, false);
                    view.findViewById(R.id.line).setBackgroundColor(params.gridLineColor);
                }
                view.setLayoutParams(lineParams);
                grid.addView(view);
            }
        }
    }

    private void constructLabels() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        TextView textView;
        for (int position : params.labeledBars) {
            textView = (TextView) inflater.inflate(R.layout.view_label, labels, false);
            textView.setTextColor(params.labelColor);
            textView.setBackgroundResource(params.labelBackground);
            textView.setText(String.format(Locale.getDefault(), params.labelFormat, values.get
                    (position)));
            textView.measure(0, 0);
            int textViewWidth = textView.getMeasuredWidth();
            int textViewHeight = textView.getMeasuredHeight();
            int barHeight = list.getChildAt(position).findViewById(R.id.positive)
                    .getMeasuredHeight();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(position * barWidth + Util.dpToPx(SIDE_BAR_MARGIN) + Math.round
                    (barWidth / 2f) - Math.round(textViewWidth / 2f) + ((FrameLayout
                    .LayoutParams) frame.getLayoutParams()).leftMargin, chart.getMeasuredHeight() -
                    barHeight - textViewHeight - Util.dpToPx(params.labelMarginBottom), 0, 0);
            textView.setLayoutParams(layoutParams);
            labels.addView(textView);
        }
        animateLabels(DEFAULT_LABEL_ANIMATION_TIME);
    }

    private void animateLabels(long duration) {
        Animation alphaAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                labels.setAlpha(interpolatedTime);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        alphaAnimation.setDuration(duration);
        labels.startAnimation(alphaAnimation);
    }

    private void validateValues() {
        addZeroPadding();
        trimDataSize();
        getMinMax();
        calculateScale();
    }

    private void addZeroPadding() {
        if (params.fixedDataSetSize > 0) {
            while (this.values.size() < params.fixedDataSetSize) {
                this.values.add(0, 0d);
            }
        }
    }

    private void trimDataSize() {
        if (params.fixedDataSetSize > 0) {
            while (this.values.size() > params.fixedDataSetSize) {
                this.values.remove(0);
            }
        }
    }

    private void getMinMax() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        int minPos = -1;
        int maxPos = -1;

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

        maxValue = max;

        params.highlightedBars.add(minPos);
        params.highlightedBars.add(maxPos);

        params.labeledBars.add(minPos);
        params.labeledBars.add(maxPos);
    }

    private void calculateScale() {
        scaleMax = params.scaleStep != 0 ? Util.ceil(maxValue, params.scaleStep) : maxValue * (1
                + DEFAULT_SCALE_PADDING);
        if (params.absoluteScaleMax != 0 && scaleMax > params.absoluteScaleMax) {
            scaleMax = params.absoluteScaleMax;
        }
        if (params.minimumScaleMax != 0 && scaleMax < params.minimumScaleMax) {
            scaleMax = params.minimumScaleMax;
        }
    }

    private Parameters getAtttributeParameters(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable
                .MelonBarChart, 0, 0);
        Parameters params = new Parameters();
        try {
            params.fixedDataSetSize = a.getInteger(R.styleable.MelonBarChart_fixed_data_set_size,
                    DefaultParameters.fixedDataSetSize);
            params.scaleStep = a.getFloat(R.styleable.MelonBarChart_scale_step, DefaultParameters
                    .scaleStep);
            params.absoluteScaleMax = a.getFloat(R.styleable
                    .MelonBarChart_absolute_scale_maximum, DefaultParameters.absoluteScaleMax);
            params.minimumScaleMax = a.getFloat(R.styleable.MelonBarChart_minimum_scale_maximum,
                    DefaultParameters.minimumScaleMax);

            params.labelColor = a.getColor(R.styleable.MelonBarChart_label_color,
                    DefaultParameters.labelColor);
            params.labelBackground = a.getResourceId(R.styleable.MelonBarChart_label_background,
                    DefaultParameters.labelBackground);
            params.labelMarginBottom = a.getDimensionPixelSize(R.styleable
                    .MelonBarChart_label_margin_bottom, Util.dpToPx(DefaultParameters
                    .labelMarginBottom));
            params.labelFormat = a.getString(R.styleable.MelonBarChart_label_format);
            if (params.labelFormat == null) {
                params.labelFormat = DefaultParameters.labelFormat;
            }

            params.gridLineColor = a.getColor(R.styleable.MelonBarChart_grid_line_color,
                    DefaultParameters.gridLineColor);
            params.gridLineDashedColor = a.getColor(R.styleable
                            .MelonBarChart_grid_line_dashed_color,
                    DefaultParameters.gridLineDashedColor);

            params.barAccentHeight = a.getDimensionPixelSize(R.styleable
                    .MelonBarChart_bar_accent_height, -1);
            if (params.barAccentHeight == -1) {
                params.barAccentHeight = DefaultParameters.barAccentHeight;
            } else {
                params.barAccentHeight = Util.pxToDp(params.barAccentHeight);
            }
            params.barAccentColor = a.getColor(R.styleable.MelonBarChart_bar_accent_color,
                    DefaultParameters.barAccentColor);
            params.barBackground = a.getResourceId(R.styleable.MelonBarChart_bar_background, R
                    .drawable.normal_bar);
            params.highlightedBarBackground = a.getResourceId(R.styleable
                    .MelonBarChart_highlighted_bar_background, R.drawable.highlighted_bar);

            params.title = a.getString(R.styleable.MelonBarChart_title);
            if (params.title == null) {
                params.title = DefaultParameters.title;
            }
            params.titleColor = a.getColor(R.styleable.MelonBarChart_title_color,
                    DefaultParameters.titleColor);

            params.overlayText = a.getString(R.styleable.MelonBarChart_overlay_text);
            if (params.overlayText == null) {
                params.overlayText = DefaultParameters.overlayText;
            }
            params.overlayTextColor = a.getColor(R.styleable.MelonBarChart_overlay_text_color,
                    DefaultParameters.overlayTextColor);
            params.overlayBackgroundColor = a.getColor(R.styleable
                    .MelonBarChart_overlay_background_color, DefaultParameters
                    .overlayBackgroundColor);
            params.overlayBackgroundOpacity = a.getFloat(R.styleable
                    .MelonBarChart_overlay_background_opacity, DefaultParameters
                    .overlayBackgroundOpacity);

        } finally {
            a.recycle();
        }
        return params;
    }

    private Parameters getDefaultParameters() {
        Parameters params = new Parameters();

        params.fixedDataSetSize = DefaultParameters.fixedDataSetSize;
        params.scaleStep = DefaultParameters.scaleStep;
        params.absoluteScaleMax = DefaultParameters.absoluteScaleMax;
        params.minimumScaleMax = DefaultParameters.minimumScaleMax;

        params.labelColor = DefaultParameters.labelColor;
        params.labelBackground = DefaultParameters.labelBackground;
        params.labelMarginBottom = Util.dpToPx(DefaultParameters.labelMarginBottom);
        params.labelFormat = DefaultParameters.labelFormat;

        params.gridLineColor = DefaultParameters.gridLineColor;
        params.gridLineDashedColor = DefaultParameters.gridLineDashedColor;

        params.barAccentHeight = Util.dpToPx(DefaultParameters.barAccentHeight);
        params.barAccentColor = DefaultParameters.barAccentColor;
        params.barBackground = DefaultParameters.barBackground;
        params.highlightedBarBackground = DefaultParameters.highlightedBarBackground;

        params.title = DefaultParameters.title;
        params.titleColor = DefaultParameters.titleColor;

        params.overlayText = DefaultParameters.overlayText;
        params.overlayTextColor = DefaultParameters.overlayTextColor;
        params.overlayBackgroundColor = DefaultParameters.overlayBackgroundColor;
        params.overlayBackgroundOpacity = DefaultParameters.overlayBackgroundOpacity;

        return params;
    }

    class Parameters {
        int fixedDataSetSize;
        float scaleStep;
        float absoluteScaleMax;
        float minimumScaleMax;

        int barAccentHeight;
        int barAccentColor;
        int barBackground;
        int highlightedBarBackground;

        int gridLineColor;
        int gridLineDashedColor;

        int labelColor;
        int labelBackground;
        int labelMarginBottom;
        String labelFormat;

        String title;
        int titleColor;

        String overlayText;
        int overlayTextColor;
        int overlayBackgroundColor;
        float overlayBackgroundOpacity;

        Set<Integer> dashedLines = new HashSet<>();
        Set<Integer> highlightedBars = new HashSet<>();
        Set<Integer> labeledBars = new HashSet<>();

    }

    static class DefaultParameters {
        static final int fixedDataSetSize = 0;
        static final float scaleStep = 0f;
        static final float absoluteScaleMax = 0f;
        static final float minimumScaleMax = 0f;

        static int labelColor = Color.parseColor("#ffffff");
        static int labelBackground = R.drawable.label_frame;
        static final int labelMarginBottom = 2;
        static final String labelFormat = "%.2d";

        static int gridLineColor = Color.parseColor("#2A454E");
        static int gridLineDashedColor = Color.parseColor("#566C73");

        static int barAccentHeight = -1;
        static int barAccentColor = -1;
        static int barBackground = -1;
        static int highlightedBarBackground = -1;

        static String title = "Title Placeholder";
        static int titleColor = Color.parseColor("#ffffff");

        static String overlayText = "We currently don't have enough data to show your graph. Just" +
                " wait for it while using the application.";
        static int overlayTextColor = Color.parseColor("#ffffff");
        static int overlayBackgroundColor = Color.parseColor("#15323C");
        static float overlayBackgroundOpacity = 0.9f;

    }

    public void setValues(List<Double> values) {

        fakeData = false;

        this.values.clear();
        params.labeledBars.clear();
        params.highlightedBars.clear();

        this.values.addAll(values);

        validateValues();

        resetChart();
    }

    public void setDashedGridLines(Set<Integer> dashedLines) {
        params.dashedLines.clear();
        params.dashedLines.addAll(dashedLines);

        resetChart();
    }

    public void fillInFakeData() {
        int size = params.fixedDataSetSize != 0 ? params.fixedDataSetSize :
                DEFAULT_FAKE_DATA_SET_SIZE;
        double max = params.minimumScaleMax != 0 ? params.minimumScaleMax :
                DEFAULT_FAKE_DATA_SET_MAX;
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            values.add(random.nextDouble() * max);
        }

        validateValues();
    }
}
