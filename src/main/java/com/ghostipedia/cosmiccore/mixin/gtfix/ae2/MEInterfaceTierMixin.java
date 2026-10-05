package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MachineBuilder.class, remap = false)
public abstract class MEInterfaceTierMixin {

    @Inject(method = "createAdditionalObjects", at = @At("HEAD"))
    private void cosmiccore$useHVTier(CallbackInfo ci) {
        var builder = (MachineBuilder<?, ?, ?>) (Object) this;
        if (!builder.getOwner().getModid().equals("gtceu")) return;
        switch (builder.getName()) {
            case "me_input_bus", "me_input_hatch", "me_output_bus", "me_output_hatch", "me_stocking_input_bus", "me_stocking_input_hatch", "me_pattern_buffer", "me_pattern_buffer_proxy" -> builder
                    .tier(GTValues.HV);
            default -> {}
        }
    }
}
