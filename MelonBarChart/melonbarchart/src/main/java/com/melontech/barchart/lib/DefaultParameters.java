package com.melontech.barchart.lib;

import android.content.Context;
import android.graphics.Color;

/**
 * Created by Dean Panayotov on 10/27/2016
 */

class DefaultParameters extends Parameters {

    static DefaultParameters instance = new DefaultParameters();

    public DefaultParameters(){
        fixedDataSetSize = 0;
        scaleStep = 0f;
        absoluteScaleMax = 0f;
        minimumScaleMax = 0f;

        labelColor = Color.parseColor("#ffffff");
        labelBackground = R.drawable.label_frame;
        labelMarginBottom = Util.dpToPx(2);
        labelFormat = "%.2d";

        gridLineColor = Color.parseColor("#2A454E");
        gridLineDashedColor = Color.parseColor("#566C73");

        barAccentHeight = -1;
        barAccentColor = -1;
        barBackground = -1;
        highlightedBarBackground = -1;

        title = "Title Placeholder";
        titleColor = Color.parseColor("#ffffff");

        overlayText = "We currently don't have enough data to show your graph. Just" +
                " wait for it while using the application.";
        overlayTextColor = Color.parseColor("#ffffff");
        overlayBackgroundColor = Color.parseColor("#15323C");
        overlayBackgroundOpacity = 0.9f;
    }
}
