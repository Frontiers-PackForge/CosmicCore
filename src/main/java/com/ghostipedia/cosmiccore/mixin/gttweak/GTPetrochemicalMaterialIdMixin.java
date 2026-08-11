package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.common.data.GTPetrochemicalRegistryKeys;

import com.gregtechceu.gtceu.common.data.materials.OrganicChemistryMaterials;
import com.gregtechceu.gtceu.common.data.materials.UnknownCompositionMaterials;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = { UnknownCompositionMaterials.class, OrganicChemistryMaterials.class }, remap = false)
public abstract class GTPetrochemicalMaterialIdMixin {

    @Redirect(
              method = "register",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/GTCEu;id(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation cosmicCore$rekeyPetrochemicalMaterial(String path) {
        return GTPetrochemicalRegistryKeys.canonicalId(path);
    }
}
