package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.ghostipedia.cosmiccore.common.recipe.condition.DeedCondition;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema$GTKubeRecipe", remap = false)
public abstract class GTKubeRecipeMixin {

    public GTRecipeSchema.GTKubeRecipe deed(String deedId) {
        return deed(deedId, false);
    }

    public GTRecipeSchema.GTKubeRecipe deed(String deedId, boolean reverse) {
        GTRecipeSchema.GTKubeRecipe self = (GTRecipeSchema.GTKubeRecipe) (Object) this;
        return self.addCondition(new DeedCondition(reverse, ResourceLocation.parse(deedId)));
    }

    @Inject(method = "getOrCreateId", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$canonicalizeId(CallbackInfoReturnable<ResourceLocation> cir) {
        KubeRecipe self = (KubeRecipe) (Object) this;
        ResourceLocation rid = self.id;
        if (rid == null || self.type == null || self.type.id == null) {
            return;
        }
        String typePath = self.type.id.getPath();
        ResourceLocation canonical = rid.withPath(p -> {
            int slash = p.indexOf('/');
            return typePath + "/" + (slash >= 0 ? p.substring(slash + 1) : p);
        });
        self.id = canonical;
        cir.setReturnValue(canonical);
    }
}
