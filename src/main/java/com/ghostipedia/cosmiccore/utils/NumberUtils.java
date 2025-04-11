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

    public static int mapRange(int in, int inStart, int inEnd, int outStart, int outEnd) {
        return outStart + ((outEnd - outStart) / (inEnd - inStart)) * (in - inStart);
    }

    public static long mapRange(long in, long inStart, long inEnd, long outStart, long outEnd) {
        return outStart + ((outEnd - outStart) / (inEnd - inStart)) * (in - inStart);
    }

    public static float mapRange(float in, float inStart, float inEnd, float outStart, float outEnd) {
        return outStart + ((outEnd - outStart) / (inEnd - inStart)) * (in - inStart);
    }

    public static double mapRange(double in, double inStart, double inEnd, double outStart, double outEnd) {
        return outStart + ((outEnd - outStart) / (inEnd - inStart)) * (in - inStart);
    }
}
