package com.ghostipedia.cosmiccore.common.orrery;

import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.deployment.LeylinePrefab;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.GridServices;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.stacks.AEItemKey;
import appeng.me.helpers.PlayerSource;
import appeng.util.Platform;

import java.util.UUID;

public final class OrreryNetwork {

    private OrreryNetwork() {}

    public enum Status {
        CONNECTED,
        UNBOUND,
        NETWORK_UNAVAILABLE,
        OUT_OF_RANGE
    }

    public record Connection(Status status, IGrid grid, IGridNode node) {}

    public static void register() {
        GridServices.register(OrreryCatalogue.class, OrreryCatalogue.class);
        OrreryCraftingMenuHost.register();
        GridLinkables.register(CosmicItems.LEYLINE_ORRERY, new IGridLinkableHandler() {

            @Override
            public boolean canLink(ItemStack stack) {
                return stack.is(CosmicItems.LEYLINE_ORRERY.get());
            }

            @Override
            public void link(ItemStack stack, GlobalPos pos) {
                stack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
            }

            @Override
            public void unlink(ItemStack stack) {
                stack.remove(AEComponents.WIRELESS_LINK_TARGET);
            }
        });
    }

    public static IGrid linkedGrid(MinecraftServer server, GlobalPos target) {
        if (target == null) return null;
        var level = server.getLevel(target.dimension());
        if (level == null) return null;
        var blockEntity = Platform.getTickingBlockEntity(level, target.pos());
        return blockEntity instanceof IWirelessAccessPoint accessPoint ? accessPoint.getGrid() : null;
    }

    public static Connection connect(ServerPlayer player, ItemStack stack) {
        var target = stack.get(AEComponents.WIRELESS_LINK_TARGET);
        if (target == null) return new Connection(Status.UNBOUND, null, null);
        var grid = linkedGrid(player.server, target);
        if (grid == null || !grid.getEnergyService().isNetworkPowered())
            return new Connection(Status.NETWORK_UNAVAILABLE, null, null);
        for (var accessPoint : grid.getService(OrreryCatalogue.class).accessPoints()) {
            if (!accessPoint.isActive()) continue;
            var location = accessPoint.getLocation();
            if (location.getLevel() == player.level() &&
                    location.getPos().distToLowCornerSqr(player.getX(), player.getY(), player.getZ()) <
                            accessPoint.getRange() * accessPoint.getRange())
                return new Connection(Status.CONNECTED, grid, accessPoint.getActionableNode());
        }
        return new Connection(Status.OUT_OF_RANGE, null, null);
    }

    public static ItemStack extract(ServerPlayer player, IGrid grid, UUID design) {
        var inventory = grid.getStorageService().getInventory();
        var source = new PlayerSource(player);
        for (var entry : grid.getStorageService().getCachedInventory()) {
            if (entry.getLongValue() <= 0 || !(entry.getKey() instanceof AEItemKey key) ||
                    key.getItem() != CosmicItems.LEYLINE_PACKAGE.get())
                continue;
            var stack = key.toStack();
            if (design.equals(LeylinePrefab.reference(stack)) &&
                    inventory.extract(key, 1, Actionable.MODULATE, source) == 1)
                return stack.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack refund(ServerPlayer player, GlobalPos target, ItemStack stack) {
        var grid = linkedGrid(player.server, target);
        if (grid == null || !grid.getEnergyService().isNetworkPowered()) return stack;
        long inserted = grid.getStorageService().getInventory().insert(AEItemKey.of(stack), stack.getCount(),
                Actionable.MODULATE, new PlayerSource(player));
        return stack.copyWithCount(stack.getCount() - (int) inserted);
    }
}
