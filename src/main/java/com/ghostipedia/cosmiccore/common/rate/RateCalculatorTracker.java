package com.ghostipedia.cosmiccore.common.rate;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RateCalculatorTracker {

    private static final Map<GlobalPos, WatchedMachine> WATCHED = new HashMap<>();
    private static final Map<UUID, Set<GlobalPos>> SUBSCRIPTIONS = new HashMap<>();

    private RateCalculatorTracker() {}

    public static RateCalculatorMachineSnapshot watch(UUID subscription, ServerLevel level, BlockPos position) {
        if (!(MetaMachine.getMachine(level, position) instanceof IRecipeLogicMachine machine)) return null;
        return snapshotDetached(machine.getRecipeLogic());
    }

    public static void unwatch(UUID subscription, ServerLevel level, BlockPos position) {
        GlobalPos key = GlobalPos.of(level.dimension(), position);
        Set<GlobalPos> watched = SUBSCRIPTIONS.get(subscription);
        if (watched == null || !watched.remove(key)) return;
        if (watched.isEmpty()) SUBSCRIPTIONS.remove(subscription);
        release(key);
    }

    public static void clearSubscription(UUID subscription) {
        Set<GlobalPos> watched = SUBSCRIPTIONS.remove(subscription);
        if (watched != null) watched.forEach(RateCalculatorTracker::release);
    }

    public static RateCalculatorMachineSnapshot snapshot(RecipeLogic logic) {
        WatchedMachine watched = watched(logic);
        return watched == null ? snapshotDetached(logic) : refresh(watched.snapshot, logic);
    }

    public static void onRecipeStarted(RecipeLogic logic, GTRecipe recipe) {
        WatchedMachine watched = watched(logic);
        if (watched != null) {
            watched.snapshot.markFresh();
            configure(watched.snapshot, logic, recipe);
        }
    }

    public static void onRecipeCompleted(RecipeLogic logic, GTRecipe recipe) {
        if (!(logic instanceof RateCalculatorRecipeHistory history) || recipe == null) return;
        String id = recipe.getId().toString();
        if (!Objects.equals(id, history.cosmiccore$getLastCompletedRecipeId()))
            history.cosmiccore$setLastCompletedRecipeId(id);
    }

    public static boolean isWatched(IRecipeCapabilityHolder holder) {
        return holder instanceof IRecipeLogicMachine machine && watched(machine.getRecipeLogic()) != null;
    }

    public static RateCalculatorConsumption beginConsumption(IRecipeCapabilityHolder holder, boolean enabled,
                                                             boolean perTick) {
        boolean active = enabled && isWatched(holder);
        return new RateCalculatorConsumption(active ? ((MetaMachine) holder).getLevel().registryAccess() : null,
                active, perTick);
    }

    public static void endConsumption(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean perTick,
                                      boolean success, RateCalculatorConsumption consumption) {
        if (!(holder instanceof IRecipeLogicMachine machine)) return;
        WatchedMachine watched = watched(machine.getRecipeLogic());
        if (watched == null) return;
        watched.snapshot.addObserved(true, consumption.removed());
        watched.snapshot.addObserved(false, consumption.returned());
        if (!consumption.complete()) watched.snapshot.markInputPartial();
        if (!success || !consumption.complete()) return;
        if (perTick) {
            watched.tickRecipe = machine.getRecipeLogic().getLastRecipe();
            watched.tickInputs = consumption.removed();
            watched.tickReturns = consumption.returned();
        } else {
            watched.cycleRecipe = recipe;
            watched.cycleInputs = consumption.removed();
            watched.cycleReturns = consumption.returned();
        }
    }

    public static void onRecipeRunnerCommitted(IRecipeCapabilityHolder holder, GTRecipe recipe, boolean input,
                                               boolean perTick,
                                               Map<RecipeCapability<?>, List<Object>> resolvedContents) {
        if (!(holder instanceof IRecipeLogicMachine machine)) return;
        WatchedMachine watched = watched(machine.getRecipeLogic());
        if (watched == null) return;
        watched.snapshot.addObserved(input, resources(resolvedContents, perTick,
                ((MetaMachine) machine).getLevel().registryAccess()));
    }

    public static void onRecipeRunnerVoided(IRecipeCapabilityHolder holder) {
        if (!(holder instanceof IRecipeLogicMachine machine)) return;
        WatchedMachine watched = watched(machine.getRecipeLogic());
        if (watched != null) watched.snapshot.markObservedPartial();
    }

    public static void invalidateAll() {
        WATCHED.values().forEach(entry -> entry.snapshot.invalidate());
    }

    private static void release(GlobalPos key) {
        WatchedMachine entry = WATCHED.get(key);
        if (entry == null || --entry.references > 0) return;
        WATCHED.remove(key);
    }

    private static WatchedMachine watched(RecipeLogic logic) {
        if (!(logic.getRLMachine() instanceof MetaMachine meta) || !(meta.getLevel() instanceof ServerLevel level)) {
            return null;
        }
        return WATCHED.get(GlobalPos.of(level.dimension(), meta.getBlockPos()));
    }

    private static RateCalculatorMachineSnapshot snapshotDetached(RecipeLogic logic) {
        IRecipeLogicMachine machine = logic.getRLMachine();
        if (!(machine instanceof MetaMachine meta) || !(meta.getLevel() instanceof ServerLevel level)) {
            throw new IllegalArgumentException("Rate Calculator requires a loaded server MetaMachine");
        }
        return refresh(new RateCalculatorMachineSnapshot(level.dimension(), meta.getBlockPos().asLong()), logic);
    }

    private static RateCalculatorMachineSnapshot refresh(RateCalculatorMachineSnapshot snapshot, RecipeLogic logic) {
        if (!(logic.getRLMachine() instanceof MetaMachine meta)) return snapshot;
        GTRecipe recipe = logic.getLastRecipe();
        if (recipe != null) configure(snapshot, logic, recipe);
        else snapshot.configureWithoutRecipe(meta.getDefinition().getId().toString(),
                logic.getStatus().name().toLowerCase(), logic.getProgress());
        return snapshot;
    }

    private static GTRecipe findCurrentRecipe(RecipeLogic logic) {
        IRecipeLogicMachine machine = logic.getRLMachine();
        Iterator<GTRecipe> matches = machine.getRecipeType().searchRecipe(machine, recipe -> true);
        GTRecipe candidate = null;
        while (matches.hasNext()) {
            GTRecipe recipe = machine.fullModifyRecipe(matches.next());
            if (recipe == null || !RecipeHelper.checkConditions(recipe, logic).isSuccess() ||
                    !RecipeHelper.matchContents(machine, recipe).isSuccess())
                continue;
            if (candidate != null) return null;
            candidate = recipe;
        }
        return candidate;
    }

    private static GTRecipe findFlashedRecipe(RecipeLogic logic) {
        if (!(logic instanceof RateCalculatorRecipeHistory history)) return null;
        String savedId = history.cosmiccore$getLastCompletedRecipeId();
        ResourceLocation id = savedId == null ? null : ResourceLocation.tryParse(savedId);
        if (id == null) return null;
        IRecipeLogicMachine machine = logic.getRLMachine();
        var holder = ((MetaMachine) machine).getLevel().getRecipeManager().byKey(id).orElse(null);
        if (holder == null || !(holder.value() instanceof GTRecipe origin) ||
                origin.recipeType != machine.getRecipeType())
            return null;
        return machine.fullModifyRecipe(origin.copy());
    }

    private static void configure(RateCalculatorMachineSnapshot snapshot, RecipeLogic logic, GTRecipe recipe) {
        MetaMachine meta = (MetaMachine) logic.getRLMachine();
        var energy = RecipeHelper.getRealEUt(recipe);
        long inputEUt = recipe.getInputEUt().getTotalEU();
        long outputEUt = recipe.getOutputEUt().getTotalEU();
        List<RateCalculatorResource> fallbackInputs = configuredResources(recipe, true, false,
                meta.getLevel().registryAccess());
        List<RateCalculatorResource> fallbackTickInputs = configuredResources(recipe, true, true,
                meta.getLevel().registryAccess());
        List<RateCalculatorResource> inputs = fallbackInputs;
        List<RateCalculatorResource> tickInputs = fallbackTickInputs;
        List<RateCalculatorResource> outputs = configuredResources(recipe, false, false,
                meta.getLevel().registryAccess());
        outputs.addAll(configuredResources(recipe, false, true, meta.getLevel().registryAccess()));
        WatchedMachine watched = watched(logic);
        boolean learnedCycle = watched != null && watched.cycleRecipe == recipe && deterministicInputs(recipe, false);
        boolean learnedTick = watched != null && watched.tickRecipe == recipe && deterministicInputs(recipe, true);
        if (watched != null) {
            if (learnedCycle) {
                inputs = new ArrayList<>(watched.cycleInputs);
                outputs.addAll(watched.cycleReturns);
            }
            if (learnedTick) {
                tickInputs = watched.tickInputs;
                outputs.addAll(watched.tickReturns);
            }
        }
        if (!learnedCycle && recipe.ingredientActions.isEmpty())
            inputs = resolveConsumedIdentities(logic, inputs, meta.getLevel().registryAccess());
        if (!learnedCycle && !recipe.ingredientActions.isEmpty())
            inputs = inputs.stream().map(RateCalculatorResource::uncertain).toList();
        inputs = new ArrayList<>(inputs);
        inputs.addAll(tickInputs);
        List<String> unsupported = unsupportedCapabilities(recipe);
        int currentTier = meta instanceof IOverclockMachine machine ? machine.getOverclockTier() : -1;
        int minimumTier = meta instanceof IOverclockMachine machine ? machine.getMinOverclockTier() : currentTier;
        int maximumTier = meta instanceof IOverclockMachine machine ? machine.getMaxOverclockTier() : currentTier;
        boolean configurationPartial = hasUnknown(inputs) || hasUnknown(outputs) || !unsupported.isEmpty() ||
                !recipe.ingredientActions.isEmpty() && !learnedCycle;
        List<RateCalculatorCapacityProfile> profiles = List.of(
                profile(currentTier, recipe, inputs, outputs, configurationPartial));
        snapshot.configure(meta.getDefinition().getId().toString(), logic.getStatus().name().toLowerCase(),
                recipe.getId().toString(), recipe.duration, logic.getProgress(), recipe.getTotalRuns(),
                energy.voltage(), energy.amperage(), inputEUt, outputEUt, currentTier, minimumTier, maximumTier,
                inputs, outputs, unsupported, profiles,
                configurationPartial);
    }

    private static boolean deterministicInputs(GTRecipe recipe, boolean perTick) {
        var contents = perTick ? recipe.tickInputs : recipe.inputs;
        return contents.entrySet().stream()
                .filter(entry -> entry.getKey() == ItemRecipeCapability.CAP ||
                        entry.getKey() == FluidRecipeCapability.CAP)
                .flatMap(entry -> entry.getValue().stream())
                .noneMatch(content -> content.chance() > 0 && content.chance() < content.maxChance());
    }

    private static RateCalculatorCapacityProfile profile(int tier, GTRecipe recipe,
                                                         List<RateCalculatorResource> inputs,
                                                         List<RateCalculatorResource> outputs, boolean partial) {
        var energy = RecipeHelper.getRealEUt(recipe);
        return new RateCalculatorCapacityProfile(tier, recipe.duration, recipe.getTotalRuns(), recipe.ocLevel,
                recipe.parallels, recipe.subtickParallels, recipe.batchParallels, energy.voltage(), energy.amperage(),
                recipe.getInputEUt().getTotalEU(), recipe.getOutputEUt().getTotalEU(), partial, inputs, outputs);
    }

    private static List<String> unsupportedCapabilities(GTRecipe recipe) {
        Set<String> unsupported = new java.util.LinkedHashSet<>();
        for (Map<RecipeCapability<?>, List<Content>> contents : List.of(recipe.inputs, recipe.outputs,
                recipe.tickInputs, recipe.tickOutputs))
            contents.keySet().stream().filter(capability -> capability != ItemRecipeCapability.CAP &&
                    capability != FluidRecipeCapability.CAP && capability != EURecipeCapability.CAP)
                    .map(Object::toString).forEach(unsupported::add);
        return List.copyOf(unsupported);
    }

    private static List<RateCalculatorResource> configuredResources(GTRecipe recipe, boolean input, boolean perTick,
                                                                    net.minecraft.core.HolderLookup.Provider registries) {
        var contents = perTick ? (input ? recipe.tickInputs : recipe.tickOutputs) :
                (input ? recipe.inputs : recipe.outputs);
        List<RateCalculatorResource> result = new ArrayList<>();
        ChanceLogic itemLogic = recipe.getChanceLogicForCapability(ItemRecipeCapability.CAP, input ? IO.IN : IO.OUT,
                perTick);
        for (Content content : contents.getOrDefault(ItemRecipeCapability.CAP, List.of())) {
            if (content.chance() == 0) continue;
            int chance = content.chance();
            ItemStack[] stacks = ItemRecipeCapability.CAP.of(content.content()).getItems();
            if (stacks.length != 1) result.add(RateCalculatorResource.unknown("item", itemKey(stacks),
                    stacks.length == 0 ? 0 : stacks[0].getCount(), chance, content.maxChance(), perTick,
                    chance == content.maxChance() || itemLogic == ChanceLogic.OR, itemLogic.toString(),
                    RateCalculatorResource.itemAlternatives(stacks, registries)));
            else result.add(RateCalculatorResource.item(stacks[0], chance, content.maxChance(), perTick,
                    true, chance == content.maxChance() || itemLogic == ChanceLogic.OR, itemLogic.toString(),
                    registries));
        }
        ChanceLogic fluidLogic = recipe.getChanceLogicForCapability(FluidRecipeCapability.CAP, input ? IO.IN : IO.OUT,
                perTick);
        for (Content content : contents.getOrDefault(FluidRecipeCapability.CAP, List.of())) {
            if (content.chance() == 0) continue;
            int chance = content.chance();
            FluidStack[] stacks = FluidRecipeCapability.CAP.of(content.content()).getFluids();
            if (stacks.length != 1) result.add(RateCalculatorResource.unknown("fluid", fluidKey(stacks),
                    stacks.length == 0 ? 0 : stacks[0].getAmount(), chance, content.maxChance(), perTick,
                    chance == content.maxChance() || fluidLogic == ChanceLogic.OR, fluidLogic.toString(),
                    RateCalculatorResource.fluidAlternatives(stacks, registries)));
            else result.add(RateCalculatorResource.fluid(stacks[0], chance, content.maxChance(), perTick,
                    true, chance == content.maxChance() || fluidLogic == ChanceLogic.OR, fluidLogic.toString(),
                    registries));
        }
        return result;
    }

    private static List<RateCalculatorResource> resources(Map<RecipeCapability<?>, List<Object>> contents,
                                                          boolean perTick,
                                                          net.minecraft.core.HolderLookup.Provider registries) {
        List<RateCalculatorResource> result = new ArrayList<>();
        for (Object content : contents.getOrDefault(ItemRecipeCapability.CAP, List.of())) {
            ItemStack[] stacks = ItemRecipeCapability.CAP.of(content).getItems();
            if (stacks.length == 1) result.add(RateCalculatorResource.item(stacks[0], 10_000, 10_000, perTick, true,
                    true, "OR", registries));
            else result.add(RateCalculatorResource.unknown("item", itemKey(stacks),
                    stacks.length == 0 ? 0 : stacks[0].getCount(), 0, 0, perTick, true, "OR",
                    RateCalculatorResource.itemAlternatives(stacks, registries)));
        }
        for (Object content : contents.getOrDefault(FluidRecipeCapability.CAP, List.of())) {
            FluidStack[] stacks = FluidRecipeCapability.CAP.of(content).getFluids();
            if (stacks.length == 1) result.add(RateCalculatorResource.fluid(stacks[0], 10_000, 10_000, perTick, true,
                    true, "OR", registries));
            else result.add(RateCalculatorResource.unknown("fluid", fluidKey(stacks),
                    stacks.length == 0 ? 0 : stacks[0].getAmount(), 0, 0, perTick, true, "OR",
                    RateCalculatorResource.fluidAlternatives(stacks, registries)));
        }
        return result;
    }

    private static List<RateCalculatorResource> resolveConsumedIdentities(RecipeLogic logic,
                                                                          List<RateCalculatorResource> configured,
                                                                          net.minecraft.core.HolderLookup.Provider registries) {
        Set<ResourceIdentity> consumed = new HashSet<>();
        for (var ingredient : logic.getConsumedInputs().getConsumedInputs(ItemRecipeCapability.CAP)) {
            for (ItemStack stack : ingredient.getItems()) {
                if (!stack.isEmpty()) consumed.add(ResourceIdentity.item(stack, registries));
            }
        }
        for (var ingredient : logic.getConsumedInputs().getConsumedInputs(FluidRecipeCapability.CAP)) {
            for (FluidStack stack : ingredient.getFluids()) {
                if (!stack.isEmpty()) consumed.add(ResourceIdentity.fluid(stack, registries));
            }
        }
        if (consumed.isEmpty()) return configured;
        List<RateCalculatorResource> resolved = new ArrayList<>(configured.size());
        for (RateCalculatorResource resource : configured) {
            if (resource.known()) {
                resolved.add(resource);
                continue;
            }
            Set<String> alternativeIds = resource.alternatives().stream()
                    .map(RateCalculatorResource.Alternative::id).collect(java.util.stream.Collectors.toSet());
            List<ResourceIdentity> matches = consumed.stream()
                    .filter(identity -> identity.kind().equals(resource.kind()) &&
                            alternativeIds.contains(identity.id()))
                    .toList();
            resolved.add(matches.size() == 1 ? resource.resolved(matches.getFirst().alternative()) : resource);
        }
        return resolved;
    }

    private static boolean hasUnknown(List<RateCalculatorResource> resources) {
        return resources.stream().anyMatch(resource -> !resource.known());
    }

    private static String itemKey(ItemStack[] stacks) {
        return java.util.Arrays.stream(stacks).map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .sorted().reduce("ingredient", (left, right) -> left + "|" + right);
    }

    private static String fluidKey(FluidStack[] stacks) {
        return java.util.Arrays.stream(stacks).map(stack -> BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString())
                .sorted().reduce("ingredient", (left, right) -> left + "|" + right);
    }

    private record ResourceIdentity(String kind, String id, net.minecraft.nbt.Tag icon) {

        private static ResourceIdentity item(ItemStack stack, net.minecraft.core.HolderLookup.Provider registries) {
            RateCalculatorResource resource = RateCalculatorResource.item(stack, 10_000, 10_000, false, true, true,
                    "OR", registries);
            return new ResourceIdentity(resource.kind(), resource.id(), resource.icon());
        }

        private static ResourceIdentity fluid(FluidStack stack, net.minecraft.core.HolderLookup.Provider registries) {
            RateCalculatorResource resource = RateCalculatorResource.fluid(stack, 10_000, 10_000, false, true, true,
                    "OR", registries);
            return new ResourceIdentity(resource.kind(), resource.id(), resource.icon());
        }

        private RateCalculatorResource.Alternative alternative() {
            return new RateCalculatorResource.Alternative(id, icon);
        }
    }

    private static final class WatchedMachine {

        private final RateCalculatorMachineSnapshot snapshot;
        private int references;
        private GTRecipe cycleRecipe;
        private GTRecipe tickRecipe;
        private List<RateCalculatorResource> cycleInputs = List.of();
        private List<RateCalculatorResource> cycleReturns = List.of();
        private List<RateCalculatorResource> tickInputs = List.of();
        private List<RateCalculatorResource> tickReturns = List.of();

        private WatchedMachine(RateCalculatorMachineSnapshot snapshot) {
            this.snapshot = snapshot;
        }
    }
}
