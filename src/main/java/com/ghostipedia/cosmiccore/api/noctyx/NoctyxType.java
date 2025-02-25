package com.ghostipedia.cosmiccore.api.noctyx;

import static net.minecraft.util.FastColor.ARGB32.color;

public enum NoctyxType {

    ALL("ALL", color(255, 255, 255, 255)),
    TYPE_0("0", color(255, 255, 0, 255)),
    TYPE_1("1", color(255, 204, 204, 255)),
    TYPE_2("2", color(255, 127, 221, 234)),
    TYPE_3("3", color(255, 160, 255, 219)),
    TYPE_4("4", color(255, 255, 186, 105)),
    TYPE_5("5", color(255, 255, 111, 105)),
    TYPE_6("6", color(255, 43, 50, 86)),
    TYPE_7("7", color(255, 119, 22, 70));

    public final String name;
    public final int color;

    NoctyxType(String name, int color) {
        this.name = name;
        this.color = color;
    }
}
