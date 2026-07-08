package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.machine.MachineOutputLimits;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = WorkableTieredMachine.class, remap = false)
public class TieredMachineOutputSlotClampMixin {

    @ModifyArg(method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;<init>(ILcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
               index = 0,
               remap = false)
    private int cosmiccore$clampToOutputLimit(int slots) {
        return MachineOutputLimits.clampItemSlots((MetaMachine) (Object) this, slots);
    }
}
