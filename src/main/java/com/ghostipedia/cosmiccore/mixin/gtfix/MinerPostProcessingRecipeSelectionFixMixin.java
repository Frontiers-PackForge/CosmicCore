package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.machine.trait.miner.MinerLogic;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.function.Predicate;

@Mixin(value = MinerLogic.class, remap = false)
public abstract class MinerPostProcessingRecipeSelectionFixMixin {

    @Shadow
    protected abstract int getVoltageTier();

    @Inject(
            method = "doPostProcessing(Lnet/minecraft/core/NonNullList;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void cosmiccore$skipMissingPostProcessingSlots(CallbackInfoReturnable<Boolean> cir) {
        GTRecipeType recipeType = ((MinerLogic) (Object) this).getRLMachine().getRecipeType();
        if (recipeType.getMaxInputs(ItemRecipeCapability.CAP) < 1 ||
                recipeType.getMaxOutputs(ItemRecipeCapability.CAP) < 1) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
                   method = "doPostProcessing(Lnet/minecraft/core/NonNullList;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/storage/loot/LootParams$Builder;)Z",
                   at = @At(
                            value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;searchRecipe(Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeCapabilityHolder;Ljava/util/function/Predicate;)Ljava/util/Iterator;",
                            remap = false),
                   require = 1,
                   remap = false)
    private Iterator<GTRecipe> cosmiccore$skipOverVoltageRecipes(GTRecipeType recipeType,
                                                                 IRecipeCapabilityHolder holder,
                                                                 Predicate<GTRecipe> canHandle,
                                                                 Operation<Iterator<GTRecipe>> original) {
        int voltageTier = getVoltageTier();
        return original.call(recipeType, holder,
                canHandle.and(recipe -> GTUtil.getTierByVoltage(recipe.getInputEUt().getTotalEU()) <= voltageTier));
    }
}
