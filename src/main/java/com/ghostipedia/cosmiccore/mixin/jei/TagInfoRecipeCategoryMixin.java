package com.ghostipedia.cosmiccore.mixin.jei;

import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.library.plugins.jei.tags.ITagInfoRecipe;
import mezz.jei.library.plugins.jei.tags.TagInfoRecipeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Defensive guard for JEI's tag-info category when it is driven through EMI's JEMI bridge. EMI's extras builder
 * supplies no recipe-slots view, so {@link IRecipeExtrasBuilder#getRecipeSlots()} returns null and JEI's
 * createRecipeExtras NPEs for every tag recipe shown in EMI, spamming "Exception adding JEMI extras" (EMI catches
 * it, so it is log noise rather than a crash, but it floods the GTM recipe screens). Skip the extras when there is
 * no slot view; native JEI always provides one, so this never triggers outside the JEMI path.
 */
@Mixin(value = TagInfoRecipeCategory.class, remap = false)
public class TagInfoRecipeCategoryMixin {

    @Inject(
            method = "createRecipeExtras(Lmezz/jei/api/gui/widgets/IRecipeExtrasBuilder;Lmezz/jei/library/plugins/jei/tags/ITagInfoRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void cosmiccore$skipExtrasWhenNoSlots(IRecipeExtrasBuilder builder, ITagInfoRecipe recipe,
                                                  IFocusGroup focuses, CallbackInfo ci) {
        if (builder.getRecipeSlots() == null) {
            ci.cancel();
        }
    }
}
