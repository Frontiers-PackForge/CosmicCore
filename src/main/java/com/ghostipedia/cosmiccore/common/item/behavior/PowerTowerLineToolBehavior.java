package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.item.PowerTowerCoilItem;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMachine;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;
import com.ghostipedia.cosmiccore.common.transmission.link.PowerTowerLinkResult;
import com.ghostipedia.cosmiccore.common.transmission.link.PowerTowerLinkService;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class PowerTowerLineToolBehavior implements IInteractionItem, IAddInformation {

    private static final String TAG_KEY = "PowerTowerLine";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_NODE = "Node";

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !(MetaMachine.getMachine(context.getLevel(),
                context.getClickedPos()) instanceof PowerTowerMachine tower) || !tower.isFormed())
            return InteractionResult.PASS;
        if (!MachineOwner.canBreakOwnerMachine(player, tower)) return InteractionResult.FAIL;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        UUID nodeId = tower.getGraphNodeId();
        if (nodeId == null) return InteractionResult.FAIL;
        ItemStack tool = context.getItemInHand();
        PendingTowerSelection selection = readPendingSelection(tool);
        if (selection == null) {
            writePendingSelection(tool, context.getLevel(), nodeId);
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.first",
                    context.getClickedPos().toShortString()).withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.SUCCESS;
        }
        if (!selection.dimension().equals(context.getLevel().dimension().location())) {
            clearPendingSelection(tool);
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.dimension")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        ItemStack coilStack = player.getItemInHand(context.getHand() == InteractionHand.MAIN_HAND ?
                InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(coilStack.getItem() instanceof PowerTowerCoilItem coil)) {
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.coil")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        PowerTowerSavedData data = PowerTowerSavedData
                .getOrCreate((net.minecraft.server.level.ServerLevel) context.getLevel());
        PowerTowerNode firstNode = data.graph().node(selection.nodeId());
        PowerTowerNode secondNode = data.graph().node(nodeId);
        PowerTowerMachine firstTower = firstNode == null ? null :
                loadedTower(context.getLevel(), firstNode.controllerPos());
        if (firstTower == null || secondNode == null || !MachineOwner.canBreakOwnerMachine(player, firstTower)) {
            clearPendingSelection(tool);
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.endpoint")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        double distance = firstNode.wireAttachmentCenter().distanceTo(secondNode.wireAttachmentCenter());
        double sag = Math.max(1.0, Math.min(4.0, distance * 0.06));
        PowerTowerLinkResult result = new PowerTowerLinkService(data.graph(), data).tryCreateSpan(context.getLevel(),
                selection.nodeId(), nodeId, coil.getVoltageTier(), 0.65, sag,
                pos -> firstTower.containsFormedStructurePosition(pos) ||
                        tower.containsFormedStructurePosition(pos));
        if (result.spanCreated()) {
            if (!player.getAbilities().instabuild) coilStack.shrink(1);
            clearPendingSelection(tool);
            data.loadedTerminals().wakeComponentTerminals(data.graph(), nodeId);
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.created")
                    .withStyle(ChatFormatting.GREEN), true);
            return InteractionResult.SUCCESS;
        }
        player.displayClientMessage(Component.translatable(errorTranslationKey(result.status()))
                .withStyle(ChatFormatting.RED), true);
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack stack, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (readPendingSelection(stack) == null) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            clearPendingSelection(stack);
            player.displayClientMessage(Component.translatable("cosmiccore.power_tower.line.cleared")
                    .withStyle(ChatFormatting.YELLOW), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag flag) {
        lines.add(Component.translatable("cosmiccore.power_tower.line.tooltip").withStyle(ChatFormatting.GRAY));
        PendingTowerSelection selection = readPendingSelection(stack);
        if (selection != null) {
            lines.add(Component.translatable("cosmiccore.power_tower.line.tooltip.selected",
                    selection.dimension().toString()).withStyle(ChatFormatting.AQUA));
        }
    }

    private static @Nullable PowerTowerMachine loadedTower(Level level, BlockPos pos) {
        return level.isLoaded(pos) && MetaMachine.getMachine(level, pos) instanceof PowerTowerMachine tower &&
                tower.isFormed() ? tower : null;
    }

    private static void writePendingSelection(ItemStack stack, Level level, UUID nodeId) {
        ItemData.mutateTag(stack, root -> {
            CompoundTag selection = new CompoundTag();
            selection.putString(TAG_DIMENSION, level.dimension().location().toString());
            selection.putUUID(TAG_NODE, nodeId);
            root.put(TAG_KEY, selection);
        });
    }

    private static @Nullable PendingTowerSelection readPendingSelection(ItemStack stack) {
        CompoundTag root = ItemData.readTag(stack);
        if (!root.contains(TAG_KEY)) return null;
        CompoundTag selection = root.getCompound(TAG_KEY);
        ResourceLocation dimension = ResourceLocation.tryParse(selection.getString(TAG_DIMENSION));
        return dimension == null || !selection.hasUUID(TAG_NODE) ? null :
                new PendingTowerSelection(dimension, selection.getUUID(TAG_NODE));
    }

    private static void clearPendingSelection(ItemStack stack) {
        ItemData.mutateTag(stack, root -> root.remove(TAG_KEY));
    }

    private static String errorTranslationKey(PowerTowerLinkResult.Status status) {
        return "cosmiccore.power_tower.line.error." + status.name().toLowerCase(java.util.Locale.ROOT);
    }

    private record PendingTowerSelection(ResourceLocation dimension, UUID nodeId) {}
}
