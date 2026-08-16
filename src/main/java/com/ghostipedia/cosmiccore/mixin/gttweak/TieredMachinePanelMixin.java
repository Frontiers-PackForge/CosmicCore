package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IConfiguredMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockUi;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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

    @ModifyExpressionValue(
                           method = "build",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;getRecipeTypes()[Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;",
                                    remap = false),
                           require = 1)
    private GTRecipeType[] cosmiccore$hideConfiguredRecipeTypeButton(GTRecipeType[] original) {
        if (machine instanceof IConfiguredMultiblockMachine && original.length > 0) {
            return new GTRecipeType[] { original[0] };
        }
        return original;
    }

    @Inject(method = "build", at = @At("RETURN"))
    private void cosmiccore$addStructureTierButton(PanelSyncManager syncManager, UISettings settings,
                                                   CallbackInfoReturnable<MachineUIPanel> cir) {
        if (!(machine instanceof ITieredMultiblockMachine tiered) ||
                !(machine.getDefinition() instanceof MultiblockMachineDefinition definition) ||
                !TieredMultiblockPatterns.isTiered(definition)) {
            return;
        }
        IntSyncValue value = new IntSyncValue(tiered::getStructureTier, tiered::setStructureTier).allowC2S();
        var button = TieredMultiblockUi.createTierButton(
                definition, value,
                tiered::getStructureTierStreak, 18);
        if (machine instanceof IConfiguredMultiblockMachine configured) {
            button.setEnabledIf(widget -> !configured.isConfigurationSelectionLocked());
        }
        cir.getReturnValue().getRightConfiguratorPanel().child(button);
    }
}
