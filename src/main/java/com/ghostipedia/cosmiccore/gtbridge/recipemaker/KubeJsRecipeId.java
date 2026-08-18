package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class KubeJsRecipeId {

    public enum Outcome {
        GENERATED,
        AUTO_VARIANT,
        EXPLICIT,
        EXPLICIT_OCCUPIED,
        FAILED
    }

    public record Resolution(String constructorId, ResourceLocation loadedId, @Nullable String variant,
                             Outcome outcome) {}

    private static final String NAMESPACE = "frontiers";

    private KubeJsRecipeId() {}

    public static Resolution resolveDraft(Player player, RecipeDraft draft, String entered) {
        String map = draft.recipeType.registryName.getPath();
        List<ResourceLocation> outputs = resources(draft.itemOutputs, draft.fluidOutputs);
        List<ResourceLocation> inputs = resources(draft.itemInputs, draft.fluidInputs);
        List<Integer> circuits = circuits(draft.itemInputs);
        return resolve(player, map, entered, outputs, inputs, circuits,
                draft.recipeType.getMaxOutputs(EURecipeCapability.CAP) > 0);
    }

    public static Resolution resolveCodec(Player player, String typeId, String entered,
                                          List<ResourceLocation> outputs, List<ResourceLocation> inputs) {
        ResourceLocation type = ResourceLocation.tryParse(typeId);
        String map = type == null ? "recipe" : type.getPath();
        boolean generator = type != null &&
                BuiltInRegistries.RECIPE_TYPE.get(type) instanceof GTRecipeType gtType &&
                gtType.getMaxOutputs(EURecipeCapability.CAP) > 0;
        return resolve(player, map, entered, outputs, inputs, List.of(), generator);
    }

    private static Resolution resolve(Player player, String map, String entered, List<ResourceLocation> outputs,
                                      List<ResourceLocation> inputs, List<Integer> circuits, boolean inputFirst) {
        String fallback = inputFirst ? primary(inputs, outputs) : primary(outputs, inputs);
        boolean explicit = entered != null && !entered.isBlank();
        if (explicit) {
            ExplicitId explicitId = explicitId(map, entered);
            ResourceLocation loadedId = loadedId(explicitId.namespace(), map, explicitId.semantic());
            Outcome outcome = loaded(player, loadedId) == null ? Outcome.EXPLICIT : Outcome.EXPLICIT_OCCUPIED;
            return new Resolution(constructorId(explicitId.namespace(), explicitId.semantic()), loadedId, null,
                    outcome);
        }
        String semantic = fallback;
        if (semantic.isEmpty()) semantic = "recipe";
        ResourceLocation loadedId = loadedId(NAMESPACE, map, semantic);
        RecipeHolder<?> existing = loaded(player, loadedId);
        if (existing == null) {
            return new Resolution(constructorId(NAMESPACE, semantic), loadedId, null, Outcome.GENERATED);
        }
        if (!(existing.value() instanceof GTRecipe gtRecipe)) {
            return new Resolution(constructorId(NAMESPACE, semantic), loadedId, null, Outcome.FAILED);
        }
        List<String> variants = routeVariants(inputs, circuits, gtRecipe);
        String combined = "";
        for (String variant : variants) {
            combined = combined.isEmpty() ? variant : combined + "_" + variant;
            String candidateSemantic = semantic + "/" + combined;
            ResourceLocation candidateLoadedId = loadedId(NAMESPACE, map, candidateSemantic);
            if (loaded(player, candidateLoadedId) == null) {
                return new Resolution(constructorId(NAMESPACE, candidateSemantic), candidateLoadedId, combined,
                        Outcome.AUTO_VARIANT);
            }
        }
        return new Resolution(constructorId(NAMESPACE, semantic), loadedId, null, Outcome.FAILED);
    }

    private static List<String> routeVariants(List<ResourceLocation> inputs, List<Integer> circuits,
                                              GTRecipe existing) {
        Set<ResourceLocation> existingInputs = new LinkedHashSet<>();
        List<Integer> existingCircuits = new ArrayList<>();
        for (ItemStack stack : RecipeHelper.getInputItems(existing)) {
            int circuit = circuit(stack);
            if (circuit >= 0) existingCircuits.add(circuit);
            else existingInputs.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        for (FluidStack stack : RecipeHelper.getInputFluids(existing)) {
            existingInputs.add(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
        }
        LinkedHashSet<String> variants = new LinkedHashSet<>();
        for (ResourceLocation input : inputs) {
            if (!existingInputs.contains(input) && !input.getPath().equals("programmed_circuit")) {
                variants.add("with_" + cleanSegment(input.getPath()));
            }
        }
        Set<ResourceLocation> requestedInputs = new LinkedHashSet<>(inputs);
        for (ResourceLocation input : existingInputs) {
            if (!requestedInputs.contains(input) && !input.getPath().equals("programmed_circuit")) {
                variants.add("without_" + cleanSegment(input.getPath()));
            }
        }
        for (int circuit : circuits) {
            if (!existingCircuits.contains(circuit)) variants.add("circuit_" + circuit);
        }
        return List.copyOf(variants);
    }

    private static List<ResourceLocation> resources(List<ItemStack> items, List<FluidStack> fluids) {
        List<ResourceLocation> resources = new ArrayList<>();
        for (ItemStack stack : items) {
            if (stack != null && !stack.isEmpty() && circuit(stack) < 0) {
                resources.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            }
        }
        for (FluidStack stack : fluids) {
            if (stack != null && !stack.isEmpty()) {
                resources.add(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
            }
        }
        return resources;
    }

    private static List<Integer> circuits(List<ItemStack> items) {
        List<Integer> circuits = new ArrayList<>();
        for (ItemStack stack : items) {
            int configuration = circuit(stack);
            if (configuration >= 0) circuits.add(configuration);
        }
        return circuits;
    }

    private static int circuit(ItemStack stack) {
        return stack == null || stack.isEmpty() ? -1 : stack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, -1);
    }

    private static String primary(List<ResourceLocation> outputs, List<ResourceLocation> inputs) {
        ResourceLocation resource = !outputs.isEmpty() ? outputs.getFirst() :
                inputs.isEmpty() ? null : inputs.getFirst();
        return resource == null ? "" : cleanSegment(resource.getPath());
    }

    private static ExplicitId explicitId(String map, String entered) {
        String value = entered.trim();
        ResourceLocation parsed = value.contains(":") ? ResourceLocation.tryParse(value) : null;
        String namespace = parsed == null ? NAMESPACE : parsed.getNamespace();
        String path = parsed == null ? value : parsed.getPath();
        String prefix = map + "/";
        if (path.startsWith(prefix)) path = path.substring(prefix.length());
        String semantic = cleanPath(path);
        return new ExplicitId(namespace, semantic.isEmpty() ? "recipe" : semantic);
    }

    private static String constructorId(String namespace, String semantic) {
        return namespace + ":" + semantic;
    }

    private static ResourceLocation loadedId(String namespace, String map, String semantic) {
        return ResourceLocation.fromNamespaceAndPath(namespace, map + "/" + semantic);
    }

    private static @Nullable RecipeHolder<?> loaded(Player player, ResourceLocation id) {
        return player.level().getRecipeManager().byKey(id).orElse(null);
    }

    private static String cleanPath(String value) {
        String[] parts = value.toLowerCase(Locale.ROOT).split("/");
        List<String> cleaned = new ArrayList<>();
        for (String part : parts) {
            String segment = cleanSegment(part);
            if (!segment.isEmpty()) cleaned.add(segment);
        }
        return String.join("/", cleaned);
    }

    private static String cleanSegment(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]+", "_").replaceAll("^_+|_+$", "");
    }

    private record ExplicitId(String namespace, String semantic) {}
}
