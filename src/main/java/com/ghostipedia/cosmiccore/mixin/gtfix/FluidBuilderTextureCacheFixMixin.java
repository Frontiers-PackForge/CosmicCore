package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = FluidBuilder.class, remap = false)
public abstract class FluidBuilderTextureCacheFixMixin {

    @ModifyArg(
               method = "determineTextures",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/info/MaterialIconType;getBlockTexturePath(Lcom/gregtechceu/gtceu/api/data/chemical/material/info/MaterialIconSet;Z)Lnet/minecraft/resources/ResourceLocation;"),
               index = 1,
               require = 1,
               expect = 1,
               allow = 1)
    private boolean cosmiccore$refreshMaterialFluidTexture(boolean doReadCache) {
        return false;
    }
}
