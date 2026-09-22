package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.AlloyBlastRecipeProducer;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AlloyBlastRecipeProducer.class, remap = false)
public abstract class AlloyBlastRecipeProducerMoltenOutputMixin {

    @Shadow
    protected abstract void addFreezerRecipes(Material material, Fluid molten, int temperature,
                                              RecipeOutput provider);

    @Redirect(
              method = "produce",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;getFluid()Lnet/minecraft/world/level/material/Fluid;",
                       remap = false),
              require = 1,
              remap = false)
    private Fluid cosmiccore$useRegisteredMoltenOutput(Material receiver, Material material,
                                                       BlastProperty property, RecipeOutput provider) {
        Fluid molten = receiver.getFluid(FluidStorageKeys.MOLTEN);
        if (molten != null) addFreezerRecipes(material, molten, property.getBlastTemperature(), provider);
        return molten;
    }
}
