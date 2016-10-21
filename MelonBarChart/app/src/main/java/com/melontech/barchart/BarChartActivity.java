package com.melontech.barchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.melontech.barchart.lib.MelonBarChart;

public class BarChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        findViewById(R.id.animate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MelonBarChart)findViewById(R.id.chartB)).animateBars();
            }
        });
    }
}
