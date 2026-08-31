package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSoulContainer;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulIngredient;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import com.mojang.serialization.Codec;

import java.util.*;
import java.util.stream.Collectors;

public class SoulRecipeCapability extends RecipeCapability<SoulIngredient> {

    public final static SoulRecipeCapability CAP = new SoulRecipeCapability();

    protected SoulRecipeCapability() {
        super(CosmicCore.id("soul"), 0x5E2129FF, true, 10, SerializerSoulIngredient.INSTANCE);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    // TODO: try to remove
    @Override
    public SoulIngredient copyInner(SoulIngredient content) {
        return super.copyInner(content);
    }

    @Override
    public SoulIngredient copyWithModifier(SoulIngredient content, ContentModifier modifier) {
        var modifiedStack = content.stack().withAmount(modifier.apply(content.stack().amount()));
        return SoulIngredient.of(modifiedStack);
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        var totals = new EnumMap<SoulType, Integer>(SoulType.class);
        for (Object item : ingredients) {
            if (item instanceof SoulIngredient soul) {
                totals.merge(soul.stack().type(), soul.stack().amount(), Math::addExact);
            }
        }
        return totals.entrySet().stream()
                .map(entry -> (Object) SoulIngredient.of(entry.getKey(), entry.getValue()))
                .toList();
    }

    /// Get the total available input of each soul type.
    /// The value is the sum of the available amount in each hatch.
    /// The available amount is the minimum between the contained souls and the available throughput.
    private static Map<SoulType, Integer> getInputContents(IRecipeCapabilityHolder holder) {
        var handlerLists = holder.getCapabilitiesForIO(IO.IN);
        if (handlerLists.isEmpty()) return new HashMap<>();

        var totalSouls = new HashMap<SoulType, Integer>();

        for (var handlerList : handlerLists) {
            if (!handlerList.hasCapability(SoulRecipeCapability.CAP)) continue;
            var soulHandlers = handlerList.getCapability(SoulRecipeCapability.CAP);
            for (var handler : soulHandlers) {
                var soulHandler = (NotifiableSoulContainer) handler;
                for (var content : soulHandler.getContents()) {
                    if (content instanceof SoulIngredient soulIngredient) {
                        var type = soulIngredient.stack().type();
                        var available = Math.min(soulIngredient.stack().amount(), soulHandler.getThroughput(type));
                        totalSouls.merge(type, available, Math::max);
                    } else throw new IllegalArgumentException("Invalid content type");
                }
            }
        }
        return totalSouls;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        if (!holder.hasCapabilityProxies()) return 0;

        var inputs = (tick ? recipe.tickInputs : recipe.inputs).get(this);
        if (inputs == null || inputs.isEmpty()) return 0;

        var totalInputs = getInputContents(holder);

        var requiredInputs = new EnumMap<SoulType, Integer>(SoulType.class);
        inputs.stream()
                .map(content -> (SoulIngredient) content.content())
                .forEach(ingredient -> requiredInputs.merge(
                        ingredient.stack().type(), ingredient.stack().amount(), Math::addExact));

        var parallelMap = requiredInputs.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() == 0 ? Integer.MAX_VALUE :
                                totalInputs.getOrDefault(entry.getKey(), 0) / entry.getValue()));

        int maxParallel = parallelMap.values().stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);

        return Math.min(limit, maxParallel);
    }

    // TODO(8.0.0): re-add XEI display via the new XEI category API.
    // RecipeCapability#addXEIInfo was removed in 8.0.0; the original LDLib LabelWidget rendering
    // (<type>_soul_in / <type>_soul_out) lived here and needs reimplementing against the new XEI category hook.

    private static class SerializerSoulIngredient implements IContentSerializer<SoulIngredient> {

        public static SerializerSoulIngredient INSTANCE = new SerializerSoulIngredient();

        @Override
        public SoulIngredient of(Object o) {
            if (o instanceof SoulStack stack) return SoulIngredient.of(stack);
            else if (o instanceof SoulIngredient ingredient) return ingredient;
            return SoulIngredient.of(new SoulStack(SoulType.Raw, 0));
        }

        @Override
        public SoulIngredient defaultValue() {
            return SoulIngredient.of(new SoulStack(SoulType.Raw, 0));
        }

        @Override
        public Class<SoulIngredient> contentClass() {
            return SoulIngredient.class;
        }

        @Override
        public Codec<SoulIngredient> codec() {
            return SoulIngredient.CODEC;
        }
    }
}
