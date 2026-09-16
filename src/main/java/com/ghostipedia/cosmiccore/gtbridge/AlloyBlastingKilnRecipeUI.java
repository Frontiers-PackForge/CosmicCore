package com.ghostipedia.cosmiccore.gtbridge;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;

import java.util.List;

public final class AlloyBlastingKilnRecipeUI {

    public static final RecipeUIModifier COIL_INFO = (recipe, widget) -> {
        if (!recipe.data.contains("ebf_temp")) return;

        int temperature = recipe.data.getInt("ebf_temp");
        widget.textComponents.child(new TextWidget<>(
                Text.lang("gtceu.recipe.temperature", FormattingUtil.formatTemperature(temperature))));

        Flow coilRow = Flow.row().coverChildrenHeight(18);
        ICoilType requiredCoil = ICoilType.getMinRequiredType(temperature);
        if (requiredCoil != null && !requiredCoil.getMaterial().isNull()) {
            coilRow.child(new TextWidget<>(Text.lang("gtceu.recipe.coil.tier",
                    Component.translatable(requiredCoil.getMaterial().getUnlocalizedName()).getString())));
        }
        widget.textComponents.child(coilRow);

        List<ItemStack> items = GTCEuAPI.HEATING_COILS.entrySet().stream()
                .filter(coil -> coil.getKey().getCoilTemperature() >= temperature)
                .map(coil -> new ItemStack(coil.getValue().get()))
                .toList();
        widget.textComponents.child(RecipeViewerSlotWidget.create()
                .recipeSlotRole(RecipeSlotRole.RENDER_ONLY)
                .value(ItemStackList.of(items))
                .background(IDrawable.EMPTY)
                .decoration()
                .right(23)
                .bottom(-3));
    };

    private AlloyBlastingKilnRecipeUI() {}
}
