package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockUi;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;

import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MachineUIPanelBuilder.class, remap = false)
public class TieredMachinePanelMixin {

    @Shadow
    @Final
    private MetaMachine machine;

    @Inject(method = "build", at = @At("RETURN"))
    private void cosmiccore$addStructureTierButton(PanelSyncManager syncManager, UISettings settings,
                                                   CallbackInfoReturnable<MachineUIPanel> cir) {
        if (!(machine instanceof ITieredMultiblockMachine tiered) ||
                !(machine.getDefinition() instanceof MultiblockMachineDefinition definition) ||
                !TieredMultiblockPatterns.isTiered(definition)) {
            return;
        }
        IntSyncValue value = new IntSyncValue(tiered::getStructureTier, tiered::setStructureTier).allowC2S();
        cir.getReturnValue().getRightConfiguratorPanel().child(TieredMultiblockUi.createTierButton(
                definition, value,
                tiered::getStructureTierStreak, 18));
    }
}
