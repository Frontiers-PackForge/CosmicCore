package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema$GTKubeRecipe", remap = false)
public abstract class GTKubeRecipeDataWriteFixMixin {

    @Inject(method = {
            "addData(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addData(Ljava/lang/String;I)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addData(Ljava/lang/String;J)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addData(Ljava/lang/String;F)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addDataString(Ljava/lang/String;Ljava/lang/String;)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addDataNumber(Ljava/lang/String;D)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;",
            "addDataBool(Ljava/lang/String;Z)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTKubeRecipe;"
    }, at = @At("RETURN"))
    private void cosmiccore$markDataWritten(CallbackInfoReturnable<GTRecipeSchema.GTKubeRecipe> cir) {
        GTRecipeSchema.GTKubeRecipe recipe = cir.getReturnValue();
        recipe.setValue(GTRecipeSchema.DATA, recipe.getValue(GTRecipeSchema.DATA));
    }
}
