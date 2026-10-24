package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.GTPetrochemicalRegistryKeys;

import com.gregtechceu.gtceu.api.data.chemical.material.MaterialBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.common.data.materials.OrganicChemistryMaterials;
import com.gregtechceu.gtceu.common.data.materials.UnknownCompositionMaterials;

import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.UnaryOperator;

@Mixin(value = { UnknownCompositionMaterials.class, OrganicChemistryMaterials.class }, remap = false)
public abstract class GTPetrochemicalMaterialIdMixin {

    @Redirect(
              method = "register",
              at = @At(value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;material(Ljava/lang/String;Ljava/util/function/UnaryOperator;)Lcom/gregtechceu/gtceu/api/registry/registrate/entry/MaterialRegistryEntry;"))
    private static MaterialRegistryEntry cosmicCore$rekeyPetrochemicalMaterial(GTRegistrate registrate, String path,
                                                                               UnaryOperator<MaterialBuilder> builder) {
        ResourceLocation id = GTPetrochemicalRegistryKeys.canonicalId(path);
        GTRegistrate owner = id.getNamespace().equals("cosmiccore") ? CosmicRegistration.REGISTRATE : registrate;
        return owner.material(id.getPath(), builder);
    }
}
