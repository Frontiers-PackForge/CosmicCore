package com.ghostipedia.cosmiccore.common.rate;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

public final class RateCalculatorConsumption implements AutoCloseable {

    private static final ThreadLocal<RateCalculatorConsumption> CURRENT = new ThreadLocal<>();
    private final RateCalculatorConsumption previous;
    private final HolderLookup.Provider registries;
    private final boolean enabled;
    private final boolean perTick;
    private final Map<String, Long> expected = new LinkedHashMap<>();
    private final Map<String, Long> handled = new LinkedHashMap<>();
    private final List<RateCalculatorResource> removed = new ArrayList<>();
    private final List<RateCalculatorResource> returned = new ArrayList<>();
    private boolean complete = true;
    private int depth;

    RateCalculatorConsumption(HolderLookup.Provider registries, boolean enabled, boolean perTick) {
        this.previous = CURRENT.get();
        this.registries = registries;
        this.enabled = enabled;
        this.perTick = perTick;
        CURRENT.set(this);
    }

    public static void expect(Map<RecipeCapability<?>, List<Object>> contents) {
        RateCalculatorConsumption frame = CURRENT.get();
        if (frame == null || !frame.enabled) return;
        contents.forEach((capability, values) -> {
            if (!values.isEmpty() && material(capability)) frame.expect(kind(capability), quantity(capability, values));
        });
    }

    void expect(String kind, long amount) {
        expected.put(kind, amount);
    }

    public static List<?> handle(IRecipeHandler<?> handler, IO io, GTRecipe recipe, List<?> left,
                                 boolean simulate, Supplier<List<?>> operation) {
        RateCalculatorConsumption frame = CURRENT.get();
        if (frame == null || !frame.enabled || io != IO.IN || simulate ||
                !material(handler.getCapability()) || frame.depth != 0)
            return operation.get();
        String kind = kind(handler.getCapability());
        return measure(kind, true, false, quantity(handler.getCapability(), left),
                recipe != null && !recipe.ingredientActions.isEmpty(),
                () -> frame.contents(handler.getContents(), kind), operation,
                result -> quantity(handler.getCapability(), result));
    }

    public static HandlingSnapshot snapshot(RecipeCapability<?> capability, List<?> contents) {
        return new HandlingSnapshot(material(capability) ? List.of() : copy(capability, contents),
                material(capability) ? quantity(capability, contents) : 0);
    }

    public static boolean handled(RecipeCapability<?> capability, HandlingSnapshot requested, List<?> remainder) {
        return material(capability) ? quantity(capability, remainder) < requested.quantity() :
                !requested.contents().isEmpty() && (remainder == null || !requested.contents().equals(remainder));
    }

    static <T> T measure(String kind, boolean input, boolean simulate, long requested, boolean retained,
                         Supplier<List<RateCalculatorResource>> inventory, Supplier<T> operation,
                         ToLongFunction<T> remainder) {
        RateCalculatorConsumption frame = CURRENT.get();
        if (frame == null || !frame.enabled || !input || simulate || frame.depth != 0) return operation.get();
        frame.depth++;
        try {
            List<RateCalculatorResource> before = read(inventory);
            T result = null;
            boolean completed = false;
            try {
                result = operation.get();
                completed = true;
                return result;
            } finally {
                List<RateCalculatorResource> after = read(inventory);
                if (before != null && after != null) {
                    List<RateCalculatorResource> removals = difference(before, after);
                    frame.removed.addAll(removals);
                    frame.returned.addAll(difference(after, before));
                    if (completed) {
                        long consumed = Math.max(0, requested - remainder.applyAsLong(result));
                        frame.handled.merge(kind, consumed, Long::sum);
                        long removed = removals.stream().mapToLong(RateCalculatorResource::amount).sum();
                        if (removed != consumed && !retained) frame.complete = false;
                    }
                } else frame.complete = false;
            }
        } finally {
            frame.depth--;
        }
    }

    private static List<RateCalculatorResource> read(Supplier<List<RateCalculatorResource>> inventory) {
        try {
            return inventory.get();
        } catch (RuntimeException exception) {
            return null;
        }
    }

    List<RateCalculatorResource> contents(List<?> contents, String kind) {
        List<RateCalculatorResource> result = new ArrayList<>();
        for (Object content : contents) {
            if (content instanceof ItemStack stack && kind.equals("item")) {
                if (!stack.isEmpty()) result.add(RateCalculatorResource.item(stack, 10_000, 10_000, perTick,
                        true, true, "OR", registries));
            } else if (content instanceof FluidStack stack && kind.equals("fluid")) {
                if (!stack.isEmpty()) result.add(RateCalculatorResource.fluid(stack, 10_000, 10_000, perTick,
                        true, true, "OR", registries));
            } else return null;
        }
        return result;
    }

    static List<RateCalculatorResource> difference(List<RateCalculatorResource> before,
                                                   List<RateCalculatorResource> after) {
        Map<Key, RateCalculatorResource> remaining = new LinkedHashMap<>();
        for (RateCalculatorResource resource : before) {
            remaining.merge(Key.of(resource), resource, (left, right) -> left.add(right.amount()));
        }
        for (RateCalculatorResource resource : after) {
            remaining.computeIfPresent(Key.of(resource), (key, value) -> value.add(-resource.amount()));
        }
        return remaining.values().stream().filter(resource -> resource.amount() > 0).toList();
    }

    List<RateCalculatorResource> removed() {
        return List.copyOf(removed);
    }

    List<RateCalculatorResource> returned() {
        return List.copyOf(returned);
    }

    boolean complete() {
        return complete && expected.entrySet().stream()
                .allMatch(entry -> handled.getOrDefault(entry.getKey(), 0L).equals(entry.getValue()));
    }

    private static long quantity(RecipeCapability<?> capability, List<?> contents) {
        if (contents == null) return 0;
        long total = 0;
        for (Object content : contents) {
            if (capability == ItemRecipeCapability.CAP) total += ItemRecipeCapability.CAP.of(content).count();
            else if (capability == FluidRecipeCapability.CAP) total += FluidRecipeCapability.CAP.of(content).amount();
        }
        return total;
    }

    private static List<?> copy(RecipeCapability<?> capability, List<?> contents) {
        List<Object> copy = new ArrayList<>(contents.size());
        for (Object content : contents) copy.add(copy(capability, content));
        return copy;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object copy(RecipeCapability<?> capability, Object content) {
        RecipeCapability raw = capability;
        return raw.copyInner(raw.of(content));
    }

    private static boolean material(RecipeCapability<?> capability) {
        return capability == ItemRecipeCapability.CAP || capability == FluidRecipeCapability.CAP;
    }

    private static String kind(RecipeCapability<?> capability) {
        return capability == ItemRecipeCapability.CAP ? "item" : "fluid";
    }

    @Override
    public void close() {
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }

    private record Key(String kind, String id, Tag icon) {

        static Key of(RateCalculatorResource resource) {
            return new Key(resource.kind(), resource.id(), resource.icon());
        }
    }

    public record HandlingSnapshot(List<?> contents, long quantity) {}
}
