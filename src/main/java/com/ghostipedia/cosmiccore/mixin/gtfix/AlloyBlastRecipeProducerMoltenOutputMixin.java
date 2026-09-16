package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.data.recipe.misc.alloyblast.AlloyBlastRecipeProducer;

import net.minecraft.world.level.material.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AlloyBlastRecipeProducer.class, remap = false)
public abstract class AlloyBlastRecipeProducerMoltenOutputMixin {

    @Redirect(
              method = "produce",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;getFluid()Lnet/minecraft/world/level/material/Fluid;",
                       remap = false),
              require = 1,
              remap = false)
    private Fluid cosmiccore$useRegisteredMoltenOutput(Material material) {
        return material.getFluid(FluidStorageKeys.MOLTEN);
    }
}
