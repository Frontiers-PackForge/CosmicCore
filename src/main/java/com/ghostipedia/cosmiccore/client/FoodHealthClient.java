package com.ghostipedia.cosmiccore.client;

import com.ghostipedia.cosmiccore.common.food.CosmicFoodModifiers;
import com.ghostipedia.cosmiccore.mixin.client.GuiAccessor;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class FoodHealthClient {

    private FoodHealthClient() {}

    public static void sync(double foodHealthBonus) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        if (!CosmicFoodModifiers.applyMaxHealth(minecraft.player, foodHealthBonus)) return;
        int health = Mth.ceil(minecraft.player.getHealth());
        GuiAccessor gui = (GuiAccessor) minecraft.gui;
        gui.cosmiccore$setLastHealth(health);
        gui.cosmiccore$setDisplayHealth(health);
        gui.cosmiccore$setLastHealthTime(Util.getMillis());
        gui.cosmiccore$setHealthBlinkTime(0);
    }
}
