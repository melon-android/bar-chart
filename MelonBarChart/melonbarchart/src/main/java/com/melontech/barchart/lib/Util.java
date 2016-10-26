package com.melontech.barchart.lib;

import android.content.res.Resources;

/**
 * Created by Dean Panayotov on 10/26/2016
 */

public class Util {
    public static double round(double val, double step) {
        return Math.round(val / step) * step;
    }

    public static double ceil(double input, double step) {
        return Math.ceil(input / step) * step;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

}
