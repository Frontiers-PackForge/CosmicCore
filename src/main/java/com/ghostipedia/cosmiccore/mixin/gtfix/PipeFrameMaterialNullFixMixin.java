package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PipeBlockEntity.class, remap = false)
public class PipeFrameMaterialNullFixMixin {

    @Inject(method = "getFrameMaterial", at = @At("RETURN"), cancellable = true, remap = false)
    private void cosmiccore$guardNullFrame(CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(GTMaterials.NULL);
        }
    }
}
