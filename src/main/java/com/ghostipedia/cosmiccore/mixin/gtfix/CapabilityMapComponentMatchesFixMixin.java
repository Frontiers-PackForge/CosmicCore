package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMap;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMapComponent;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = CapabilityMapComponent.class, remap = false)
public abstract class CapabilityMapComponentMatchesFixMixin {

    public boolean matches(RecipeMatchContext cx, Object value, ReplacementMatchInfo match) {
        CapabilityMap map = (CapabilityMap) value;
        for (var entry : map.entrySet()) {
            ContentJS<?> content = GTRecipeComponents.VALID_CAPS.get(entry.getKey());
            if (content == null) {
                continue;
            }
            List<Content> values = entry.getValue();
            for (int i = 0; i < values.size(); i++) {
                if (content.matches(cx, values.get(i), match)) {
                    return true;
                }
            }
        }
        return false;
    }
}
