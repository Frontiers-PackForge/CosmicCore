package com.ghostipedia.cosmiccore.common.item.behavior;

import net.minecraft.world.item.DyeColor;

public enum ExtendedDyeColor {

    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK),
    SOLVENT(null);

    private DyeColor dyeColor;
    public static final ExtendedDyeColor[] VALUES = ExtendedDyeColor.values();

    ExtendedDyeColor(DyeColor dyeColor) {
        this.dyeColor = dyeColor;
    }

    public static ExtendedDyeColor getColorFromDyeId(int dyeID) {
        return fromDyeColor(DyeColor.byId(dyeID));
    }

    public DyeColor getColor() {
        return dyeColor;
    }

    public boolean isSolvent() {
        return this == SOLVENT;
    }

    public int getTextColor() {
        return dyeColor != null ? dyeColor.getTextColor() : -1;
    }

    public String getSerializedName() {
        return this.name();
    }

    public static ExtendedDyeColor fromDyeColor(DyeColor dyeColor) {
        for (var extendedDyeColor : VALUES)
            if (extendedDyeColor.getColor() == dyeColor) {
                return extendedDyeColor;
            }

        return SOLVENT;
    }
    public int getColorId() {
        return dyeColor != null ? dyeColor.getId() : 0;  // Return the DyeColor ID or -1 if there's no dye color
    }

}
