package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeLogic.class)
public interface RecipeLogicAccessor {

    @Accessor(value = "runDelay")
    int getRunDelay();

    @Accessor(value = "runDelay")
    void setRunDelay(int delay);

    @Accessor(value = "lastRecipe")
    void setLastRecipe(GTRecipe recipe);

    @Accessor(value = "lastOriginRecipe")
    void setLastOriginRecipe(GTRecipe recipe);

    @Accessor(value = "recipeDirty")
    void setRecipeDirty(boolean value);

    @Invoker
    ActionResult callCheckRecipe(GTRecipe recipe);
}
