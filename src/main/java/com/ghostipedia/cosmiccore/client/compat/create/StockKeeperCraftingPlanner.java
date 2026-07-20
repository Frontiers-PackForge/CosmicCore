package com.ghostipedia.cosmiccore.client.compat.create;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class StockKeeperCraftingPlanner {

    private static final int MAX_CRAFTS = 1_000_000;
    private static final int MAX_FLOW = MAX_CRAFTS * 9;
    private static final ThreadLocal<RequestState> REQUEST = new ThreadLocal<>();
    private static final ThreadLocal<CapacityPass> CAPACITY_PASS = new ThreadLocal<>();

    private StockKeeperCraftingPlanner() {}

    public static void beginRequest(CraftableBigItemStack recipe, int requestedDifference) {
        REQUEST.set(new RequestState(recipe, requestedDifference));
    }

    public static void endRequest() {
        REQUEST.remove();
    }

    public static void beginCapacityPass() {
        CAPACITY_PASS.set(new CapacityPass(true));
    }

    public static void endCapacityPass() {
        CAPACITY_PASS.remove();
    }

    @Nullable
    public static DirectRequest takeRequest(CraftableBigItemStack recipe, int outputCount) {
        RequestState state = REQUEST.get();
        if (state == null || state.consumed || state.recipe != recipe) {
            return null;
        }
        state.consumed = true;
        int cycles = ceilDiv(Math.abs(state.requestedDifference), outputCount);
        return new DirectRequest(state.requestedDifference < 0, cycles);
    }

    @Nullable
    public static Pair<Integer, List<List<BigItemStack>>> plan(
                                                               CraftableBigItemStack recipe,
                                                               InventorySummary summary,
                                                               Function<ItemStack, Integer> countModifier,
                                                               List<BigItemStack> currentOrders,
                                                               List<CraftableBigItemStack> allRecipes,
                                                               @Nullable Player player,
                                                               int newTypeLimit,
                                                               @Nullable DirectRequest directRequest,
                                                               int outputCount) {
        List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty())
                .toList();
        if (!hasReusableIngredient(ingredients, summary, currentOrders, player)) {
            return null;
        }

        if (directRequest == null) {
            return planCurrentCapacity(
                    ingredients, summary, countModifier, currentOrders, player, outputCount);
        }

        boolean ownsCapacityPass = CAPACITY_PASS.get() == null;
        if (ownsCapacityPass) {
            CAPACITY_PASS.set(new CapacityPass(false));
            reserveOtherPlans(recipe, allRecipes, currentOrders, player, directRequest.removing);
        }

        try {
            int currentCycles = recipe.count / outputCount;
            if (directRequest.removing) {
                return planRemoval(
                        ingredients,
                        summary,
                        countModifier,
                        currentOrders,
                        player,
                        currentCycles,
                        Math.min(currentCycles, directRequest.cycles),
                        outputCount);
            }

            return planAddition(
                    ingredients,
                    summary,
                    countModifier,
                    currentOrders,
                    player,
                    currentCycles,
                    directRequest.cycles,
                    newTypeLimit,
                    outputCount);
        } finally {
            if (ownsCapacityPass) {
                CAPACITY_PASS.remove();
            }
        }
    }

    public static boolean hasReusableIngredient(
                                                CraftableBigItemStack recipe, List<BigItemStack> currentOrders,
                                                @Nullable Player player) {
        List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty())
                .toList();
        if (ingredients.stream().anyMatch(StockKeeperCraftingPlanner::ingredientCanReuse)) {
            return true;
        }
        for (BigItemStack order : currentOrders) {
            if (usesPerItem(order.stack, ingredients) > 1) {
                return true;
            }
        }
        return player != null && !reusablePlayerStacks(player, ingredients).isEmpty();
    }

    public static List<ItemStack> reusablePlayerStacks(Player player, List<Ingredient> ingredients) {
        List<ItemStack> reusable = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && usesPerItem(stack, ingredients) > 1) {
                reusable.add(stack.copyWithCount(1));
            }
        }
        return reusable;
    }

    private static Pair<Integer, List<List<BigItemStack>>> planAddition(
                                                                        List<Ingredient> ingredients,
                                                                        InventorySummary summary,
                                                                        Function<ItemStack, Integer> countModifier,
                                                                        List<BigItemStack> currentOrders,
                                                                        @Nullable Player player,
                                                                        int currentCycles,
                                                                        int requestedCycles,
                                                                        int newTypeLimit,
                                                                        int outputCount) {
        List<Supply> supplies = new ArrayList<>();
        addReusableOrderSupplies(supplies, currentOrders, ingredients, false);
        addPlayerSupplies(supplies, player, ingredients);
        addSummarySupplies(supplies, summary, countModifier, ingredients, true);

        boolean[] reusableIngredients = reusableIngredients(ingredients, supplies);
        int low = 0;
        int high = requestedCycles;
        Allocation best = allocate(
                ingredients,
                supplies,
                additionDemands(reusableIngredients, currentCycles, 0),
                currentOrders,
                newTypeLimit);

        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            Allocation allocation = allocate(
                    ingredients,
                    supplies,
                    additionDemands(reusableIngredients, currentCycles, candidate),
                    currentOrders,
                    newTypeLimit);
            if (allocation == null) {
                high = candidate - 1;
            } else {
                low = candidate;
                best = allocation;
            }
        }

        if (low == 0 || best == null) {
            return Pair.of(0, List.of());
        }
        return Pair.of(low * outputCount, best.entries);
    }

    private static Pair<Integer, List<List<BigItemStack>>> planRemoval(
                                                                       List<Ingredient> ingredients,
                                                                       InventorySummary summary,
                                                                       Function<ItemStack, Integer> countModifier,
                                                                       List<BigItemStack> currentOrders,
                                                                       @Nullable Player player,
                                                                       int currentCycles,
                                                                       int requestedCycles,
                                                                       int outputCount) {
        List<Supply> supplies = new ArrayList<>();
        addSummarySupplies(supplies, summary, countModifier, ingredients, true);
        addPlayerSupplies(supplies, player, ingredients);

        Allocation current = allocate(ingredients, supplies, uniformDemands(ingredients.size(), currentCycles),
                currentOrders, -1);
        if (current == null) {
            return Pair.of(0, List.of());
        }

        int targetCycles = currentCycles - requestedCycles;
        Allocation target = allocate(ingredients, supplies, uniformDemands(ingredients.size(), targetCycles),
                currentOrders, -1);
        if (target == null) {
            return Pair.of(0, List.of());
        }

        return Pair.of(requestedCycles * outputCount, subtractEntries(current.entries, target.entries));
    }

    private static Pair<Integer, List<List<BigItemStack>>> planCurrentCapacity(
                                                                               List<Ingredient> ingredients,
                                                                               InventorySummary summary,
                                                                               Function<ItemStack, Integer> countModifier,
                                                                               List<BigItemStack> currentOrders,
                                                                               @Nullable Player player,
                                                                               int outputCount) {
        List<Supply> supplies = new ArrayList<>();
        addSummarySupplies(supplies, summary, countModifier, ingredients, true);
        addPlayerSupplies(supplies, player, ingredients);

        int high = upperBound(ingredients, supplies);
        int low = 0;
        Allocation best = allocate(ingredients, supplies, uniformDemands(ingredients.size(), 0), currentOrders, -1);
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            Allocation allocation = allocate(ingredients, supplies, uniformDemands(ingredients.size(), candidate),
                    currentOrders, -1);
            if (allocation == null) {
                high = candidate - 1;
            } else {
                low = candidate;
                best = allocation;
            }
        }

        if (best == null) {
            return Pair.of(0, List.of());
        }
        CapacityPass capacityPass = CAPACITY_PASS.get();
        if (capacityPass != null) {
            capacityPass.commit(best.consumptions);
        }
        return Pair.of(low * outputCount, best.entries);
    }

    private static boolean hasReusableIngredient(
                                                 List<Ingredient> ingredients,
                                                 InventorySummary summary,
                                                 List<BigItemStack> currentOrders,
                                                 @Nullable Player player) {
        if (ingredients.stream().anyMatch(StockKeeperCraftingPlanner::ingredientCanReuse)) {
            return true;
        }
        for (BigItemStack stack : summary.getStacks()) {
            if (usesPerItem(stack.stack, ingredients) > 1) {
                return true;
            }
        }
        for (BigItemStack stack : currentOrders) {
            if (usesPerItem(stack.stack, ingredients) > 1) {
                return true;
            }
        }
        return player != null && !reusablePlayerStacks(player, ingredients).isEmpty();
    }

    private static boolean ingredientCanReuse(Ingredient ingredient) {
        for (ItemStack stack : ingredient.getItems()) {
            if (usesPerItem(stack, List.of(ingredient)) > 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean[] reusableIngredients(List<Ingredient> ingredients, List<Supply> supplies) {
        boolean[] reusable = new boolean[ingredients.size()];
        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            Ingredient ingredient = ingredients.get(ingredientIndex);
            reusable[ingredientIndex] = ingredientCanReuse(ingredient);
            if (reusable[ingredientIndex]) {
                continue;
            }
            for (Supply supply : supplies) {
                if (supply.uses > 1 && ingredient.test(supply.stack)) {
                    reusable[ingredientIndex] = true;
                    break;
                }
            }
        }
        return reusable;
    }

    private static int[] additionDemands(boolean[] reusableIngredients, int currentCycles, int addedCycles) {
        int[] demands = new int[reusableIngredients.length];
        for (int index = 0; index < demands.length; index++) {
            demands[index] = reusableIngredients[index] ? currentCycles + addedCycles : addedCycles;
        }
        return demands;
    }

    private static int[] uniformDemands(int size, int cycles) {
        int[] demands = new int[size];
        Arrays.fill(demands, cycles);
        return demands;
    }

    private static void addReusableOrderSupplies(
                                                 List<Supply> supplies, List<BigItemStack> orders,
                                                 List<Ingredient> ingredients, boolean output) {
        for (BigItemStack order : orders) {
            int uses = usesPerItem(order.stack, ingredients);
            if (uses > 1 && order.count > 0) {
                addSupply(supplies, order.stack, order.count, uses, output);
            }
        }
    }

    private static void reserveOtherPlans(
                                          CraftableBigItemStack currentRecipe,
                                          List<CraftableBigItemStack> allRecipes,
                                          List<BigItemStack> currentOrders,
                                          @Nullable Player player,
                                          boolean orderedOutput) {
        CapacityPass capacityPass = CAPACITY_PASS.get();
        if (capacityPass == null || player == null) {
            return;
        }
        for (CraftableBigItemStack recipe : allRecipes) {
            if (recipe == currentRecipe || recipe.count <= 0) {
                continue;
            }
            int outputCount = recipe.getOutputCount(player.level());
            int cycles = recipe.count / outputCount;
            List<Ingredient> ingredients = recipe.getIngredients().stream()
                    .filter(ingredient -> !ingredient.isEmpty())
                    .toList();
            List<Supply> supplies = new ArrayList<>();
            addReusableOrderSupplies(supplies, currentOrders, ingredients, orderedOutput);
            addPlayerSupplies(supplies, player, ingredients);
            boolean[] reusable = reusableIngredients(ingredients, supplies);
            int[] demands = new int[ingredients.size()];
            for (int index = 0; index < demands.length; index++) {
                demands[index] = reusable[index] ? cycles : 0;
            }
            Allocation allocation = allocate(ingredients, supplies, demands, currentOrders, -1);
            if (allocation != null) {
                capacityPass.commit(allocation.consumptions);
            }
        }
    }

    private static void addPlayerSupplies(
                                          List<Supply> supplies, @Nullable Player player,
                                          List<Ingredient> ingredients) {
        if (player == null) {
            return;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int uses = usesPerItem(stack, ingredients);
            if (uses > 1) {
                addSupply(supplies, stack, stack.getCount(), uses, false);
            }
        }
    }

    private static void addSummarySupplies(
                                           List<Supply> supplies,
                                           InventorySummary summary,
                                           Function<ItemStack, Integer> countModifier,
                                           List<Ingredient> ingredients,
                                           boolean output) {
        for (BigItemStack entry : summary.getStacks()) {
            int uses = usesPerItem(entry.stack, ingredients);
            int count = summary.getCountOf(entry.stack);
            CapacityPass capacityPass = CAPACITY_PASS.get();
            if (capacityPass == null || uses <= 1 || !capacityPass.ignoreReusableCountModifier) {
                count = boundedAdd(count, countModifier.apply(entry.stack));
            }
            if (count <= 0) {
                continue;
            }
            addSupply(supplies, entry.stack, count, uses, output);
        }
    }

    private static void addSupply(
                                  List<Supply> supplies, ItemStack stack, int count, int uses, boolean output) {
        if (uses == 0) {
            return;
        }
        for (Supply supply : supplies) {
            if (supply.output == output && supply.uses == uses &&
                    ItemStack.isSameItemSameComponents(supply.stack, stack)) {
                supply.physicalCount = boundedAdd(supply.physicalCount, count);
                return;
            }
        }
        CapacityPass capacityPass = CAPACITY_PASS.get();
        int reservedUses = capacityPass != null && uses > 1 ? capacityPass.reservedUses(stack, output) : 0;
        supplies.add(new Supply(
                stack.copyWithCount(1), Math.min(count, BigItemStack.INF), uses, output, reservedUses));
    }

    private static int usesPerItem(ItemStack stack, List<Ingredient> ingredients) {
        boolean matches = false;
        int uses = MAX_CRAFTS;
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.test(stack)) {
                continue;
            }
            matches = true;
            uses = Math.min(uses, usesForIngredient(stack, ingredient));
        }
        return matches ? uses : 0;
    }

    private static int usesForIngredient(ItemStack stack, Ingredient ingredient) {
        if (!stack.hasCraftingRemainingItem()) {
            return 1;
        }
        if (stack.getItem() instanceof IGTTool tool) {
            if (stack.has(DataComponents.UNBREAKABLE)) {
                return MAX_CRAFTS;
            }
            int remaining = Math.max(1, tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1);
            int damage = Math.max(1, tool.getToolStats().getToolDamagePerCraft(stack));
            return Math.max(1, ceilDiv(remaining, damage));
        }

        ItemStack remainder = stack.getCraftingRemainingItem();
        if (remainder.isEmpty() || !ingredient.test(remainder)) {
            return 1;
        }
        if (!remainder.isDamageableItem() &&
                ItemStack.isSameItemSameComponents(stack, remainder)) {
            return MAX_CRAFTS;
        }
        if (remainder.is(stack.getItem()) && remainder.getDamageValue() > stack.getDamageValue()) {
            int damage = remainder.getDamageValue() - stack.getDamageValue();
            int remaining = Math.max(1, stack.getMaxDamage() - stack.getDamageValue());
            return Math.max(1, ceilDiv(remaining, damage));
        }
        return 1;
    }

    @Nullable
    private static Allocation allocate(
                                       List<Ingredient> ingredients,
                                       List<Supply> supplies,
                                       int[] demands,
                                       List<BigItemStack> currentOrders,
                                       int newTypeLimit) {
        int source = 0;
        int supplyOffset = 1;
        int ingredientOffset = supplyOffset + supplies.size();
        int sink = ingredientOffset + ingredients.size();
        FlowNetwork network = new FlowNetwork(sink + 1);
        FlowEdge[][] supplyEdges = new FlowEdge[supplies.size()][ingredients.size()];
        int totalDemand = 0;

        for (int supplyIndex = 0; supplyIndex < supplies.size(); supplyIndex++) {
            Supply supply = supplies.get(supplyIndex);
            network.addEdge(source, supplyOffset + supplyIndex, supply.availableUses());
            for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
                if (!ingredients.get(ingredientIndex).test(supply.stack)) {
                    continue;
                }
                supplyEdges[supplyIndex][ingredientIndex] = network.addEdge(supplyOffset + supplyIndex,
                        ingredientOffset + ingredientIndex, MAX_CRAFTS);
            }
        }

        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            int demand = Math.min(MAX_CRAFTS, Math.max(0, demands[ingredientIndex]));
            totalDemand = flowAdd(totalDemand, demand);
            network.addEdge(ingredientOffset + ingredientIndex, sink, demand);
        }

        if (network.maxFlow(source, sink) != totalDemand) {
            return null;
        }

        List<List<BigItemStack>> entries = new ArrayList<>();
        for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            entries.add(new ArrayList<>());
        }
        Set<StackKey> newTypes = new HashSet<>();
        List<CapacityUse> consumptions = new ArrayList<>();

        for (int supplyIndex = 0; supplyIndex < supplies.size(); supplyIndex++) {
            Supply supply = supplies.get(supplyIndex);
            int totalUses = 0;
            for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
                FlowEdge edge = supplyEdges[supplyIndex][ingredientIndex];
                if (edge != null) {
                    totalUses = boundedAdd(totalUses, edge.flow());
                }
            }
            if (totalUses == 0) {
                continue;
            }
            if (supply.uses > 1) {
                consumptions.add(new CapacityUse(supply.stack, supply.output, totalUses));
            }
            if (!supply.output) {
                continue;
            }

            int physical = Math.min(supply.physicalCount, supply.physicalDelta(totalUses));
            if (physical == 0) {
                continue;
            }

            if (newTypeLimit != -1 && !containsStack(currentOrders, supply.stack)) {
                newTypes.add(new StackKey(supply.stack));
                if (newTypes.size() > newTypeLimit) {
                    return null;
                }
            }

            int remainingPhysical = physical;
            for (int ingredientIndex = 0; ingredientIndex < ingredients.size() &&
                    remainingPhysical > 0; ingredientIndex++) {
                FlowEdge edge = supplyEdges[supplyIndex][ingredientIndex];
                if (edge == null || edge.flow() == 0) {
                    continue;
                }
                int assigned = Math.min(remainingPhysical, ceilDiv(edge.flow(), supply.uses));
                mergeEntry(entries.get(ingredientIndex), supply.stack, assigned);
                remainingPhysical -= assigned;
            }
        }

        return new Allocation(entries, consumptions);
    }

    private static int upperBound(List<Ingredient> ingredients, List<Supply> supplies) {
        int upper = MAX_CRAFTS;
        for (Ingredient ingredient : ingredients) {
            int capacity = 0;
            for (Supply supply : supplies) {
                if (ingredient.test(supply.stack)) {
                    capacity = boundedAdd(capacity, supply.availableUses());
                }
            }
            upper = Math.min(upper, capacity);
        }
        return upper;
    }

    private static List<List<BigItemStack>> subtractEntries(
                                                            List<List<BigItemStack>> current,
                                                            List<List<BigItemStack>> target) {
        List<List<BigItemStack>> difference = new ArrayList<>();
        for (int ingredientIndex = 0; ingredientIndex < current.size(); ingredientIndex++) {
            List<BigItemStack> entries = new ArrayList<>();
            for (BigItemStack currentEntry : current.get(ingredientIndex)) {
                int targetCount = countOf(target.get(ingredientIndex), currentEntry.stack);
                int count = Math.max(0, currentEntry.count - targetCount);
                if (count > 0) {
                    entries.add(new BigItemStack(currentEntry.stack.copyWithCount(1), count));
                }
            }
            difference.add(entries);
        }
        return difference;
    }

    private static int countOf(List<BigItemStack> entries, ItemStack stack) {
        for (BigItemStack entry : entries) {
            if (ItemStack.isSameItemSameComponents(entry.stack, stack)) {
                return entry.count;
            }
        }
        return 0;
    }

    private static void mergeEntry(List<BigItemStack> entries, ItemStack stack, int count) {
        if (count <= 0) {
            return;
        }
        for (BigItemStack entry : entries) {
            if (ItemStack.isSameItemSameComponents(entry.stack, stack)) {
                entry.count = boundedAdd(entry.count, count);
                return;
            }
        }
        entries.add(new BigItemStack(stack.copyWithCount(1), count));
    }

    private static boolean containsStack(List<BigItemStack> entries, ItemStack stack) {
        return entries.stream().anyMatch(entry -> ItemStack.isSameItemSameComponents(entry.stack, stack));
    }

    private static int boundedAdd(int first, int second) {
        return (int) Math.max(0, Math.min(MAX_CRAFTS, (long) first + second));
    }

    private static int boundedMultiply(int first, int second) {
        return (int) Math.min(MAX_CRAFTS, (long) first * second);
    }

    private static int flowAdd(int first, int second) {
        return (int) Math.min(MAX_FLOW, (long) first + second);
    }

    private static int ceilDiv(int value, int divisor) {
        return value == 0 ? 0 : 1 + (value - 1) / divisor;
    }

    public record DirectRequest(boolean removing, int cycles) {}

    private record Allocation(List<List<BigItemStack>> entries, List<CapacityUse> consumptions) {}

    private record CapacityUse(ItemStack stack, boolean output, int uses) {}

    private record CapacityKey(StackKey stack, boolean output) {}

    private record StackKey(ItemStack stack) {

        @Override
        public boolean equals(Object object) {
            return object instanceof StackKey other && ItemStack.isSameItemSameComponents(stack, other.stack);
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(stack);
        }
    }

    private static final class RequestState {

        private final CraftableBigItemStack recipe;
        private final int requestedDifference;
        private boolean consumed;

        private RequestState(CraftableBigItemStack recipe, int requestedDifference) {
            this.recipe = recipe;
            this.requestedDifference = requestedDifference;
        }
    }

    private static final class Supply {

        private final ItemStack stack;
        private int physicalCount;
        private final int uses;
        private final boolean output;
        private final int reservedUses;

        private Supply(ItemStack stack, int physicalCount, int uses, boolean output, int reservedUses) {
            this.stack = stack;
            this.physicalCount = physicalCount;
            this.uses = uses;
            this.output = output;
            this.reservedUses = reservedUses;
        }

        private int availableUses() {
            return Math.max(0, boundedMultiply(physicalCount, uses) - reservedUses);
        }

        private int physicalDelta(int addedUses) {
            if (uses <= 1 || CAPACITY_PASS.get() == null) {
                return ceilDiv(addedUses, uses);
            }
            return ceilDiv(boundedAdd(reservedUses, addedUses), uses) - ceilDiv(reservedUses, uses);
        }
    }

    private static final class CapacityPass {

        private final Map<CapacityKey, Integer> reservedUses = new HashMap<>();
        private final boolean ignoreReusableCountModifier;

        private CapacityPass(boolean ignoreReusableCountModifier) {
            this.ignoreReusableCountModifier = ignoreReusableCountModifier;
        }

        private int reservedUses(ItemStack stack, boolean output) {
            return reservedUses.getOrDefault(new CapacityKey(new StackKey(stack), output), 0);
        }

        private void commit(List<CapacityUse> consumptions) {
            for (CapacityUse consumption : consumptions) {
                CapacityKey key = new CapacityKey(new StackKey(consumption.stack), consumption.output);
                reservedUses.merge(key, consumption.uses, StockKeeperCraftingPlanner::boundedAdd);
            }
        }
    }

    private static final class FlowNetwork {

        private final List<List<FlowEdge>> edges;
        private final int[] levels;
        private final int[] nextEdges;

        private FlowNetwork(int nodes) {
            edges = new ArrayList<>(nodes);
            for (int node = 0; node < nodes; node++) {
                edges.add(new ArrayList<>());
            }
            levels = new int[nodes];
            nextEdges = new int[nodes];
        }

        private FlowEdge addEdge(int from, int to, int capacity) {
            FlowEdge forward = new FlowEdge(to, edges.get(to).size(), capacity);
            FlowEdge reverse = new FlowEdge(from, edges.get(from).size(), 0);
            edges.get(from).add(forward);
            edges.get(to).add(reverse);
            return forward;
        }

        private int maxFlow(int source, int sink) {
            int flow = 0;
            while (buildLevels(source, sink)) {
                Arrays.fill(nextEdges, 0);
                int pushed;
                while ((pushed = push(source, sink, MAX_CRAFTS)) > 0) {
                    flow = flowAdd(flow, pushed);
                }
            }
            return flow;
        }

        private boolean buildLevels(int source, int sink) {
            Arrays.fill(levels, -1);
            levels[source] = 0;
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            while (!queue.isEmpty()) {
                int node = queue.removeFirst();
                for (FlowEdge edge : edges.get(node)) {
                    if (edge.capacity == 0 || levels[edge.to] != -1) {
                        continue;
                    }
                    levels[edge.to] = levels[node] + 1;
                    queue.addLast(edge.to);
                }
            }
            return levels[sink] != -1;
        }

        private int push(int node, int sink, int available) {
            if (node == sink) {
                return available;
            }
            List<FlowEdge> outgoing = edges.get(node);
            while (nextEdges[node] < outgoing.size()) {
                FlowEdge edge = outgoing.get(nextEdges[node]);
                if (edge.capacity > 0 && levels[edge.to] == levels[node] + 1) {
                    int pushed = push(edge.to, sink, Math.min(available, edge.capacity));
                    if (pushed > 0) {
                        edge.capacity -= pushed;
                        edges.get(edge.to).get(edge.reverseIndex).capacity += pushed;
                        return pushed;
                    }
                }
                nextEdges[node]++;
            }
            return 0;
        }
    }

    private static final class FlowEdge {

        private final int to;
        private final int reverseIndex;
        private final int initialCapacity;
        private int capacity;

        private FlowEdge(int to, int reverseIndex, int capacity) {
            this.to = to;
            this.reverseIndex = reverseIndex;
            this.capacity = capacity;
            initialCapacity = capacity;
        }

        private int flow() {
            return initialCapacity - capacity;
        }
    }
}
