package com.ghostipedia.cosmiccore.client.recipemaker;

import net.minecraft.client.Minecraft;

public final class RecipeMakerClipboard {

    private RecipeMakerClipboard() {}

    public static void copy(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
