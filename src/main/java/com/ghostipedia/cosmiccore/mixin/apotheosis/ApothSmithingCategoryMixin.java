package com.ghostipedia.cosmiccore.mixin.apotheosis;

import net.minecraft.world.item.crafting.SmithingRecipe;

import dev.shadowsoffire.apotheosis.adventure.compat.ApothSmithingCategory;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ApothSmithingCategory.class, remap = false)
public abstract class ApothSmithingCategoryMixin {

    @Shadow
    public abstract boolean isHandled(SmithingRecipe recipe);

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/minecraft/world/item/crafting/SmithingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void cosmicCore$injectSetRecipe(IRecipeLayoutBuilder builder, SmithingRecipe recipe, IFocusGroup focuses,
                                           CallbackInfo ci) {
        if (!isHandled(recipe)) {
            ci.cancel();
        }
    }
}
