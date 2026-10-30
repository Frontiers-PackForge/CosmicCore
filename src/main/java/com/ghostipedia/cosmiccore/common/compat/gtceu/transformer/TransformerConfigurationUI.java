package com.ghostipedia.cosmiccore.common.compat.gtceu.transformer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;

public final class TransformerConfigurationUI {

    private TransformerConfigurationUI() {}

    public static void build(TransformerMachine machine, ParentWidget<?> mainWidget, PosGuiData guiData,
                             PanelSyncManager syncManager, UISettings settings) {
        ConfigurableTransformer configurable = (ConfigurableTransformer) machine;
        int inputTier = machine.getTier() + 1;
        int maximumTransitions = TransformerStepDownPolicy.maxTransitions(inputTier);

        IntSyncValue transitions = new IntSyncValue(
                () -> configurable.cosmiccore$getStepDownTransitions() - 1,
                value -> configurable.cosmiccore$setStepDownTransitions(value + 1)).allowC2S();
        syncManager.syncValue("cosmiccoreTransformerTransitions", transitions);

        IntSyncValue amperage = new IntSyncValue(
                configurable::cosmiccore$getStepDownOutputAmperage,
                configurable::cosmiccore$setStepDownOutputAmperage).allowC2S();
        syncManager.syncValue("cosmiccoreTransformerAmperage", amperage);

        var tierSelector = syncManager.syncedPanel("cosmiccoreTransformerTierSelector", false,
                (manager, handler) -> new Dialog<>("cosmiccoreTransformerTierSelector")
                        .disablePanelsBelow(false)
                        .closeOnOutOfBoundsClick(true)
                        .size(100, 16 + maximumTransitions * 22)
                        .child(Flow.column().pos(8, 8).width(84).coverChildrenHeight()
                                .children(maximumTransitions, index -> new ButtonWidget<>()
                                        .size(84, 20).marginBottom(2)
                                        .overlay(Text.str(GTValues.VNF[inputTier - index - 1]))
                                        .onMousePressed((context, button) -> {
                                            if (button != 0 || machine.isTransformUp()) return false;
                                            transitions.setValue(index);
                                            handler.closePanel();
                                            return true;
                                        }))));
        var tierButton = new ButtonWidget<>()
                .background(GTGuiTextures.BUTTON)
                .overlay(Text.dynamic(() -> Component.literal(
                        GTValues.VNF[inputTier - transitions.getIntValue() - 1])))
                .pos(140, 51).size(82, 18)
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    if (tierSelector.isPanelOpen()) tierSelector.closePanel();
                    else tierSelector.openPanel();
                    return true;
                });
        tierButton.setEnabledIf(widget -> !machine.isTransformUp());

        TextFieldWidget amperageField = new TextFieldWidget()
                .setNumbers(() -> 1, configurable::cosmiccore$getMaximumStepDownOutputAmperage)
                .value(amperage)
                .autoUpdateOnChange(false)
                .pos(140, 75)
                .size(82, 18);
        amperageField.setEnabledIf(widget -> !machine.isTransformUp());

        ParentWidget<?> panel = new ParentWidget<>()
                .background(GTGuiTextures.DISPLAY)
                .size(230, 150)
                .center()
                .child(Text.dynamic(() -> Component.translatable(
                        machine.isTransformUp() ? "cosmiccore.machine.transformer.mode_up" :
                                "cosmiccore.machine.transformer.mode_down"))
                        .asWidget().color(0xFFFFFF).pos(8, 7).size(214, 10))
                .child(Text.dynamic(() -> Component.translatable(
                        "cosmiccore.machine.transformer.input_rating",
                        cosmiccore$tierName(machine.energyContainer.getInputVoltage()),
                        FormattingUtil.formatNumbers(machine.energyContainer.getInputVoltage()),
                        machine.energyContainer.getInputAmperage()))
                        .asWidget().color(0xFFFFFF).pos(8, 20).size(214, 10))
                .child(Text.dynamic(() -> Component.translatable(
                        "cosmiccore.machine.transformer.output_rating",
                        cosmiccore$tierName(machine.energyContainer.getOutputVoltage()),
                        FormattingUtil.formatNumbers(machine.energyContainer.getOutputVoltage()),
                        machine.energyContainer.getOutputAmperage()))
                        .asWidget().color(0xFFFFFF).pos(8, 32).size(214, 10))
                .child(Text.lang("cosmiccore.machine.transformer.output_tier")
                        .asWidget().color(0xFFFFFF).pos(8, 55).size(124, 10))
                .child(tierButton)
                .child(Text.lang("gtceu.creative.energy.amperage")
                        .asWidget().color(0xFFFFFF).pos(8, 79).size(124, 10))
                .child(Text.dynamic(() -> Component.translatable(
                        "cosmiccore.machine.transformer.output_amperage",
                        configurable.cosmiccore$getMaximumStepDownOutputAmperage()))
                        .asWidget().color(0xCCCCCC).pos(8, 99).size(214, 10))
                .child(amperageField)
                .child(Text.lang("cosmiccore.machine.transformer.saved_step_down")
                        .asWidget().color(0xFFFFFF).pos(8, 117).size(214, 27));
        mainWidget.size(230, 150).child(panel);
    }

    private static String cosmiccore$tierName(long voltage) {
        int tier = Mth.clamp(GTUtil.getTierByVoltage(voltage), 0, GTValues.VNF.length - 1);
        return GTValues.VNF[tier];
    }
}
