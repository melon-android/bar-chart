package com.melontech.barchart.lib;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dean Panayotov on 10/27/2016
 */

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