package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.recipeviewer.widgets.GTOreByProduct;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTMaterials.class, remap = false)
public abstract class BlackstoneOreIntegrationFixMixin {

    @Inject(method = "init()V", at = @At("TAIL"), require = 1)
    private static void cosmiccore$restoreBlackstoneOreIntegration(CallbackInfo ci) {
        long dustAmount = TagPrefix.dust.materialAmount();
        boolean hasHostDust = TagPrefix.oreBlackstone.secondaryMaterials().stream()
                .anyMatch(stack -> stack.material() == GTMaterials.Blackstone && stack.amount() == dustAmount);
        if (!hasHostDust) {
            TagPrefix.oreBlackstone.addSecondaryMaterial(new MaterialStack(GTMaterials.Blackstone, dustAmount));
        }
        GTOreByProduct.addOreByProductPrefix(TagPrefix.oreBlackstone);
    }
}
