package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.compat.gtceu.MachineDefinitionOverrides;

import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MachineBuilder.class, remap = false)
public abstract class MachineDefinitionOverridesMixin {

    @Inject(method = "createAdditionalObjects", at = @At("HEAD"))
    private void cosmiccore$applyDefinitionOverrides(CallbackInfo ci) {
        var builder = (MachineBuilder<?, ?, ?>) (Object) this;
        MachineDefinitionOverrides.apply(builder.getOwner().makeResourceLocation(builder.getName()),
                builder.properties());
    }
}
