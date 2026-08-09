package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.data.GTPetrochemicalRegistryKeys;

import com.gregtechceu.gtceu.api.fluids.FluidBuilder;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FluidBuilder.class, remap = false)
public abstract class GTPetrochemicalFluidTextureMixin {

    @Redirect(
              method = "determineTextures",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/resources/ResourceLocation;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation cosmicCore$reuseLegacyPetrochemicalTexture(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, GTPetrochemicalRegistryKeys.legacyTexturePath(path));
    }
}
