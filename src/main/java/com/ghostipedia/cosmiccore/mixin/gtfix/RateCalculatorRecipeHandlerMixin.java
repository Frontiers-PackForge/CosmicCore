package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.common.rate.RateCalculatorConsumption;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = IRecipeHandler.class, remap = false)
public interface RateCalculatorRecipeHandlerMixin {

    @WrapOperation(method = "handleRecipe",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeHandler;handleRecipeInner(Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Ljava/util/List;Z)Ljava/util/List;"))
    private List<?> cosmiccore$measureConsumption(IRecipeHandler<?> handler, IO io, GTRecipe recipe,
                                                  List<?> left, boolean simulate, Operation<List<?>> original) {
        if (simulate) {
            try (ActivityScope ignored = ActivityScope.suspend()) {
                return original.call(handler, io, recipe, left, true);
            }
        }
        if (!ActivityScope.active()) return RateCalculatorConsumption.handle(handler, io, recipe, left, false,
                () -> original.call(handler, io, recipe, left, false));
        ActivityScope scope = ActivityScope.current();
        long before = scope.mutations();
        RateCalculatorConsumption.HandlingSnapshot requested = RateCalculatorConsumption.snapshot(
                handler.getCapability(), left);
        List<?> result = RateCalculatorConsumption.handle(handler, io, recipe, left, false,
                () -> original.call(handler, io, recipe, left, false));
        ActivityScope.partialIfUnrecorded(io == IO.IN, before,
                RateCalculatorConsumption.handled(handler.getCapability(), requested, result));
        return result;
    }
}
