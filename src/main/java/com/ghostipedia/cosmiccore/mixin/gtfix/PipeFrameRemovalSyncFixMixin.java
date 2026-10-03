package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PipeBlockEntity.class, remap = false)
public abstract class PipeFrameRemovalSyncFixMixin {

    @Redirect(
              method = "onToolClick",
              at = @At(
                       value = "FIELD",
                       target = "Lcom/gregtechceu/gtceu/api/blockentity/PipeBlockEntity;frameMaterial:Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;",
                       opcode = 181))
    private void cosmiccore$syncRemovedFrame(PipeBlockEntity<?, ?> pipe, Material material) {
        pipe.setFrameMaterial(material);
    }
}
