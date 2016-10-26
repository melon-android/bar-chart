package com.melontech.barchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.melontech.barchart.lib.MelonBarChart;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class BarChartActivity extends AppCompatActivity {

    private List<Double> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        final MelonBarChart chart = (MelonBarChart) findViewById(R.id.chartB);
        final Random random = new Random();

        findViewById(R.id.animate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values.add(random.nextDouble() * 24);
                values.add(random.nextDouble() * 12);
                chart.setValues(values);
            }
        });

        chart.setDashedGridLines(fillDashedLines());

        values = fillFakeData();
        //chart.setValues(values);
    }

    private List<Double> fillFakeData() {
        List<Double> values = new ArrayList<>();

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
        values.add(4d);
        values.add(6d);
        values.add(2d);
//        values.add(8d);
//        values.add(8d);
//        values.add(7d);
//        values.add(0d);
//        values.add(13d);
//        values.add(30d);
//        values.add(10d);
//        values.add(7d);
//        values.add(15d);
//        values.add(5d);
//        values.add(2d);
//        values.add(4d);
//        values.add(6d);
//        values.add(8d);
        return values;
    }

    private Set<Integer> fillDashedLines() {
        Set<Integer> dashedLines = new HashSet<>();
        dashedLines.add(3);
        return dashedLines;
    }
}
