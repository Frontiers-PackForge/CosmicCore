package com.ghostipedia.cosmiccore.common.compat.effortlessbuilding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import appeng.api.implementations.parts.ICablePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.parts.PartPlacement;

public final class EffortlessBuildingAE2CableCompat {

    private EffortlessBuildingAE2CableCompat() {}

    public static boolean isCableItem(ItemStack stack) {
        return isCableItem(stack.getItem());
    }

    public static boolean isCableItem(Item item) {
        return item instanceof IPartItem<?> partItem &&
                ICablePart.class.isAssignableFrom(partItem.getPartClass());
    }

    public static boolean place(
                                ServerPlayer player, ServerLevel level, BlockPos pos, Direction face,
                                ItemStack stack) {
        if (!(stack.getItem() instanceof IPartItem<?> partItem) ||
                !ICablePart.class.isAssignableFrom(partItem.getPartClass()) ||
                PartHelper.getPartHost(level, pos) != null)
            return false;
        if (!level.getBlockState(pos).canBeReplaced() &&
                !level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3))
            return false;
        return PartPlacement.placePart(player, level, partItem, stack.getComponents(), pos, face) != null;
    }

    public static boolean matchesUnmodifiedCable(ServerLevel level, BlockPos pos, ItemStack stack) {
        IPartHost host = PartHelper.getPartHost(level, pos);
        if (host == null) return false;
        IPart center = host.getPart(null);
        if (!(center instanceof ICablePart) || center.getPartItem() != stack.getItem()) return false;
        for (Direction direction : Direction.values()) {
            if (host.getPart(direction) != null || host.getFacadeContainer().getFacade(direction) != null) return false;
        }
        return true;
    }
}
