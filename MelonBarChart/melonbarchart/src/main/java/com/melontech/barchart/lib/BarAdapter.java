package com.melontech.barchart.lib;

import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpanayotov on 10/11/2016.
 */

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.BarViewHolder> {

    private static final double DEFAULT_SCALE_PADDING = 0.2;

    private List<Double> values = new ArrayList<>();

    private int barWidth;
    private int dataSetSize;

    private double min, max;
    private int minPos, maxPos;

    private double defaultScaleMax = 0f;
    private double absoluteScaleMax = 0f;
    private double scaleStep = 0f;
    private double scaleMedian = 0f;

    private double currentScaleMax = 0f;

    public BarAdapter(List<Double> values, int barwidth, int dataSetSize) {
        this.barWidth = barwidth;
        this.dataSetSize = dataSetSize;

        if (values != null) {
            setValues(values);
        }
    }

    public BarAdapter(int width, int dataSetSize) {
        this(null, width, dataSetSize);
    }

    public BarAdapter(List<Double> values, int width) {
        this(values, width, 0);
    }

    public BarAdapter(int width) {
        this(null, width, 0);
    }

    public double getDefaultScaleMax() {
        return defaultScaleMax;
    }

    public void setDefaultScaleMax(double defaultScaleMax) {
        this.defaultScaleMax = defaultScaleMax;
    }

    public double getAbsoluteScaleMax() {
        return absoluteScaleMax;
    }

    public void setAbsoluteScaleMax(double absoluteScaleMax) {
        this.absoluteScaleMax = absoluteScaleMax;
    }

    public double getScaleStep() {
        return scaleStep;
    }

    public void setScaleStep(double scaleStep) {
        this.scaleStep = scaleStep;
    }

    public double getScaleMedian() {
        return scaleMedian;
    }

    public void setScaleMedian(double scaleMedian) {
        this.scaleMedian = scaleMedian;
    }

    public void setValues(List<Double> values) {
        this.values.clear();
        this.values.addAll(values);
        addZeroPadding();
        trimDataSize();
        getMinMax();
        calculateScale();
        notifyDataSetChanged();
    }

    private void calculateScale() {
        currentScaleMax = scaleStep != 0 ? ceil(max, scaleStep) : max * (1 + DEFAULT_SCALE_PADDING);
        if (absoluteScaleMax != 0 && currentScaleMax > absoluteScaleMax) {
            currentScaleMax = absoluteScaleMax;
        }
        if (defaultScaleMax != 0 && currentScaleMax < defaultScaleMax) {
            currentScaleMax = defaultScaleMax;
        }
        Log.d("zxc", "currentScaleMax: "+currentScaleMax);
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
        if (dataSetSize > 0) {
            while (this.values.size() < dataSetSize) {
                this.values.add(0, 0d);
            }
        }
    }

    private void trimDataSize() {
        if (dataSetSize > 0) {
            while (this.values.size() > dataSetSize) {
                this.values.remove(0);
            }
        }
    }

    @Override
    public BarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bar, parent,
                false);
        return new BarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BarViewHolder holder, int position) {
        holder.bar.setLayoutParams(new LinearLayoutCompat.LayoutParams(barWidth, ViewGroup
                .LayoutParams.MATCH_PARENT));

        if (position == minPos || position == maxPos) {
            holder.positive.setBackgroundResource(R.drawable.min_max_bar);
        } else {
            holder.positive.setBackgroundResource(R.drawable.normal_bar);
        }

        double resolvedPositiveValue = Math.min(absoluteScaleMax, values.get(position).doubleValue());
        double resolvedNegativeValue = currentScaleMax - resolvedPositiveValue;


        ExpandAnimation eap = new ExpandAnimation(0, (float) resolvedPositiveValue, holder.positive);
        eap.setDuration(1000);
        holder.positive.startAnimation(eap);

        ExpandAnimation ean = new ExpandAnimation((float) currentScaleMax, (float) resolvedNegativeValue, holder.negative);
        ean.setDuration(1000);
        holder.negative.startAnimation(ean);



    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class BarViewHolder extends RecyclerView.ViewHolder {

        View bar;
        View positive;
        View negative;

        public BarViewHolder(View itemView) {
            super(itemView);
            bar = itemView.findViewById(R.id.bar);
            positive = itemView.findViewById(R.id.positive);
            negative = itemView.findViewById(R.id.negative);
        }
    }

    private class ExpandAnimation extends Animation {

        private final float mStartWeight;
        private final float mDeltaWeight;
        private View view;

        public ExpandAnimation(float startWeight, float endWeight, View view) {
            mStartWeight = startWeight;
            mDeltaWeight = endWeight - startWeight;
            this.view = view;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            lp.weight = (mStartWeight + (mDeltaWeight * interpolatedTime));
            view.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    private static double ceil(double input, double step) {
        return Math.ceil(input / step) * step;
    }
}
