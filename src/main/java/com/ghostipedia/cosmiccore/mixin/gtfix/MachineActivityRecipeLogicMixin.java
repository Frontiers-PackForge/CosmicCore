package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.api.machine.activity.MachineActivity;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class MachineActivityRecipeLogicMixin {

    @WrapMethod(method = "serverTick")
    private void cosmiccore$scopeWork(Operation<Void> original) {
        try (ActivityScope ignored = MachineActivityRuntime.scope((RecipeLogic) (Object) this)) {
            original.call();
        }
    }

    @WrapOperation(method = "setupRecipe",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;handleRecipeIO(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private ActionResult cosmiccore$start(RecipeLogic logic, GTRecipe recipe, IO io, Operation<ActionResult> original) {
        MachineActivityRuntime.started(logic, recipe);
        ActionResult result;
        try (ActivityScope ignored = MachineActivityRuntime.scope(logic)) {
            result = original.call(logic, recipe, io);
        }
        if (!result.isSuccess()) {
            MachineActivityRuntime.failure(logic, result);
            MachineActivity activity = MachineActivityRuntime.activity(logic.getMachine());
            if (activity != null) activity.interrupt(MachineActivityRuntime.lane(logic));
        }
        return result;
    }

    @WrapOperation(method = "onRecipeFinish",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/machine/trait/recipe/RecipeLogic;handleRecipeIO(Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private ActionResult cosmiccore$finish(RecipeLogic logic, GTRecipe recipe, IO io,
                                           Operation<ActionResult> original) {
        ActionResult result;
        try (ActivityScope ignored = MachineActivityRuntime.scope(logic)) {
            result = original.call(logic, recipe, io);
        }
        MachineActivityRuntime.completed(logic, result);
        return result;
    }

    @Inject(method = { "checkRecipe", "handleTickRecipe" }, at = @At("RETURN"))
    private void cosmiccore$failure(GTRecipe recipe, CallbackInfoReturnable<ActionResult> cir) {
        MachineActivityRuntime.failure((RecipeLogic) (Object) this, cir.getReturnValue());
    }

    @Inject(method = { "interruptRecipe", "resetRecipeLogic" }, at = @At("HEAD"))
    private void cosmiccore$interrupt(CallbackInfo ci) {
        RecipeLogic logic = (RecipeLogic) (Object) this;
        MachineActivity activity = MachineActivityRuntime.activity(logic.getMachine());
        if (activity != null) activity.interrupt(MachineActivityRuntime.lane(logic));
    }
}
