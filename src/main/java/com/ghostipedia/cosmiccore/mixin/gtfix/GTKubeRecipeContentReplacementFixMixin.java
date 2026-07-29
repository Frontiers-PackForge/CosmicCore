package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ContentJS.class, remap = false)
public abstract class GTKubeRecipeContentReplacementFixMixin {

    @Shadow
    @Final
    private RecipeComponentType<?> baseComponent;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject(
            method = "replace(Ldev/latvian/mods/kubejs/recipe/RecipeScriptContext;Lcom/gregtechceu/gtceu/api/recipe/content/Content;Ldev/latvian/mods/kubejs/recipe/match/ReplacementMatchInfo;Ljava/lang/Object;)Lcom/gregtechceu/gtceu/api/recipe/content/Content;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$replaceRecipeContent(RecipeScriptContext context, Content original,
                                                 ReplacementMatchInfo match, Object with,
                                                 CallbackInfoReturnable<Content> cir) {
        RecipeComponent component = baseComponent.instance();
        Object wrapped = component.wrap(context, original.content());
        Object replacement = component.replace(context, wrapped, match, with);
        cir.setReturnValue(new Content(replacement, original.chance(), original.maxChance()));
    }
}
