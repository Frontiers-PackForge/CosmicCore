package com.ghostipedia.cosmiccore.gtbridge;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.gui.CapabilityContentBuilder;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeViewerCapabilityLayoutBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.WidgetTree;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

public final class EmberRecipeUI {

    private EmberRecipeUI() {}

    public static final RecipeViewerCapabilityLayoutBuilder LAYOUT = (layout, widget, io) -> {
        if (layout.getRecipeType().getMaxSlots(EmberRecipeCapability.CAP, io) == 0) return;
        widget.textComponents.child(Flow.col().childPadding(1).coverChildrenHeight().widthRel(1f)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .name(GTRecipeViewerWidget.capabilityWidgetName(EmberRecipeCapability.CAP, io, 0)));
    };

    public static final CapabilityContentBuilder CONTENT = (widget, content, io, perTick, recipeType, recipe,
                                                            chanceTier, recipeTier) -> {
        if (!(widget instanceof Flow flow)) return;
        long amount = EmberRecipeCapability.CAP.of(content.content()).longValue();
        String name = io == IO.IN ? "ember_in" : "ember_out";
        var text = Text.lang(io == IO.IN ? "cosmiccore.recipe.ember_in" : "cosmiccore.recipe.ember_out",
                FormattingUtil.formatNumbers(amount)).withStyle(ChatFormatting.GOLD);
        var existing = WidgetTree.findFirstWithNameNullable(flow, name);
        if (existing != null) ((TextWidget<?>) existing).value(text);
        else flow.child(text.asWidget().name(name));
    };
}
