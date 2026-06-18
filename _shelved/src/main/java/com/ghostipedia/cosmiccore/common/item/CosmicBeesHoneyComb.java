package com.ghostipedia.cosmiccore.common.item;

import net.minecraft.util.StringRepresentable;

import forestry.api.core.IItemSubtype;

import java.awt.*;
import java.util.Locale;

public enum CosmicBeesHoneyComb implements StringRepresentable, IItemSubtype {

    LOFTY_OXYGEN(new Color(0x8080FF), new Color(0x4242FF)),
    LOFTY_HYDROGEN(new Color(0x80FFE1), new Color(0x4242FF)),
    LOFTY_NITROGEN(new Color(0xFF80F9), new Color(0x4242FF)),
    LOFTY_ARGON(new Color(0x97FF80), new Color(0x4242FF)),

    ROSE_POLYMER(new Color(0xFF4E6F), new Color(0x5D5D5D)),
    CITRUS_POLYMER(new Color(0xFF9900), new Color(0x5D5D5D)),
    WAXY_POLYMER(new Color(0xA100FF), new Color(0x5D5D5D)),

    BIOHAZARD(new Color(0x00FF33), new Color(0x082C00)),
    PALE(new Color(0xC8E7F1), new Color(0x3F3F3F)),
    SOUL(new Color(0x3FEBF1), new Color(0x3A3A3A)),
    RUNIC(new Color(0xA68941), new Color(0xA2A2A2)),
    AMBROSIC(new Color(0xD7C238), new Color(0x314234)),

    ABRASIVE(new Color(0x834500), new Color(0x312E2B)),
    ENERGIZED(new Color(0xD7C238), new Color(0x312E2B)),
    SLICK(new Color(0x251531), new Color(0x312E2B)),
    PYROLYTIC(new Color(0x5B4B3F), new Color(0x312E2B)),
    LUNAR(new Color(0x10735F), new Color(0x223149)),
    SOLAR(new Color(0xF3DC4C), new Color(0x223149)),
    COSMOS(new Color(0xA276CB), new Color(0x223149)),

    HADAL(new Color(0x01015B), new Color(0x000000)),
    SHAMAN(new Color(0xE0099B), new Color(0x720303)),
    ASHEN(new Color(0x6D6872), new Color(0x223149)),
    FRACKING(new Color(0xDAD3B8), new Color(0x223149)),
    FATE(new Color(0x06B64D), new Color(0x193D05)),
    GRAND_GARDEN(new Color(0x42801D), new Color(0x1D5703)),
    ARCHITECT(new Color(0xD0FFE9), new Color(0x003588)),
    INQUISITIVE(new Color(0xDCA260), new Color(0x666C77)),
    HELLSMITH(new Color(0xEA5555), new Color(0x640529)),
    RADOXIA(new Color(0x4D0E88), new Color(0x55647E)),
    ABSENT(new Color(0xE1E1E1), new Color(0x9A9A9A)),
    ILLUSIVE(new Color(0xEFFAAC), new Color(0x7C92B6)),
    CONSTRUCTIVE(new Color(0xCBA676), new Color(0x464922)),
    PRISMATIC(new Color(0x76CB87), new Color(0x436CAD)),
    HYDRAULIC(new Color(0x715F81), new Color(0x0F521F)),
    COBBLED(new Color(0x6A6272), new Color(0x323741)),
    EXHAUSTIVE(new Color(0x4F625B), new Color(0x3A4350)),
    VIRTUE(new Color(0x6BD1D5), new Color(0x223149)),

    ;

    public static final CosmicBeesHoneyComb[] VALUES = values();

    public final String name;
    public final int primaryColor;
    public final int secondaryColor;

    CosmicBeesHoneyComb(Color primary, Color secondary) {
        this(primary, secondary, null);
    }

    CosmicBeesHoneyComb(Color primary, Color secondary, String compatName) {
        this.name = toString().toLowerCase(Locale.ENGLISH);
        this.primaryColor = primary.getRGB();
        this.secondaryColor = secondary.getRGB();
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static CosmicBeesHoneyComb get(int meta) {
        if (meta >= VALUES.length) {
            meta = 0;
        }
        return VALUES[meta];
    }
}
