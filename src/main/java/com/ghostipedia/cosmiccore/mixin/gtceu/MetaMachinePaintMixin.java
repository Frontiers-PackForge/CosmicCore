package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MetaMachine.class, remap = false)
public abstract class MetaMachinePaintMixin {

    @Inject(method = "setPaintingColor", at = @At("TAIL"))
    private void cosmiccore$onPaintChange(int paintingColor, CallbackInfo ci) {
        if (!(((Object) this) instanceof IMultiPart part)) return;

        // Each part lazy-caches its RecipeHandlerList with the painting color at first access and
        // never updates after. Sync the cached color so anything reading it (GTM handler-group
        // routing, our partition logic) sees the new paint.
        for (RecipeHandlerList rhl : part.getRecipeHandlers()) {
            rhl.setColor(paintingColor);
        }

        for (var controller : part.getControllers()) {
            if (controller instanceof MultithreadedMachine mt) {
                mt.refreshThreadPartitioning();
            }
        }
    }
}
