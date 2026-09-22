package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;

import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.widgets.TextWidget;

public final class VitaeCampusRecipeUI {

    public static final RecipeUIModifier ALTAR_TIER = (recipe, widget) -> {
        int tier = recipe.data.contains(ImbumentPylonMachine.ALTAR_TIER_KEY) ?
                recipe.data.getInt(ImbumentPylonMachine.ALTAR_TIER_KEY) : 4;
        widget.textComponents.child(new TextWidget<>(Text.lang(
                "cosmiccore.vitae_campus.recipe.altar_tier", tier)).color(0xFFFFFFFF));
    };

    private VitaeCampusRecipeUI() {}
}
