package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.trait.BedrockOreMinerLogic;
import com.gregtechceu.gtceu.common.machine.trait.FluidDrillLogic;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(value = { FluidDrillLogic.class, BedrockOreMinerLogic.class }, remap = false)
public abstract class MachineActivityDrillMixin {

    @WrapOperation(method = "onRecipeFinish",
                   at = @At(value = "INVOKE",
                            target = "Lcom/gregtechceu/gtceu/api/recipe/RecipeHelper;handleRecipeIO(Lcom/gregtechceu/gtceu/api/capability/recipe/IRecipeCapabilityHolder;Lcom/gregtechceu/gtceu/api/recipe/GTRecipe;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;Ljava/util/Map;)Lcom/gregtechceu/gtceu/api/recipe/ActionResult;"))
    private ActionResult cosmiccore$complete(IRecipeCapabilityHolder holder, GTRecipe recipe, IO io,
                                             Map<RecipeCapability<?>, Object2IntMap<?>> caches,
                                             Operation<ActionResult> original) {
        RecipeLogic logic = (RecipeLogic) (Object) this;
        ActionResult result;
        try (ActivityScope ignored = MachineActivityRuntime.scope(logic)) {
            result = original.call(holder, recipe, io, caches);
        }
        MachineActivityRuntime.completed(logic, result);
        return result;
    }
}
