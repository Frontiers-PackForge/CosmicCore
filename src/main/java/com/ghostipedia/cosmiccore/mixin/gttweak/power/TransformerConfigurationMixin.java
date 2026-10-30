package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.compat.gtceu.transformer.ConfigurableTransformer;
import com.ghostipedia.cosmiccore.common.compat.gtceu.transformer.TransformerConfigurationUI;
import com.ghostipedia.cosmiccore.common.compat.gtceu.transformer.TransformerStepDownPolicy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.ClientFieldChangeListener;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransformerMachine.class, remap = false)
public abstract class TransformerConfigurationMixin implements ConfigurableTransformer, IMuiMachine {

    @Unique
    @SaveField(nbtKey = "cosmiccore_step_down_transitions")
    @SyncToClient
    private int cosmiccore$stepDownTransitions = TransformerStepDownPolicy.DEFAULT_TRANSITIONS;

    @Unique
    @SaveField(nbtKey = "cosmiccore_step_down_output_amperage")
    @SyncToClient
    private int cosmiccore$stepDownOutputAmperage = TransformerStepDownPolicy.AUTOMATIC_AMPERAGE;

    @Inject(method = "onLoad", at = @At("HEAD"), require = 1)
    private void cosmiccore$validateConfiguration(CallbackInfo ci) {
        TransformerMachine machine = cosmiccore$self();
        int inputTier = machine.getTier() + 1;
        cosmiccore$stepDownTransitions = TransformerStepDownPolicy.transitions(
                inputTier, cosmiccore$stepDownTransitions);
        cosmiccore$stepDownOutputAmperage = TransformerStepDownPolicy.storedOutputAmperage(
                cosmiccore$stepDownOutputAmperage, cosmiccore$getMaximumStepDownOutputAmperage());
    }

    @Inject(method = "updateEnergyContainer(Z)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$applyStepDownConfiguration(boolean transformUp, CallbackInfo ci) {
        if (transformUp) return;
        TransformerMachine machine = cosmiccore$self();
        long inputVoltage = GTValues.V[machine.getTier() + 1];
        long outputVoltage = GTValues.V[cosmiccore$getStepDownOutputTier()];
        int inputAmperage = machine.getBaseAmp();
        int outputAmperage = cosmiccore$getStepDownOutputAmperage();
        machine.energyContainer.resetBasicInfo(
                TransformerStepDownPolicy.storageCapacity(inputVoltage, inputAmperage),
                inputVoltage, inputAmperage, outputVoltage, outputAmperage);
        machine.energyContainer.setSideInputCondition(
                side -> side == machine.getFrontFacing() && machine.isWorkingEnabled());
        machine.energyContainer.setSideOutputCondition(
                side -> side != machine.getFrontFacing() && machine.isWorkingEnabled());
        ci.cancel();
    }

    @Inject(method = "tintColor(I)I", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$tintConfiguredOutput(int index, CallbackInfoReturnable<Integer> cir) {
        TransformerMachine machine = cosmiccore$self();
        if (index == 3 && !machine.isTransformUp()) {
            cir.setReturnValue(GTValues.VC[cosmiccore$getStepDownOutputTier()]);
        }
    }

    @Unique
    @ClientFieldChangeListener(fieldName = "cosmiccore$stepDownTransitions")
    private void cosmiccore$onStepDownTransitionsSynced() {
        cosmiccore$refreshClientConfiguration();
    }

    @Unique
    @ClientFieldChangeListener(fieldName = "cosmiccore$stepDownOutputAmperage")
    private void cosmiccore$onStepDownOutputAmperageSynced() {
        cosmiccore$refreshClientConfiguration();
    }

    @Override
    public int cosmiccore$getStepDownTransitions() {
        TransformerMachine machine = cosmiccore$self();
        return TransformerStepDownPolicy.transitions(machine.getTier() + 1, cosmiccore$stepDownTransitions);
    }

    @Override
    public int cosmiccore$getStepDownOutputTier() {
        TransformerMachine machine = cosmiccore$self();
        return TransformerStepDownPolicy.outputTier(
                machine.getTier() + 1, cosmiccore$getStepDownTransitions());
    }

    @Override
    public int cosmiccore$getStepDownOutputAmperage() {
        return TransformerStepDownPolicy.effectiveOutputAmperage(
                cosmiccore$stepDownOutputAmperage, cosmiccore$getMaximumStepDownOutputAmperage());
    }

    @Override
    public int cosmiccore$getMaximumStepDownOutputAmperage() {
        TransformerMachine machine = cosmiccore$self();
        return TransformerStepDownPolicy.maximumOutputAmperage(
                machine.getBaseAmp(), machine.getTier() + 1, cosmiccore$getStepDownTransitions());
    }

    @Override
    public void cosmiccore$setStepDownTransitions(int transitions) {
        TransformerMachine machine = cosmiccore$self();
        if (machine.isRemote()) return;
        int configured = TransformerStepDownPolicy.transitions(machine.getTier() + 1, transitions);
        if (configured == cosmiccore$stepDownTransitions) return;
        cosmiccore$stepDownTransitions = configured;
        cosmiccore$stepDownOutputAmperage = TransformerStepDownPolicy.storedOutputAmperage(
                cosmiccore$stepDownOutputAmperage, cosmiccore$getMaximumStepDownOutputAmperage());
        cosmiccore$configurationChanged();
    }

    @Override
    public void cosmiccore$setStepDownOutputAmperage(int amperage) {
        TransformerMachine machine = cosmiccore$self();
        if (machine.isRemote()) return;
        int configured = TransformerStepDownPolicy.storedOutputAmperage(
                amperage, cosmiccore$getMaximumStepDownOutputAmperage());
        if (configured == cosmiccore$stepDownOutputAmperage) return;
        cosmiccore$stepDownOutputAmperage = configured;
        cosmiccore$configurationChanged();
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        TransformerConfigurationUI.build(cosmiccore$self(), mainWidget, guiData, syncManager, settings);
    }

    @Unique
    private void cosmiccore$configurationChanged() {
        TransformerMachine machine = cosmiccore$self();
        machine.setChanged();
        machine.getSyncDataHolder().markClientSyncFieldDirty("cosmiccore$stepDownTransitions");
        machine.getSyncDataHolder().markClientSyncFieldDirty("cosmiccore$stepDownOutputAmperage");
        if (!machine.isTransformUp()) {
            machine.updateEnergyContainer(false);
        }
        machine.scheduleRenderUpdate();
    }

    @Unique
    private void cosmiccore$refreshClientConfiguration() {
        TransformerMachine machine = cosmiccore$self();
        machine.updateEnergyContainer(machine.isTransformUp());
        machine.scheduleRenderUpdate();
    }

    @Unique
    private TransformerMachine cosmiccore$self() {
        return (TransformerMachine) (Object) this;
    }
}
