package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.EffortlessBuildingAE2CountQueryPacket;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.me.helpers.PlayerSource;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;

public final class EffortlessBuildingAE2Bridge {

    private static final long QUERY_INTERVAL_NANOS = 250_000_000L;
    private static final Map<Item, Integer> CLIENT_COUNTS = new HashMap<>();
    private static final Map<Item, Long> CLIENT_QUERY_TIMES = new HashMap<>();

    private EffortlessBuildingAE2Bridge() {}

    public static int count(Player player, Item item) {
        IGrid grid = findGrid(player);
        if (grid == null) return 0;
        MEStorage storage = grid.getStorageService().getInventory();
        long count = storage.extract(AEItemKey.of(item), Long.MAX_VALUE, Actionable.SIMULATE, source(player));
        return (int) Math.min(count, Integer.MAX_VALUE);
    }

    public static int extract(Player player, Item item, int amount) {
        if (amount <= 0) return 0;
        IGrid grid = findGrid(player);
        if (grid == null) return 0;
        MEStorage storage = grid.getStorageService().getInventory();
        long extracted = storage.extract(AEItemKey.of(item), amount, Actionable.MODULATE, source(player));
        return (int) Math.min(extracted, Integer.MAX_VALUE);
    }

    public static int insert(Player player, Item item, int amount) {
        if (amount <= 0) return 0;
        IGrid grid = findGrid(player);
        if (grid == null) return 0;
        MEStorage storage = grid.getStorageService().getInventory();
        long inserted = storage.insert(AEItemKey.of(item), amount, Actionable.MODULATE, source(player));
        return (int) Math.min(inserted, Integer.MAX_VALUE);
    }

    public static int restockMainHand(Player player, Item item) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty() && !held.is(item)) return 0;
        int current = held.isEmpty() ? 0 : held.getCount();
        int maximum = item.getDefaultMaxStackSize();
        int extracted = extract(player, item, maximum - current);
        if (extracted <= 0) return 0;
        if (held.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item, extracted));
        } else {
            held.grow(extracted);
        }
        return extracted;
    }

    public static int cachedClientCount(Item item) {
        return CLIENT_COUNTS.getOrDefault(item, 0);
    }

    public static void setCachedClientCount(Item item, int count) {
        CLIENT_COUNTS.put(item, Math.max(0, count));
    }

    public static void requestClientCount(Item item) {
        long now = System.nanoTime();
        Long previous = CLIENT_QUERY_TIMES.get(item);
        if (!isIntervalElapsed(previous, now, QUERY_INTERVAL_NANOS)) return;
        CLIENT_QUERY_TIMES.put(item, now);
        CCoreNetwork.sendToServer(new EffortlessBuildingAE2CountQueryPacket(item));
    }

    public static void clearClientCache() {
        CLIENT_COUNTS.clear();
        CLIENT_QUERY_TIMES.clear();
    }

    public static int saturatingAdd(int first, int second) {
        return (int) Math.min((long) first + second, Integer.MAX_VALUE);
    }

    public static boolean isIntervalElapsed(@Nullable Long previous, long now, long interval) {
        return previous == null || now - previous >= interval;
    }

    @Nullable
    private static IGrid findGrid(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            IGrid grid = findGrid(player, stack);
            if (grid != null) return grid;
        }
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (var result : handler.findCurios(
                            stack -> stack.getItem() instanceof WirelessTerminalItem)) {
                        IGrid grid = findGrid(player, result.stack());
                        if (grid != null) return grid;
                    }
                    return (IGrid) null;
                })
                .orElse(null);
    }

    @Nullable
    private static IGrid findGrid(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof WirelessTerminalItem terminal)) return null;
        return terminal.getLinkedGrid(stack, player.level(), null);
    }

    private static IActionSource source(Player player) {
        return new PlayerSource(player);
    }
}
