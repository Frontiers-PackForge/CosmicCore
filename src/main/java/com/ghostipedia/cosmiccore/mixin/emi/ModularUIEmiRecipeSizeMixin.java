package com.ghostipedia.cosmiccore.mixin.emi;

import com.gregtechceu.gtceu.integration.recipeviewer.emi.GTOreProcessingEmiCategory.GTEmiOreProcessingWrapper;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.MultiblockInfoEmiCategory.MultiblockInfoEmiWrapper;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.ProgrammedCircuitEmiCategory.GTProgrammedCircuitWrapper;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein.GTBedrockFluidEmiCategory.GTBedrockFluid;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein.GTBedrockOreEmiCategory.GTBedrockOre;
import com.gregtechceu.gtceu.integration.recipeviewer.emi.orevein.GTOreVeinEmiCategory.GTEmiOreVein;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.widget.Bounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModularUIEmiRecipe.class, remap = false)
public abstract class ModularUIEmiRecipeSizeMixin {

    @Shadow
    private boolean sizeCalculated;
    @Shadow
    private Bounds bounds;
    @Shadow
    private int displayWidth;
    @Shadow
    private int displayHeight;

    @Inject(method = "requireSize", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$estimateSize(CallbackInfo ci) {
        if (this.sizeCalculated) {
            ci.cancel();
            return;
        }
        int w;
        int h;
        Object self = this;
        if (self instanceof MultiblockInfoEmiWrapper) {
            w = 320;
            h = 260;
        } else if (self instanceof GTEmiOreProcessingWrapper) {
            w = 180;
            h = 170;
        } else if (self instanceof GTEmiOreVein) {
            w = 130;
            h = 164;
        } else if (self instanceof GTBedrockOre) {
            w = 130;
            h = 144;
        } else if (self instanceof GTBedrockFluid) {
            w = 128;
            h = 144;
        } else if (self instanceof GTProgrammedCircuitWrapper) {
            w = 154;
            h = 84;
        } else {
            return;
        }
        this.displayWidth = w;
        this.displayHeight = h;
        this.bounds = new Bounds(0, 0, w, h);
        this.sizeCalculated = true;
        ci.cancel();
    }
}
