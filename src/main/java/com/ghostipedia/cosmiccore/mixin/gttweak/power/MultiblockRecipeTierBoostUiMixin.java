package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IRecipeTierBoostMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class MultiblockRecipeTierBoostUiMixin {

    @Inject(method = "getWidgetsForDisplay", at = @At("RETURN"))
    private void cosmiccore$replaceEnergyDisplay(PanelSyncManager syncManager,
                                                 CallbackInfoReturnable<List<IWidget>> cir) {
        WorkableElectricMultiblockMachine machine = (WorkableElectricMultiblockMachine) (Object) this;
        if (machine.isGenerator() || !(machine instanceof IRecipeTierBoostMachine tierBoostMachine)) return;

        List<IWidget> widgets = cir.getReturnValue();
        if (widgets.size() < 2) return;
        widgets.remove(1);
        widgets.remove(0);
        widgets.add(0, cosmiccore$createRecipeTierLine(syncManager, tierBoostMachine, machine));
        widgets.add(1, cosmiccore$createMaximumThroughputDisplay(syncManager, tierBoostMachine, machine));
    }

    private static IWidget cosmiccore$createRecipeTierLine(PanelSyncManager syncManager,
                                                           IRecipeTierBoostMachine tierBoostMachine,
                                                           WorkableElectricMultiblockMachine machine) {
        BooleanSyncValue formed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(machine::isFormed));
        IntSyncValue tier = syncManager.getOrCreateSyncHandler("energyTier", IntSyncValue.class,
                () -> new IntSyncValue(machine::getTier));
        BooleanSyncValue applied = syncManager.getOrCreateSyncHandler("cosmiccoreTierBoostApplied",
                BooleanSyncValue.class,
                () -> new BooleanSyncValue(() -> tierBoostMachine.getRecipeTierBoostState().boostApplied()));
        IntSyncValue inputTier = syncManager.getOrCreateSyncHandler("cosmiccoreTierBoostInputTier",
                IntSyncValue.class,
                () -> new IntSyncValue(() -> tierBoostMachine.getRecipeTierBoostState().inputTier()));
        IntSyncValue sourceCount = syncManager.getOrCreateSyncHandler("cosmiccoreTierBoostSourceCount",
                IntSyncValue.class,
                () -> new IntSyncValue(() -> tierBoostMachine.getRecipeTierBoostState()
                        .highestInputContainerCount()));
        LongSyncValue sourceAmperage = syncManager.getOrCreateSyncHandler("cosmiccoreTierBoostSourceAmperage",
                LongSyncValue.class,
                () -> new LongSyncValue(() -> tierBoostMachine.getRecipeTierBoostState()
                        .highestInputAmperage()));

        var tierText = Text.dynamic(() -> Component.translatable("gtceu.multiblock.max_recipe_tier",
                Component.literal(GTValues.VNF[tier.getIntValue()]))
                .withStyle(ChatFormatting.GRAY)).asWidget();
        RichTooltip tooltip = new RichTooltip().autoUpdate(true).tooltipBuilder(builder -> {
            builder.addLine(Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                    .withStyle(ChatFormatting.GRAY));
            if (!applied.getBoolValue()) return;
            Component inputTierName = Component.literal(GTValues.VNF[inputTier.getIntValue()]);
            Component source = sourceCount.getIntValue() == 1 ?
                    Component.translatable("cosmiccore.multiblock.tier_boost.source.single",
                            FormattingUtil.formatNumbers(sourceAmperage.getLongValue()), inputTierName) :
                    Component.translatable("cosmiccore.multiblock.tier_boost.source.multiple",
                            sourceCount.getIntValue(), inputTierName);
            builder.addLine(Component.translatable("cosmiccore.multiblock.tier_boost.applied_by", source)
                    .withStyle(ChatFormatting.GRAY));
            builder.addLine(Component.translatable("cosmiccore.multiblock.tier_boost.hover")
                    .withStyle(ChatFormatting.GRAY));
        });

        return Flow.row()
                .coverChildren()
                .childPadding(2)
                .child(tierText)
                .child(GTGuiTextures.INFO.asWidget().size(9).tooltip(tooltip))
                .setEnabledIf(widget -> formed.getBoolValue());
    }

    private static IWidget cosmiccore$createMaximumThroughputDisplay(PanelSyncManager syncManager,
                                                                     IRecipeTierBoostMachine tierBoostMachine,
                                                                     WorkableElectricMultiblockMachine machine) {
        BooleanSyncValue formed = syncManager.getOrCreateSyncHandler("isFormed", BooleanSyncValue.class,
                () -> new BooleanSyncValue(machine::isFormed));
        LongSyncValue throughput = syncManager.getOrCreateSyncHandler("cosmiccoreMaximumThroughput",
                LongSyncValue.class,
                () -> new LongSyncValue(() -> tierBoostMachine.getRecipeTierBoostState().maximumThroughput()));
        return Flow.column()
                .coverChildren()
                .child(Text.lang("cosmiccore.multiblock.maximum_throughput").style(ChatFormatting.GRAY).asWidget())
                .child(Text.dynamic(() -> Component.translatable("cosmiccore.multiblock.maximum_throughput.value",
                        FormattingUtil.formatNumbers(throughput.getLongValue())).withStyle(ChatFormatting.GRAY))
                        .asWidget()
                        .marginLeft(6))
                .setEnabledIf(widget -> formed.getBoolValue());
    }
}
