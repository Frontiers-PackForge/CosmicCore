package com.ghostipedia.cosmiccore.mixin.gtceu;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema$GTKubeRecipe", remap = false)
public abstract class GTKubeRecipeMixin {

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
