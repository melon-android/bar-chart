package com.melontech.barchart.lib;

import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dpanayotov on 10/11/2016
 */

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.BarViewHolder> {

    private static final long DEFAULT_ANIMTION_DURATION = 1000;

    private long animationDuration = DEFAULT_ANIMTION_DURATION;

    private List<Double> values = new ArrayList<>();

    private int barWidth;

    private double scaleMax = 0f;

    private Set<Integer> highlightedBars = new HashSet<>();

    public BarAdapter(List<Double> values, int barwidth, double scaleMax) {
        this.barWidth = barwidth;
        this.scaleMax = scaleMax;

        if (values != null) {
            setValues(values);
        }
    }

    public BarAdapter(int barwidth, double scaleMax) {
        this(null, barwidth, scaleMax);
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    public void setValues(List<Double> values) {
        this.values.clear();
        this.values.addAll(values);
        notifyDataSetChanged();
    }

    public void setHighlightedBars(Set<Integer> highlightedBars) {
        this.highlightedBars.clear();
        this.highlightedBars.addAll(highlightedBars);
        notifyDataSetChanged();
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

        holder.positive.setBackgroundResource(highlightedBars.contains(position) ? R.drawable
                .min_max_bar : R.drawable.normal_bar);

        double resolvedPositiveValue = Math.min(scaleMax, values.get(position).doubleValue());
        double resolvedNegativeValue = scaleMax - resolvedPositiveValue;


        animateWeight(0, (float) resolvedPositiveValue, holder.positive, animationDuration);
        animateWeight((float) scaleMax, (float) resolvedNegativeValue, holder.negative,
                animationDuration);
    }

    private void animateWeight(float startWeight, float endWeight, View view, long duration) {
        ExpandAnimation ea = new ExpandAnimation(startWeight, endWeight, view);
        ea.setDuration(duration);
        view.startAnimation(ea);
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

}
