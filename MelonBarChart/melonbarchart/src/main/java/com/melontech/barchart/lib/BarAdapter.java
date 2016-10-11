package com.melontech.barchart.lib;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dpanayotov on 10/11/2016.
 *
 */

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.BarViewHolder> {

    private List<Double> values = new ArrayList<>();

    public BarAdapter(List<Double> values){
        this.values.addAll(values);
    }

    @Override
    public BarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bar, parent, false);
        return new BarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BarViewHolder holder, int position) {
        holder.positive.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, (float) values.get(position).doubleValue()));
        holder.negative.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, (float) (20-values.get(position))));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }

    public class BarViewHolder extends RecyclerView.ViewHolder {

        View positive;
        View negative;

        public BarViewHolder(View itemView) {
            super(itemView);
            positive = itemView.findViewById(R.id.positive);
            negative = itemView.findViewById(R.id.negative);
        }
    }
}
