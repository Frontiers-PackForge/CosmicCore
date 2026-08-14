package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.generated.WireRecipeHandler;

import net.minecraft.data.recipes.RecipeOutput;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = WireRecipeHandler.class, remap = false)
public abstract class WireRecipeHandlerTwelvefoldMixin {

    @Shadow
    @Final
    private static Reference2IntMap<TagPrefix> INSULATION_AMOUNT;

    @Shadow
    @Final
    @Mutable
    private static TagPrefix[] wireSizes;

    @Shadow
    private static void generateCableCovering(RecipeOutput provider, WireProperties property, TagPrefix prefix,
                                              Material material) {}

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void cosmiccore$extendTwelvefoldRecipeTables(CallbackInfo ci) {
        INSULATION_AMOUNT.put(TwelvefoldConductorRegistration.cablePrefix(), 4);
        TagPrefix[] extended = Arrays.copyOf(wireSizes, wireSizes.length + 1);
        System.arraycopy(extended, extended.length - 2, extended, extended.length - 1, 1);
        extended[extended.length - 2] = TwelvefoldConductorRegistration.wirePrefix();
        wireSizes = extended;
    }

    @Inject(method = "run", at = @At("TAIL"), require = 1)
    private static void cosmiccore$generateTwelvefoldCableCovering(RecipeOutput provider, Material material,
                                                                   CallbackInfo ci) {
        WireProperties property = material.getProperty(PropertyKey.WIRE);
        if (property != null) {
            generateCableCovering(provider, property, TwelvefoldConductorRegistration.wirePrefix(), material);
        }
    }
}
