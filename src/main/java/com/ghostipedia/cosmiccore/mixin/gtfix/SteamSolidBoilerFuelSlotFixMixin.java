package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.steam.SteamSolidBoilerMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = SteamSolidBoilerMachine.class, remap = false)
public abstract class SteamSolidBoilerFuelSlotFixMixin {

    @ModifyArg(method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/machine/trait/notifiable/NotifiableItemStackHandler;<init>(ILcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)V"),
               index = 2,
               remap = false)
    private IO cosmiccore$fuelSlotGuiExtractable(IO capabilityIO) {
        return capabilityIO == IO.IN ? IO.BOTH : capabilityIO;
    }
}
