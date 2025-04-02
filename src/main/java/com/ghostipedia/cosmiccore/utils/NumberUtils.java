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


}
