package com.ghostipedia.cosmiccore;

import static net.minecraft.ChatFormatting.*;

public class Statics {


    public static final double DEFAULT_PRESSURE = 10;

    /**
     * The main pressure thresholds
     */
    public static final double[] P = new double[]{
            10,
            20,
            30,
            40,
            50,
            60,
            70,
            80,
            90,
            100,
            110
    };

    public static final int EV = 0;
    public static final int IV = 1;
    public static final int LUV = 2;
    public static final int ZPM = 3;
    public static final int UV = 4;
    public static final int UHV = 5;
    public static final int UEV = 6;
    public static final int UIV = 7;
    public static final int UXV = 8;
    public static final int OPV = 9;
    public static final int MAX = 10;

    /**
     * The short names of each pressure threshold
     */
    public static final String[] PN = new String[]{
            "EV",
            "IV",
            "LUV",
            "ZPM",
            "UV",
            "UHV",
            "UEV",
            "UIV",
            "uxv",
            "OPV",
            "MAX"
    };

    /**
     * The decorated names of each pressure threshold
     */
    public static final String[] PNF = new String[]{
            BLUE + "EV",
            DARK_GREEN + "IV",
            DARK_RED + "LUV",
            WHITE + "ZPM",
            DARK_BLUE + "UV",
            GOLD + "UHV",
            GRAY + "UEV",
            DARK_GRAY + "UIV",
            DARK_GRAY + "UXV",
            AQUA + "OPV",
            DARK_PURPLE + "MAX"
    };

    /**
     * The full names of each pressure threshold
     */
    public static final String[] PRESSURE_NAMES = new String[]{
            "EV",
            "IV",
            "LUV",
            "ZPM",
            "UV",
            "UHV",
            "UEV",
            "UIV",
            "UXV",
            "OPV",
            "MAX"
    };

}
