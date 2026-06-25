package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public final class RecipeMakerTextures {

    public static final IGuiTexture BACKGROUND = ResourceBorderTexture.BORDERED_BACKGROUND;
    public static final IGuiTexture VANILLA_BUTTON = ResourceBorderTexture.BUTTON_COMMON;
    public static final IGuiTexture SLOT = new ResourceTexture("ldlib:textures/gui/slot.png");
    public static final IGuiTexture FLUID_SLOT = new ResourceTexture("ldlib:textures/gui/fluid_slot.png");

    private RecipeMakerTextures() {}
}
