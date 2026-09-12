package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.rate.RateCalculatorConsumption;
import com.ghostipedia.cosmiccore.common.rate.RateCalculatorTracker;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeRunner;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(value = RecipeRunner.class, remap = false)
public abstract class RateCalculatorRecipeRunnerMixin {

    @Shadow
    @Final
    private GTRecipe recipe;
    @Shadow
    @Final
    private IO io;
    @Shadow
    @Final
    private boolean isTick;
    @Shadow
    @Final
    private boolean simulated;
    @Shadow
    @Final
    private Predicate<RecipeCapability<?>> outputVoid;
    @Shadow
    private Map<RecipeCapability<?>, List<Object>> recipeContents;
    @Unique
    private IRecipeCapabilityHolder cosmiccore$holder;
    @Unique
    private Map<RecipeCapability<?>, List<Object>> cosmiccore$resolvedContents;
    @Unique
    private boolean cosmiccore$voidedOutput;

    @WrapMethod(method = "handle")
    private ActionResult cosmiccore$scopeConsumption(Map<RecipeCapability<?>, ?> entries,
                                                     Operation<ActionResult> original) {
        boolean input = !simulated && io == IO.IN;
        try (RateCalculatorConsumption consumption = RateCalculatorTracker.beginConsumption(
                cosmiccore$holder, input, isTick)) {
            boolean success = false;
            try {
                ActionResult result = original.call(entries);
                success = result.isSuccess();
                return result;
            } finally {
                if (input)
                    RateCalculatorTracker.endConsumption(cosmiccore$holder, recipe, isTick, success, consumption);
                cosmiccore$resolvedContents = null;
                cosmiccore$voidedOutput = false;
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cosmiccore$captureHolder(GTRecipe recipe, IO io, boolean isTick, IRecipeCapabilityHolder holder,
                                          Map<RecipeCapability<?>, ?> chanceCaches, boolean simulated,
                                          CallbackInfo ci) {
        cosmiccore$holder = holder;
    }

    @Inject(method = "handle",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/recipe/RecipeRunner;handleContents()Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private void cosmiccore$captureResolvedContents(Map<RecipeCapability<?>, ?> entries,
                                                    CallbackInfoReturnable<ActionResult> cir) {
        if (simulated || !RateCalculatorTracker.isWatched(cosmiccore$holder)) return;
        if (io == IO.IN) {
            RateCalculatorConsumption.expect(recipeContents);
            return;
        }
        cosmiccore$resolvedContents = new HashMap<>();
        boolean voidedOutput = false;
        recipeContents.forEach((capability, contents) -> {
            if (io != IO.OUT || !outputVoid.test(capability)) {
                List<Object> copy = new ArrayList<>(contents.size());
                for (Object content : contents) copy.add(copyContent(capability, content));
                cosmiccore$resolvedContents.put(capability, copy);
            }
        });
        if (io == IO.OUT) {
            for (RecipeCapability<?> capability : recipeContents.keySet()) {
                if (outputVoid.test(capability)) {
                    voidedOutput = true;
                    break;
                }
            }
        }
        cosmiccore$voidedOutput = voidedOutput;
    }

    @Inject(method = "handle", at = @At("RETURN"))
    private void cosmiccore$recordCommittedContents(Map<RecipeCapability<?>, ?> entries,
                                                    CallbackInfoReturnable<ActionResult> cir) {
        if (!simulated && cir.getReturnValue().isSuccess() && cosmiccore$resolvedContents != null) {
            RateCalculatorTracker.onRecipeRunnerCommitted(cosmiccore$holder, recipe, io == IO.IN, isTick,
                    cosmiccore$resolvedContents);
            if (cosmiccore$voidedOutput) RateCalculatorTracker.onRecipeRunnerVoided(cosmiccore$holder);
        }
        cosmiccore$resolvedContents = null;
        cosmiccore$voidedOutput = false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object copyContent(RecipeCapability capability, Object content) {
        return capability.copyContent(content);
    }
}
