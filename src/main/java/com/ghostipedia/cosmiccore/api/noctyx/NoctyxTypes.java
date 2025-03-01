package com.ghostipedia.cosmiccore.api.noctyx;

public class NoctyxTypes {

    public static final String langPrefix = "cosmiccore.noctyx.type.";

    public static String prefix(String str) {
        return langPrefix + str;
    }

    public static final NoctyxType EMPTY = new NoctyxType(prefix("empty"), 0, 0, 0, 0);
    public static final NoctyxType ALL = new NoctyxType(prefix("all"), 1, 1, 1, 1);
    public static final NoctyxType TYPE_0 = new NoctyxType(prefix("0"), 1, 1, 0, 1);
    public static final NoctyxType TYPE_1 = new NoctyxType(prefix("1"), 1, .8f, .8f, 1);
    public static final NoctyxType TYPE_2 = new NoctyxType(prefix("2"), 1, .5f, .86f, .92f);
    public static final NoctyxType TYPE_3 = new NoctyxType(prefix("3"), 1, .63f, 1, .86f);
    public static final NoctyxType TYPE_4 = new NoctyxType(prefix("4"), 1, 1, .72f, .41f);
    public static final NoctyxType TYPE_5 = new NoctyxType(prefix("5"), 1, 1, .43f, .41f);
    public static final NoctyxType TYPE_6 = new NoctyxType(prefix("6"), 1, .185f, .2f, .33f);
    public static final NoctyxType TYPE_7 = new NoctyxType(prefix("7"), 1, .46f, .08f, .27f);
}
