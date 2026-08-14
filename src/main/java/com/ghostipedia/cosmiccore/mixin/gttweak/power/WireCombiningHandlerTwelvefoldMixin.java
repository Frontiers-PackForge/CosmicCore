package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.TwelvefoldConductorRegistration;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.generated.WireCombiningHandler;

import net.minecraft.data.recipes.RecipeOutput;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtOctal;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtQuadruple;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.PACKER_RECIPES;

@Mixin(value = WireCombiningHandler.class, remap = false)
public abstract class WireCombiningHandlerTwelvefoldMixin {

    @Shadow
    @Final
    @Mutable
    private static Map<TagPrefix, TagPrefix> cableToWireMap;

    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void cosmiccore$extendTwelvefoldCableMap(CallbackInfo ci) {
        Map<TagPrefix, TagPrefix> extended = new LinkedHashMap<>(cableToWireMap);
        extended.put(TwelvefoldConductorRegistration.cablePrefix(), TwelvefoldConductorRegistration.wirePrefix());
        cableToWireMap = extended;
    }

    @Inject(method = "run", at = @At("TAIL"), require = 1)
    private static void cosmiccore$generateTwelvefoldBranchRecipes(RecipeOutput provider, Material material,
                                                                   CallbackInfo ci) {
        TagPrefix wireTwelve = TwelvefoldConductorRegistration.wirePrefix();
        if (!material.hasProperty(PropertyKey.WIRE) || !material.shouldGenerateRecipesFor(wireTwelve)) {
            return;
        }
        VanillaRecipeHelper.addShapelessRecipe(provider, material.getName() + "_wire_twelve_combining",
                ChemicalHelper.get(wireTwelve, material), new MaterialEntry(wireGtOctal, material),
                new MaterialEntry(wireGtQuadruple, material));
        VanillaRecipeHelper.addShapelessRecipe(provider, material.getName() + "_wire_twelve_splitting",
                ChemicalHelper.get(wireGtQuadruple, material, 3), new MaterialEntry(wireTwelve, material));
        PACKER_RECIPES.recipeBuilder("pack_" + material.getName() + "_wires_twelve")
                .inputItems(wireGtOctal, material)
                .inputItems(wireGtQuadruple, material)
                .circuitMeta(12)
                .outputItems(wireTwelve, material)
                .save(provider);
        PACKER_RECIPES.recipeBuilder("unpack_" + material.getName() + "_wires_twelve")
                .inputItems(wireTwelve, material)
                .circuitMeta(1)
                .outputItems(wireGtOctal, material)
                .outputItems(wireGtQuadruple, material)
                .save(provider);
    }
}
