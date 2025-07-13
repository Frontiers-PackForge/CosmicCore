package com.ghostipedia.cosmiccore.common.item.behavior;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

import net.minecraft.world.item.DyeColor;

import lombok.Getter;

/**
 *
 * This enum exists to add solvent to colors
 */
public enum ExtendedDyeColor {

    WHITE(DyeColor.WHITE, "white_dye_spray_can.png"),
    ORANGE(DyeColor.ORANGE, "orange_dye_spray_can.png"),
    MAGENTA(DyeColor.MAGENTA, "magenta_dye_spray_can.png"),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, "light_blue_dye_spray_can.png"),
    YELLOW(DyeColor.YELLOW, "yellow_dye_spray_can.png"),
    LIME(DyeColor.LIME, "lime_dye_spray_can.png"),
    PINK(DyeColor.PINK, "pink_dye_spray_can.png"),
    GRAY(DyeColor.GRAY, "gray_dye_spray_can.png"),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, "light_gray_dye_spray_can.png"),
    CYAN(DyeColor.CYAN, "cyan_dye_spray_can.png"),
    PURPLE(DyeColor.PURPLE, "purple_dye_spray_can.png"),
    BLUE(DyeColor.BLUE, "blue_dye_spray_can.png"),
    BROWN(DyeColor.BROWN, "brown_dye_spray_can.png"),
    GREEN(DyeColor.GREEN, "green_dye_spray_can.png"),
    RED(DyeColor.RED, "red_dye_spray_can.png"),
    BLACK(DyeColor.BLACK, "black_dye_spray_can.png"),
    SOLVENT(null, "white_dye_spray_can.png");

    private final DyeColor dyeColor;
    @Getter
    public final ResourceTexture texture;

    ExtendedDyeColor(DyeColor dyeColor, String resloc) {
        this.dyeColor = dyeColor;
        this.texture = new ResourceTexture("cosmiccore:textures/item/" + resloc);
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
        if (dyeColor == null) return SOLVENT;

        for (ExtendedDyeColor colors : values()) {
            if (colors.dyeColor == dyeColor) {
                return colors;
            }
        }
        return SOLVENT;
    }

    // Return the DyeColor ID or -1 if there's no dye color
    public int getColorId() {
        return dyeColor != null ? dyeColor.getId() : -1;
    }
}
