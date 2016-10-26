package com.melontech.barchart.lib;

import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by dpanayotov on 10/11/2016
 */

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.BarViewHolder> {

    private int barWidth;

    private double scaleMax;

    private List<Double> values = new ArrayList<>();

    private Set<Integer> highlightedBars = new HashSet<>();

    public BarAdapter(List<Double> values, int barWidth, double scaleMax) {
        this.barWidth = barWidth;
        this.scaleMax = scaleMax;

        if (values != null) {
            setValues(values);
        }
    }

    public BarAdapter(int barWidth, double scaleMax) {
        this(null, barWidth, scaleMax);
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

    public void setScaleMax(double scaleMax) {
        this.scaleMax = scaleMax;
        notifyDataSetChanged();
    }

    @Override
    public BarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bar, parent,
                false);
        return new BarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BarViewHolder holder, final int position) {
        holder.bar.setLayoutParams(new LinearLayoutCompat.LayoutParams(barWidth, ViewGroup
                .LayoutParams.MATCH_PARENT));

        holder.positive.setBackgroundResource(highlightedBars.contains(position) ? R.drawable
                .highlighted_bar : R.drawable.normal_bar);

        double roundedScaleMax = round(scaleMax, 0.01);
        double resolvedPositiveValue = Math.min(roundedScaleMax, round(values.get(position), 0.01));
        double resolvedNegativeValue = roundedScaleMax - resolvedPositiveValue;

        setWeight((float) resolvedPositiveValue, holder.positive);
        setWeight((float) resolvedNegativeValue, holder.negative);
    }

    private void setWeight(float weight, View view) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.weight = weight;
        view.setLayoutParams(lp);
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

    private double round(double val, double step) {
        return Math.round(val / step) * step;
    }
}
