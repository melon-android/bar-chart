package com.melontech.barchart.lib;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MelonBarChart extends LinearLayout {

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
    }
}
