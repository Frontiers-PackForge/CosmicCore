package com.ghostipedia.cosmiccore.common.item.behavior;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.io.Serial;

/**
 *
 * This enum exists to add solvent to colors
 */
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

    private final DyeColor dyeColor;

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
        if (dyeColor == null) return SOLVENT;

        for (ExtendedDyeColor colors : values()) {
            if (colors.dyeColor == dyeColor) {
                return colors;
            }
        }
        return SOLVENT;
    }

    public ResourceTexture getButtonTexture() {
        String SprayColor = isSolvent()
                ? "white_spray_can.png"
                : String.format("%s_spray_can.png", dyeColor.getName().toLowerCase());

        ResourceLocation textureLocation = new ResourceLocation("cosmiccore", "item/" + SprayColor);
        return new ResourceTexture(textureLocation);
    }

    // Return the DyeColor ID or -1 if there's no dye color
    public int getColorId() {
        return dyeColor != null ? dyeColor.getId() : -1;
    }
}
