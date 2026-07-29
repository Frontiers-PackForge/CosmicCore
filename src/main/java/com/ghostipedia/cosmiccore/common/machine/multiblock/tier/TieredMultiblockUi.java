package com.ghostipedia.cosmiccore.common.machine.multiblock.tier;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IIntValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widgets.CycleButtonWidget;

import java.util.List;
import java.util.function.IntSupplier;

public final class TieredMultiblockUi {

    private TieredMultiblockUi() {}

    public static CycleButtonWidget createTierButton(MultiblockMachineDefinition definition, IIntValue<?> value,
                                                     IntSupplier streakSupplier, int size) {
        CycleButtonWidget button = new CycleButtonWidget()
                .background(GuiTextures.MC_BUTTON)
                .hoverBackground(GuiTextures.MC_BUTTON_HOVERED)
                .stateCount(TieredMultiblockPatterns.tierCount(definition))
                .value(value)
                .size(size)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(
                            Component.translatable("cosmiccore.multiblock.structure_tier", value.getIntValue() + 1));
                    if (definition == GTMultiMachines.ELECTRIC_BLAST_FURNACE && value.getIntValue() > 0) {
                        tooltip.addLine(Component.translatable("cosmiccore.multiblock.ebf.streak",
                                ElectricBlastFurnaceTierState.durationReductionPercent(streakSupplier.getAsInt())));
                        tooltip.addLine(Component.translatable("cosmiccore.multiblock.ebf.streak.rule"));
                    }
                });
        for (int tier = 0; tier < TieredMultiblockPatterns.tierCount(definition); tier++) {
            button.stateOverlay(tier, Text.str("T" + (tier + 1)).alignment(Alignment.TopLeft).asTextIcon());
        }
        return button;
    }

    public static List<IWidget> createEbfBonusLines(IntSupplier tierSupplier, IntSupplier streakSupplier) {
        var streakLine = Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.ebf.streak.progress",
                Math.min(ElectricBlastFurnaceTierState.MAX_MATCHING_RUNS, streakSupplier.getAsInt()),
                ElectricBlastFurnaceTierState.MAX_MATCHING_RUNS).withStyle(ChatFormatting.GRAY))
                .asWidget()
                .setEnabledIf(widget -> tierSupplier.getAsInt() > 0);
        var reductionLine = Text.dynamic(() -> Component.translatable(
                "cosmiccore.multiblock.ebf.streak",
                ElectricBlastFurnaceTierState.durationReductionPercent(streakSupplier.getAsInt()))
                .withStyle(ChatFormatting.AQUA))
                .asWidget()
                .setEnabledIf(widget -> tierSupplier.getAsInt() > 0);
        return List.of(streakLine, reductionLine);
    }
}
