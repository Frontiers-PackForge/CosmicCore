package com.ghostipedia.cosmiccore.common.transmission.upgrade;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.item.PowerTowerCoilItem;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMachine;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerGraph;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSpan;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PowerTowerUpgradeService {

    private PowerTowerUpgradeService() {}

    public static Result setTarget(ServerPlayer player, PowerTowerMachine tower, UUID expectedComponent,
                                   long expectedVersion, int targetTier) {
        Context context = context(player, tower, expectedComponent, expectedVersion);
        if (context == null || coilItem(targetTier) == null) return Result.rejected(Status.STALE_OR_DENIED);
        try {
            if (context.graph.setUpgradeTarget(context.nodeId, targetTier)) context.data.markGraphDirty();
        } catch (IllegalArgumentException exception) {
            return Result.rejected(Status.INVALID_TARGET);
        }
        var updated = context.graph.componentContainingNode(context.nodeId);
        return new Result(Status.TARGET_SET, 0, updated == null ? 0 : updated.remainingConnections());
    }

    public static Result upgradeAvailable(ServerPlayer player, PowerTowerMachine tower, UUID expectedComponent,
                                          long expectedVersion, int targetTier) {
        Context context = context(player, tower, expectedComponent, expectedVersion);
        if (context == null) return Result.rejected(Status.STALE_OR_DENIED);
        PowerTowerGraph.ComponentSnapshot component = context.graph.componentContainingNode(context.nodeId);
        if (component == null || component.upgradeTargetTier() != targetTier || coilItem(targetTier) == null)
            return Result.rejected(Status.INVALID_TARGET);
        StagedInventory inventory = new StagedInventory(player.getInventory());
        List<UUID> selected = planBatch(component, targetTier, inventory);
        if (selected.isEmpty()) return Result.rejected(Status.NO_AVAILABLE_EXCHANGE);
        try {
            if (!context.graph.upgradeSpans(expectedComponent, expectedVersion, targetTier, selected))
                return Result.rejected(Status.STALE_OR_DENIED);
        } catch (IllegalArgumentException exception) {
            return Result.rejected(Status.STALE_OR_DENIED);
        }
        inventory.apply();
        context.data.markGraphDirty();
        context.data.loadedTerminals().wakeComponentTerminals(context.graph, context.nodeId);
        var updated = context.graph.componentContainingNode(context.nodeId);
        return new Result(Status.UPGRADED, selected.size(), updated == null ? 0 : updated.remainingConnections());
    }

    static List<UUID> planBatch(PowerTowerGraph.ComponentSnapshot component, int targetTier,
                                CoilExchange inventory) {
        List<UUID> selected = new ArrayList<>();
        List<PowerTowerSpan> eligible = component.spans().values().stream()
                .filter(span -> span.cableVoltageTier() < targetTier)
                .sorted(Comparator.comparing(PowerTowerSpan::id)).toList();
        for (PowerTowerSpan span : eligible) {
            if (!inventory.exchange(span.cableVoltageTier(), targetTier)) break;
            selected.add(span.id());
        }
        return List.copyOf(selected);
    }

    private static Context context(ServerPlayer player, PowerTowerMachine tower, UUID expectedComponent,
                                   long expectedVersion) {
        if (!tower.canConfigure(player) || !(tower.getLevel() instanceof net.minecraft.server.level.ServerLevel level))
            return null;
        UUID nodeId = tower.getGraphNodeId();
        if (nodeId == null) return null;
        PowerTowerSavedData data = PowerTowerSavedData.getOrCreate(level);
        PowerTowerGraph graph = data.graph();
        return expectedComponent != null && expectedComponent.equals(graph.componentIdContainingNode(nodeId)) &&
                expectedVersion == graph.componentVersionContainingNode(nodeId) ?
                        new Context(data, graph, nodeId) : null;
    }

    private static Item coilItem(int tier) {
        for (var entry : CosmicItems.POWER_TOWER_COILS) {
            PowerTowerCoilItem coil = entry.get();
            if (coil.getVoltageTier() == tier) return coil;
        }
        return null;
    }

    interface CoilExchange {

        boolean exchange(int oldTier, int targetTier);
    }

    private static final class StagedInventory implements CoilExchange {

        private final Inventory inventory;
        private final List<ItemStack> slots;

        private StagedInventory(Inventory inventory) {
            this.inventory = inventory;
            slots = inventory.items.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(
                    ArrayList::new));
        }

        @Override
        public boolean exchange(int oldTier, int targetTier) {
            Item oldCoil = coilItem(oldTier);
            if (oldCoil == null) return false;
            int targetSlot = -1;
            for (int slot = 0; slot < slots.size(); slot++) {
                if (slots.get(slot).getItem() instanceof PowerTowerCoilItem coil &&
                        coil.getVoltageTier() == targetTier && !slots.get(slot).isEmpty()) {
                    targetSlot = slot;
                    break;
                }
            }
            if (targetSlot < 0) return false;
            ItemStack refund = new ItemStack(oldCoil);
            int refundSlot = -1;
            for (int slot = 0; slot < slots.size(); slot++) {
                ItemStack current = slots.get(slot);
                if (ItemStack.isSameItemSameComponents(current, refund) &&
                        current.getCount() < current.getMaxStackSize()) {
                    refundSlot = slot;
                    break;
                }
            }
            if (refundSlot < 0) {
                for (int slot = 0; slot < slots.size(); slot++) {
                    if (slots.get(slot).isEmpty() || slot == targetSlot && slots.get(slot).getCount() == 1) {
                        refundSlot = slot;
                        break;
                    }
                }
            }
            if (refundSlot < 0) return false;
            slots.get(targetSlot).shrink(1);
            if (slots.get(targetSlot).isEmpty()) slots.set(targetSlot, ItemStack.EMPTY);
            ItemStack destination = slots.get(refundSlot);
            if (destination.isEmpty()) slots.set(refundSlot, refund);
            else destination.grow(1);
            return true;
        }

        private void apply() {
            for (int slot = 0; slot < slots.size(); slot++) inventory.items.set(slot, slots.get(slot));
            inventory.setChanged();
        }
    }

    private record Context(PowerTowerSavedData data, PowerTowerGraph graph, UUID nodeId) {}

    public enum Status {
        TARGET_SET,
        UPGRADED,
        NO_AVAILABLE_EXCHANGE,
        INVALID_TARGET,
        STALE_OR_DENIED
    }

    public record Result(Status status, int upgradedConnections, int remainingConnections) {

        private static Result rejected(Status status) {
            return new Result(status, 0, 0);
        }
    }
}
