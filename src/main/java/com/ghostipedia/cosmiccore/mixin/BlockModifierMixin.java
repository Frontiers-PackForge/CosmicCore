package com.ghostipedia.cosmiccore.mixin;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sfiomn.legendarysurvivaloverhaul.common.temperature.BlockModifier;
import sfiomn.legendarysurvivaloverhaul.util.SpreadPoint;

@Mixin(BlockModifier.class)
public class BlockModifierMixin {
    @Inject(method = "getTemperatureFromSpreadPoint", at = @At("HEAD"), cancellable = true)
    public void cosmicCore$injectGetTemperatureFromSpreadPoint(Level level, SpreadPoint spreadPoint, CallbackInfoReturnable<Float> cir) {
        var entity = level.getBlockEntity(spreadPoint.position());
        if (!(entity instanceof MetaMachineBlockEntity mmbe)) {
            return;
        }
        cir.setReturnValue(cosmicCore$getMachineTemperature(mmbe.getMetaMachine()));
    }
    
    @Unique
    public float cosmicCore$getMachineTemperature(MetaMachine machine) {
        var temperature = 0f;
        if (machine instanceof IRecipeLogicMachine recipeLogicMachine) {
            var recipeLogic = recipeLogicMachine.getRecipeLogic();
            // do recipe logic calculations
        } else {
            // do regular machine calculations
        }
        return temperature;
    }
}
