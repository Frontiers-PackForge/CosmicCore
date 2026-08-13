package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.ConductorAmpacityRules;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Insulation.class, remap = false)
public abstract class InsulatedCableAmpacityMixin {

    @Shadow
    public abstract boolean isCable();

    @ModifyReturnValue(
                       method = "modifyProperties(Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/WireProperties;)Lcom/gregtechceu/gtceu/api/data/chemical/material/properties/WireProperties;",
                       at = @At("RETURN"),
                       require = 1,
                       expect = 1,
                       allow = 1)
    private WireProperties cosmiccore$applyProtectedConductorAmpacity(WireProperties original) {
        int effectiveAmperage = ConductorAmpacityRules.effectiveAmperage(original, isCable());
        if (effectiveAmperage == original.getAmperage()) {
            return original;
        }
        WireProperties adjusted = original.copy();
        adjusted.setAmperage(effectiveAmperage);
        return adjusted;
    }
}
