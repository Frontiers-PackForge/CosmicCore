package com.ghostipedia.cosmiccore.mixin.ae2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.itemlists.EncodingHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = EncodingHelper.class, remap = false)
public class PatternEncodingOmniaPreferenceMixin {

    private static final Set<ResourceLocation> COSMICCORE$OMNIA_CIRCUITS = Set.of(
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_lv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_mv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_hv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_ev"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_iv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_luv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_zpm"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_uv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_uhv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_uev"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_uiv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_uxv"),
            ResourceLocation.fromNamespaceAndPath("cosmiccore", "omnia_circuit_opv"));

    @Inject(method = "findBestIngredient", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$preferOmniaProcessingCircuit(Map<AEKey, Integer> ingredientPriorities,
                                                                List<GenericStack> possibleIngredients,
                                                                CallbackInfoReturnable<GenericStack> cir) {
        for (GenericStack candidate : possibleIngredients) {
            if (candidate.what() instanceof AEItemKey itemKey && COSMICCORE$OMNIA_CIRCUITS.contains(itemKey.getId())) {
                cir.setReturnValue(candidate);
                return;
            }
        }
    }

    @ModifyExpressionValue(
                           method = "encodeCraftingRecipe",
                           at = @At(value = "INVOKE",
                                    target = "Ljava/util/Optional;orElseGet(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private static Object cosmiccore$preferOmniaCraftingCircuit(Object selected,
                                                                @Local(name = "ingredient") Ingredient ingredient) {
        for (ItemStack candidate : ingredient.getItems()) {
            if (COSMICCORE$OMNIA_CIRCUITS.contains(BuiltInRegistries.ITEM.getKey(candidate.getItem()))) {
                return candidate;
            }
        }
        return selected;
    }
}
