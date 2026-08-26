package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EffortlessBuildingOperationExecutor {

    private EffortlessBuildingOperationExecutor() {}

    public static Result undo(
                              ServerPlayer player, ServerLevel level,
                              EffortlessBuildingOperationMetadata metadata) {
        Set<BlockPos> restored = new LinkedHashSet<>();
        for (Map.Entry<BlockPos, EffortlessBuildingBlockChange> entry : metadata.changes().entrySet()) {
            BlockPos pos = entry.getKey();
            EffortlessBuildingBlockChange change = entry.getValue();
            boolean dropsConsumed = false;
            try {
                if (!matchesAfter(level, pos, change)) continue;
                if (!player.isCreative()) {
                    if (!canConsumeAll(player, change.displacedDrops())) continue;
                    consumeAll(player, change.displacedDrops());
                    dropsConsumed = true;
                }
                if (!change.before().restore(level, pos)) {
                    restoreQuietly(level, pos, change.after());
                    giveAll(player, change.displacedDrops());
                    continue;
                }
                if (!player.isCreative()) giveOrDrop(player, change.placedItem());
                restored.add(pos);
            } catch (RuntimeException exception) {
                restoreQuietly(level, pos, change.after());
                if (dropsConsumed) giveAll(player, change.displacedDrops());
                CosmicCore.LOGGER.warn("Effortless Building undo failed at {}", pos, exception);
            }
        }
        if (metadata.pipeOperation() != null) {
            EffortlessBuildingGTPipeCompat.afterUndo(level, metadata.pipeOperation(), restored);
        }
        return new Result(restored.size(), restored);
    }

    public static Result redo(
                              ServerPlayer player, ServerLevel level,
                              EffortlessBuildingOperationMetadata metadata) {
        Set<BlockPos> restored = new LinkedHashSet<>();
        for (Map.Entry<BlockPos, EffortlessBuildingBlockChange> entry : metadata.changes().entrySet()) {
            BlockPos pos = entry.getKey();
            EffortlessBuildingBlockChange change = entry.getValue();
            boolean consumed = false;
            try {
                if (!change.before().matches(level, pos)) continue;
                if (!player.isCreative()) {
                    consumed = consumeExact(player, change.placedItem());
                    if (!consumed) continue;
                }
                if (!restoreAfter(player, level, pos, change)) {
                    restoreQuietly(level, pos, change.before());
                    if (consumed) giveOrDrop(player, change.placedItem());
                    continue;
                }
                if (!player.isCreative()) giveAll(player, change.displacedDrops());
                restored.add(pos);
            } catch (RuntimeException exception) {
                restoreQuietly(level, pos, change.before());
                if (consumed) giveOrDrop(player, change.placedItem());
                CosmicCore.LOGGER.warn("Effortless Building redo failed at {}", pos, exception);
            }
        }
        if (metadata.pipeOperation() != null) {
            EffortlessBuildingGTPipeCompat.afterRedo(level, metadata.pipeOperation(), restored);
        }
        return new Result(restored.size(), restored);
    }

    public static boolean consumeExact(ServerPlayer player, ItemStack required) {
        return consumeExact(player, required, 1);
    }

    private static boolean consumeExact(ServerPlayer player, ItemStack required, int amount) {
        if (amount <= 0) return true;
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, required)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
                if (remaining == 0) return true;
            }
        }
        return remaining == 0;
    }

    public static int countExact(Player player, ItemStack required) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, required)) {
                count = EffortlessBuildingAE2Bridge.saturatingAdd(count, stack.getCount());
            }
        }
        return count;
    }

    public static void giveOrDrop(ServerPlayer player, ItemStack supplied) {
        ItemStack stack = supplied.copy();
        player.getInventory().add(stack);
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
        drop.setNoPickUpDelay();
        player.level().addFreshEntity(drop);
    }

    private static boolean canConsumeAll(ServerPlayer player, List<ItemStack> required) {
        List<ItemStack> available = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            available.add(player.getInventory().getItem(slot).copy());
        }
        for (ItemStack requirement : required) {
            int remaining = requirement.getCount();
            for (ItemStack stack : available) {
                if (!ItemStack.isSameItemSameComponents(stack, requirement)) continue;
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
                if (remaining == 0) break;
            }
            if (remaining != 0) return false;
        }
        return true;
    }

    private static void consumeAll(ServerPlayer player, List<ItemStack> required) {
        for (ItemStack stack : required) consumeExact(player, stack, stack.getCount());
    }

    private static void giveAll(ServerPlayer player, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) giveOrDrop(player, stack);
    }

    private static void restoreQuietly(
                                       ServerLevel level, BlockPos pos,
                                       EffortlessBuildingBlockSnapshot snapshot) {
        try {
            snapshot.restore(level, pos);
        } catch (RuntimeException exception) {
            CosmicCore.LOGGER.error("Effortless Building operation rollback failed at {}", pos, exception);
        }
    }

    private static boolean matchesAfter(
                                        ServerLevel level, BlockPos pos,
                                        EffortlessBuildingBlockChange change) {
        if (EffortlessBuildingAE2CableCompat.isCableItem(change.placedItem())) {
            return EffortlessBuildingAE2CableCompat.matchesUnmodifiedCable(level, pos, change.placedItem());
        }
        return change.after().matches(level, pos);
    }

    private static boolean restoreAfter(
                                        ServerPlayer player, ServerLevel level, BlockPos pos,
                                        EffortlessBuildingBlockChange change) {
        if (EffortlessBuildingAE2CableCompat.isCableItem(change.placedItem())) {
            return EffortlessBuildingAE2CableCompat.place(
                    player, level, pos, Direction.UP, change.placedItem());
        }
        return change.after().restore(level, pos);
    }

    public record Result(int changed, Set<BlockPos> positions) {

        public Result {
            positions = Set.copyOf(positions);
        }
    }
}
