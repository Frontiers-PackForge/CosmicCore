package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ContentJS.class, remap = false)
public abstract class ContentJSMatchesFixMixin {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean matches(RecipeMatchContext cx, Object value, ReplacementMatchInfo match) {
        Content content = (Content) value;
        RecipeComponent inner = ((ContentJS<?>) (Object) this).baseComponent().instance();
        return inner.matches(cx, content.content(), match);
    }
}
