package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Adds an unambiguous method for setting vein generators from KubeJS.
 * The existing veinGenerator() method is ambiguous between VeinGenerator and ResourceLocation
 * which causes KubeJS/Rhino to fail with custom vein generator subclasses.
 */
@Mixin(value = GTOreDefinition.class, remap = false)
public abstract class GTOreDefinitionMixin {

    @Shadow
    public abstract GTOreDefinition veinGenerator(VeinGenerator generator);

    /**
     * Unambiguous method for KubeJS to set a VeinGenerator.
     * Use this instead of veinGenerator() to avoid Rhino's method ambiguity issues.
     */
    public GTOreDefinition generator(VeinGenerator gen) {
        return this.veinGenerator(gen);
    }
}
