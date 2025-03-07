package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.capability.HeatCapability;
import com.ghostipedia.cosmiccore.api.capability.IHeatBlock;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug
@Mixin(value = GTRecipeModifiers.class, remap = false)
public abstract class GTRecipeModifiersMixin {

    // make EBF need heating up, instead of immediately being at target temperature.
    // god this is evil. I love it.
    @SuppressWarnings({ "MixinAnnotationTarget", "InvalidInjectorMethodSignature" }) // it works anyway, MCDev just
                                                                                     // doesn't know that
    @ModifyVariable(method = "ebfOverclock", at = @At(value = "STORE"), ordinal = 0) // target =
                                                                                     // "Lcom/gregtechceu/gtceu/api/block/ICoilType;getCoilTemperature()I",
    private static int frontiers$modifyEbfHeatValue(int originalCoilTemp, MetaMachine machine,
                                                    @NotNull GTRecipe recipe) {
        int coilTier = 1;
        if (machine instanceof CoilWorkableElectricMultiblockMachine coilMachine) {
            System.out.println("COILBLOCKHIT");
            coilTier = coilMachine.getCoilTier() + 1;
        }
        if (machine instanceof IHeatBlock heatBlock) {
            System.out.println("HEATBLOCKHIT");
            heatBlock.setTemperature(
                    HeatCapability.adjustTempTowards(heatBlock.getTemperature(), originalCoilTemp - 273, coilTier));
            return Math.round(heatBlock.getTemperature() + 273);
        }
        return originalCoilTemp;
    }
}
