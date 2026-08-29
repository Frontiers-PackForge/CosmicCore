package com.ghostipedia.cosmiccore.mixin.gtfix.sodium;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.client.renderer.RenderType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultMaterials.class, remap = false)
public abstract class BloomRenderTypeSodiumMaterialFixMixin {

    @Inject(method = "forRenderLayer", at = @At("HEAD"), cancellable = true, require = 1)
    private static void cosmiccore$mapUnavailableBloomLayer(RenderType renderType,
                                                            CallbackInfoReturnable<Material> cir) {
        if (renderType == GTRenderTypes.bloom() && !BloomShaderManager.isBloomAvailable()) {
            cir.setReturnValue(DefaultMaterials.SOLID);
        }
    }
}
