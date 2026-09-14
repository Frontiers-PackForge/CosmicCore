package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.FluidDrillingArea;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class FluidDrillingUiMixin {

    @Inject(method = "getWidgetsForDisplay", at = @At("RETURN"))
    private void cosmiccore$areaControl(PanelSyncManager syncManager, CallbackInfoReturnable<List<IWidget>> cir) {
        if (!((Object) this instanceof FluidDrillMachine drill)) return;
        var area = (FluidDrillingArea) drill.getRecipeLogic();
        var expanded = new BooleanSyncValue(area::cosmiccore$isExpanded, area::cosmiccore$setExpanded).allowC2S();
        syncManager.syncValue("cosmiccore_drill_area", expanded);
        int diameter = 2 * Math.clamp(drill.getTier() - GTValues.MV + 1, 1, 3) + 1;
        cir.getReturnValue().add(new ToggleButton().size(162, 18).value(expanded)
                .overlay(Text.dynamic(() -> Component.translatable("cosmiccore.fluid_drill.area",
                        expanded.getBoolValue() ? diameter : 1, expanded.getBoolValue() ? diameter : 1))));
        var output = GenericSyncValue.<RegistryFriendlyByteBuf, Component>builder(Component.class)
                .getter(() -> {
                    var lines = Component.empty();
                    for (var fluid : area.cosmiccore$getOutputs()) {
                        if (!lines.getSiblings().isEmpty()) lines.append("\n");
                        lines.append(Component.translatable("cosmiccore.fluid_drill.output",
                                fluid.getHoverName(), fluid.getAmount()));
                    }
                    return lines;
                }).adapter(ByteBufAdapters.COMPONENT).copy(Component::copy).equalsDefault().build();
        syncManager.syncValue("cosmiccore_drill_outputs", output);
        cir.getReturnValue().add(Text.dynamic(output::getValue).asWidget().width(162));
    }
}
