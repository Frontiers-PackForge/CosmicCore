package com.ghostipedia.cosmiccore.common.item;

import net.minecraft.util.StringRepresentable;

import forestry.api.core.IItemSubtype;

import java.awt.*;
import java.util.Locale;

public enum CosmicBeesHoneyComb implements StringRepresentable, IItemSubtype {

    ROCKY(new Color(0x2B2BFF), new Color(0x9B0738)),
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
