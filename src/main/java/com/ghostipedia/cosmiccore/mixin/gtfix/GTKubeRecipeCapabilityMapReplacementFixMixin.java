package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMap;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.CapabilityMapComponent;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CapabilityMapComponent.class, remap = false)
public abstract class GTKubeRecipeCapabilityMapReplacementFixMixin {

    @Inject(
            method = "replace(Ldev/latvian/mods/kubejs/recipe/RecipeScriptContext;Lcom/gregtechceu/gtceu/integration/kjs/recipe/components/CapabilityMap;Ldev/latvian/mods/kubejs/recipe/match/ReplacementMatchInfo;Ljava/lang/Object;)Lcom/gregtechceu/gtceu/integration/kjs/recipe/components/CapabilityMap;",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$replaceCapabilityMap(RecipeScriptContext context, CapabilityMap original,
                                                 ReplacementMatchInfo match, Object with,
                                                 CallbackInfoReturnable<CapabilityMap> cir) {
        CapabilityMap replacement = null;

        for (var entry : original.entrySet()) {
            ContentJS<?> component = GTRecipeComponents.VALID_CAPS.get(entry.getKey());
            List<Content> values = entry.getValue();
            ArrayList<Content> replacedValues = null;

            if (component != null) {
                for (int i = 0; i < values.size(); i++) {
                    Content value = values.get(i);
                    if (!component.matches(context, value, match)) {
                        continue;
                    }
                    Content result = component.replace(context, value, match, with);
                    if (!result.equals(value)) {
                        if (replacedValues == null) {
                            replacedValues = new ArrayList<>(values);
                        }
                        replacedValues.set(i, result);
                    }
                }
            }

            if (replacedValues != null) {
                if (replacement == null) {
                    replacement = new CapabilityMap(original);
                }
                replacement.put(entry.getKey(), replacedValues);
            }
        }

        cir.setReturnValue(replacement == null ? original : replacement);
    }
}
