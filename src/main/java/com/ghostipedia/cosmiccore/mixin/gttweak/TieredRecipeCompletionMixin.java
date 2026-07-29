package com.ghostipedia.cosmiccore.mixin.gttweak;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class TieredRecipeCompletionMixin {

    @Shadow
    protected @Nullable GTRecipe lastRecipe;
    @Shadow
    protected @Nullable GTRecipe lastOriginRecipe;

    @Shadow
    protected abstract IRecipeLogicMachine getRLMachine();

    @Inject(
            method = "setupRecipe",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;setStatus(Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic$Status;)V",
                     ordinal = 1,
                     shift = At.Shift.AFTER),
            require = 1)
    private void cosmiccore$recordStartedTierRecipe(GTRecipe recipe, CallbackInfo ci) {
        if (getRLMachine() instanceof ITieredMultiblockMachine tiered) {
            tiered.beginStructureTierRecipe(recipe);
        }
    }

    @Inject(
            method = "onRecipeFinish",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/feature/IRecipeLogicMachine;afterWorking()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void cosmiccore$recordCompletedTierRecipe(CallbackInfo ci) {
        if (getRLMachine() instanceof ITieredMultiblockMachine tiered) {
            tiered.completeStructureTierRecipe(lastOriginRecipe == null ? lastRecipe : lastOriginRecipe);
        }
    }
}
