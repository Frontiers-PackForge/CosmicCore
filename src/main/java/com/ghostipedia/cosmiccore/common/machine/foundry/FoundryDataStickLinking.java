package com.ghostipedia.cosmiccore.common.machine.foundry;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.AlloyBlastingKilnMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HephaestusCauldronMachine;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.UUID;

public final class FoundryDataStickLinking {

    private static final String ROOT_KEY = "cosmiccore:foundry_link";
    private static final String POSITION_KEY = "position";
    private static final String OWNER_KEY = "owner";

    private FoundryDataStickLinking() {}

    public static InteractionResult copy(Player player, ItemStack dataStick, MetaMachine machine,
                                         GlobalPos position, UUID owner) {
        if (machine.isRemote()) return InteractionResult.SUCCESS;
        if (position == null || owner == null || !isFormed(machine)) {
            return message(player, "cosmiccore.foundry.link.not_ready", ChatFormatting.RED, InteractionResult.FAIL);
        }
        CompoundTag link = new CompoundTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, position).result()
                .ifPresent(encoded -> link.put(POSITION_KEY, encoded));
        link.putUUID(OWNER_KEY, owner);
        ItemData.mutateTag(dataStick, root -> root.put(ROOT_KEY, link));
        String name = machine.getDefinition().getName();
        dataStick.set(DataComponents.CUSTOM_NAME,
                Component.translatable("cosmiccore.foundry.datastick", name));
        player.sendSystemMessage(Component.translatable("cosmiccore.foundry.link.copied", name)
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult link(Player player, ItemStack dataStick, MetaMachine target) {
        if (target.isRemote()) return InteractionResult.SUCCESS;
        CompoundTag root = ItemData.readTag(dataStick);
        if (root == null || !root.contains(ROOT_KEY)) return InteractionResult.PASS;
        CompoundTag link = root.getCompound(ROOT_KEY);
        if (!link.contains(POSITION_KEY) || !link.hasUUID(OWNER_KEY)) {
            return message(player, "cosmiccore.foundry.link.invalid", ChatFormatting.RED, InteractionResult.FAIL);
        }
        GlobalPos sourcePosition = GlobalPos.CODEC.decode(NbtOps.INSTANCE, link.get(POSITION_KEY)).result()
                .map(result -> result.getFirst()).orElse(null);
        if (sourcePosition == null) {
            return message(player, "cosmiccore.foundry.link.invalid", ChatFormatting.RED, InteractionResult.FAIL);
        }
        if (!(target.getLevel() instanceof ServerLevel level)) return InteractionResult.FAIL;
        GlobalPos targetPosition = GlobalPos.of(level.dimension(), target.getBlockPos());
        if (!sourcePosition.dimension().equals(targetPosition.dimension())) {
            return message(player, "cosmiccore.foundry.link.dimension", ChatFormatting.RED, InteractionResult.FAIL);
        }
        if (sourcePosition.pos().distSqr(targetPosition.pos()) >
                FoundryPyrofluxPolicy.LINK_RANGE * FoundryPyrofluxPolicy.LINK_RANGE) {
            return message(player, "cosmiccore.foundry.link.range", ChatFormatting.RED, InteractionResult.FAIL);
        }
        if (!level.isLoaded(sourcePosition.pos())) {
            return message(player, "cosmiccore.foundry.link.unloaded", ChatFormatting.RED, InteractionResult.FAIL);
        }
        MetaMachine source = MetaMachine.getMachine(level, sourcePosition.pos());
        HephaestusCauldronMachine core;
        AlloyBlastingKilnMachine kiln;
        if (source instanceof HephaestusCauldronMachine sourceCore &&
                target instanceof AlloyBlastingKilnMachine targetKiln) {
            core = sourceCore;
            kiln = targetKiln;
        } else if (source instanceof AlloyBlastingKilnMachine sourceKiln &&
                target instanceof HephaestusCauldronMachine targetCore) {
                    core = targetCore;
                    kiln = sourceKiln;
                } else {
                    return message(player, "cosmiccore.foundry.link.incompatible", ChatFormatting.RED,
                            InteractionResult.FAIL);
                }
        if (!core.isFormed() || !kiln.isFormed()) {
            return message(player, "cosmiccore.foundry.link.not_ready", ChatFormatting.RED, InteractionResult.FAIL);
        }
        UUID coreOwner = core.resolveCampusOwner();
        UUID kilnOwner = kiln.foundryOwner();
        if (coreOwner == null || !Objects.equals(coreOwner, kilnOwner)) {
            return message(player, "cosmiccore.foundry.link.owner", ChatFormatting.RED, InteractionResult.FAIL);
        }
        FoundryCampusSavedData data = FoundryCampusSavedData.get(level.getServer());
        FoundryCampusSavedData.LinkResult result = data.link(
                coreOwner,
                core.globalPosition(),
                kiln.globalPosition(),
                kiln.foundryFurnaceType(),
                kiln.foundryReceivingAnchor());
        if (result != FoundryCampusSavedData.LinkResult.LINKED &&
                result != FoundryCampusSavedData.LinkResult.UPDATED) {
            String key = switch (result) {
                case CAPACITY_REACHED -> "cosmiccore.foundry.link.capacity";
                case ALREADY_LINKED -> "cosmiccore.foundry.link.already";
                case OUT_OF_RANGE -> "cosmiccore.foundry.link.range";
                case DIFFERENT_DIMENSION -> "cosmiccore.foundry.link.dimension";
                case OWNER_MISMATCH -> "cosmiccore.foundry.link.owner";
                default -> "cosmiccore.foundry.link.invalid";
            };
            return message(player, key, ChatFormatting.RED, InteractionResult.FAIL);
        }
        FoundryCampusSync.send(level, core.globalPosition());
        player.sendSystemMessage(Component.translatable("cosmiccore.foundry.link.established")
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    private static boolean isFormed(MetaMachine machine) {
        return machine instanceof HephaestusCauldronMachine core ? core.isFormed() :
                machine instanceof AlloyBlastingKilnMachine kiln && kiln.isFormed();
    }

    private static InteractionResult message(Player player, String key, ChatFormatting color,
                                             InteractionResult result) {
        player.sendSystemMessage(Component.translatable(key).withStyle(color));
        return result;
    }
}
