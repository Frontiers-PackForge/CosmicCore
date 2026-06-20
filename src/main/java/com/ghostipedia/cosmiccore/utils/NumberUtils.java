package com.ghostipedia.cosmiccore.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.text.DecimalFormat;

public class NumberUtils {

    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public static MutableComponent numberFormat(long number) {
        return Component.literal(formatLong(number));
    }

    public static String formatLong(long number) {
        if (number < 1_000) {
            return DF.format(number);
        } else if (number < 1_000_000) {
            return DF.format((double) number / 1_000.0) + "K";
        } else if (number < 1_000_000_000) {
            return DF.format((double) number / 1_000_000.0) + "M";
        } else if (number < 1_000_000_000_000L) {
            return DF.format((double) number / 1_000_000_000.0) + "G";
        } else if (number < 1_000_000_000_000_000L) {
            return DF.format((double) number / 1_000_000_000_000.0) + "T";
        } else {
            return DF.format((double) number / 1_000_000_000_000_000.0) + "P";
        }
    }

    public static String formatThousandsSeparators(double number) {
        int num_int = (int) number;
        double decimal = number - (int) number;
        StringBuilder res = new StringBuilder();
        if (0d != decimal) {
            if (0d != (int) ((100 * decimal) % 10)) {
                res.insert(0, String.format("%.2f", decimal)).deleteCharAt(0);
            } else {
                res.insert(0, String.format("%.1f", decimal)).deleteCharAt(0);
            }
        }
        while (num_int > 999) {
            res.insert(0, "," + String.format("%03d", num_int % 1000));
            num_int /= 1000;
        }
        res.insert(0, num_int);
        return res.toString();
    }
}
