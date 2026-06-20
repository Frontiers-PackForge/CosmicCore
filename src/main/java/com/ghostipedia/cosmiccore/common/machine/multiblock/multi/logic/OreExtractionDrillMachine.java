package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.client.gui.widget.drill.OreExtractionDrillFancyUIWidget;
import com.ghostipedia.cosmiccore.client.gui.widget.drill.OreExtractionDrillWidget;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class OreExtractionDrillMachine extends WorkableElectricMultiblockMachine {

    @Getter
    private final int tier;

    public OreExtractionDrillMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, m -> new OreExtractionDrillLogic((OreExtractionDrillMachine) m));
        this.tier = tier;
    }

    @NotNull
    @Override
    public OreExtractionDrillLogic getRecipeLogic() {
        return (OreExtractionDrillLogic) super.getRecipeLogic();
    }

    public float getRemovalChance() {
        return switch (tier) {
            case GTValues.LV -> 0.50f;
            case GTValues.HV -> 0.25f;
            case GTValues.IV -> 0.125f;
            case GTValues.ZPM -> 0.0625f;
            default -> 0.50f;
        };
    }

    public int getEffectiveYieldMultiplier() {
        return Math.round(1.0f / getRemovalChance());
    }

    public int getTierIndex() {
        return switch (tier) {
            case GTValues.LV -> 0;
            case GTValues.HV -> 1;
            case GTValues.IV -> 2;
            case GTValues.ZPM -> 3;
            default -> 0;
        };
    }

    public long getEnergyPerTick() {
        return GTValues.V[tier];
    }

    public boolean drainEnergy(boolean simulate) {
        var energyHandlers = this.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (energyHandlers == null) return false;

        var energyList = new EnergyContainerList(
                energyHandlers.stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList());

        long toDrain = getEnergyPerTick();
        if (energyList.getEnergyStored() < toDrain) {
            return false;
        }

        if (!simulate) {
            energyList.removeEnergy(toDrain);
        }
        return true;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        getRecipeLogic().invalidateCache();
        getRecipeLogic().loadStructureChunks();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        getRecipeLogic().invalidateCache();
        getRecipeLogic().releaseAllChunks();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        getRecipeLogic().releaseAllChunks();
    }

    @Override
    protected net.minecraft.world.InteractionResult onScrewdriverClick(
                                                                       com.gregtechceu.gtceu.utils.ExtendedUseOnContext context) {
        if (!isRemote()) {
            getRecipeLogic().restartDrill();
            context.getPlayer().sendSystemMessage(
                    Component.translatable("cosmiccore.machine.ore_extraction_drill.restarted"));
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public Widget createUIWidget() {
        return new OreExtractionDrillWidget(() -> this);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(OreExtractionDrillWidget.WIDTH + 16, OreExtractionDrillWidget.HEIGHT + 70, this,
                entityPlayer)
                .widget(new OreExtractionDrillFancyUIWidget(this, OreExtractionDrillWidget.WIDTH + 16,
                        OreExtractionDrillWidget.HEIGHT + 70, this::getTierIndex));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (isFormed()) {
            var logic = getRecipeLogic();

            textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick",
                    FormattingUtil.formatNumbers(getEnergyPerTick()), GTValues.VNF[tier]));

            textList.add(Component.literal(String.format("Removal Chance: %.1f%% (%dx yield)",
                    getRemovalChance() * 100f, getEffectiveYieldMultiplier())).withStyle(ChatFormatting.GOLD));

            OreExtractionDrillLogic.DrillPhase phase = logic.getPhase();
            Component phaseText = switch (phase) {
                case IDLE -> Component.literal("Idle").withStyle(ChatFormatting.GRAY);
                case SCANNING -> Component.literal(String.format("Scanning... %.1f%%", logic.getScanProgressPercent()))
                        .withStyle(ChatFormatting.YELLOW);
                case MINING -> Component.literal("Mining").withStyle(ChatFormatting.GREEN);
                case COMPLETE -> Component.literal("Complete").withStyle(ChatFormatting.AQUA);
            };
            textList.add(Component.literal("Phase: ").append(phaseText));

            if (phase == OreExtractionDrillLogic.DrillPhase.SCANNING) {
                int foundSoFar = logic.getPendingOreCount();
                textList.add(Component.literal(String.format("Ores found: %d", foundSoFar))
                        .withStyle(ChatFormatting.GRAY));
            } else if (phase == OreExtractionDrillLogic.DrillPhase.MINING) {
                int current = logic.getCurrentOreIndex();
                int total = logic.getPendingOreCount();
                int progressSec = logic.getMiningProgressSeconds();
                int totalSec = logic.getTotalMiningSeconds();
                textList.add(Component.literal(String.format("Ore: %d / %d (%ds / %ds)",
                        current + 1, total, progressSec, totalSec)).withStyle(ChatFormatting.WHITE));

                Map<String, Integer> oreCounts = logic.getOreTypeCounts();
                if (!oreCounts.isEmpty()) {
                    textList.add(Component.literal("Ore Types:").withStyle(ChatFormatting.GRAY));
                    oreCounts.entrySet().stream()
                            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                            .limit(5)
                            .forEach(entry -> {
                                MutableComponent oreName = Component.translatable(entry.getKey());
                                textList.add(Component.literal("  ")
                                        .append(oreName)
                                        .append(Component.literal(": " + entry.getValue()))
                                        .withStyle(ChatFormatting.GRAY));
                            });
                    if (oreCounts.size() > 5) {
                        textList.add(Component.literal(String.format("  ... and %d more types",
                                oreCounts.size() - 5)).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            } else if (phase == OreExtractionDrillLogic.DrillPhase.COMPLETE) {
                textList.add(Component.literal("Use screwdriver to restart scan")
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            textList.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        }
    }
}
