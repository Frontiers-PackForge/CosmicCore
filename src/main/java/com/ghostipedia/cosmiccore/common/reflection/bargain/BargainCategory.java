package com.ghostipedia.cosmiccore.common.reflection.bargain;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Categories for bargains - used by Soul Shape affinity system.
 * Each Soul Shape empowers certain categories and curses others.
 */
public enum BargainCategory {

    MOBILITY("mobility", ChatFormatting.AQUA),
    DEFENSE("defense", ChatFormatting.GRAY),
    OFFENSE("offense", ChatFormatting.RED),
    UTILITY("utility", ChatFormatting.YELLOW),
    SUSTENANCE("sustenance", ChatFormatting.GREEN),
    DEATH("death", ChatFormatting.DARK_RED);

    private final String id;
    private final ChatFormatting color;

    BargainCategory(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getNameKey() {
        return "cosmiccore.bargain_category." + id;
    }

    public MutableComponent getFormattedName() {
        return Component.translatable(getNameKey()).withStyle(color);
    }

    public static BargainCategory fromId(String id) {
        for (BargainCategory cat : values()) {
            if (cat.id.equals(id)) {
                return cat;
            }
        }
        return UTILITY;
    }
}
