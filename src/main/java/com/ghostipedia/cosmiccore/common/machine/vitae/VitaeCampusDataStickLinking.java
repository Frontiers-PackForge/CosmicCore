package com.ghostipedia.cosmiccore.common.machine.vitae;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.HemophagicTransfuserMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ImbumentPylonMachine;
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

public final class VitaeCampusDataStickLinking {

    private static final String ROOT_KEY = "cosmiccore:vitae_campus_link";

    private VitaeCampusDataStickLinking() {}

    public static InteractionResult copy(Player player, ItemStack dataStick, MetaMachine machine,
                                         GlobalPos position, UUID owner) {
        if (machine.isRemote()) return InteractionResult.SUCCESS;
        if (position == null || owner == null || !isFormed(machine)) {
            return message(player, "cosmiccore.vitae_campus.link.not_ready", InteractionResult.FAIL);
        }
        CompoundTag link = new CompoundTag();
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, position).result()
                .ifPresent(encoded -> link.put("position", encoded));
        link.putUUID("owner", owner);
        ItemData.mutateTag(dataStick, root -> root.put(ROOT_KEY, link));
        dataStick.set(DataComponents.CUSTOM_NAME, Component.translatable(
                "cosmiccore.vitae_campus.datastick", machine.getDefinition().getName()));
        player.sendSystemMessage(Component.translatable("cosmiccore.vitae_campus.link.copied")
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult link(Player player, ItemStack dataStick, MetaMachine target) {
        if (target.isRemote()) return InteractionResult.SUCCESS;
        CompoundTag root = ItemData.readTag(dataStick);
        if (root == null || !root.contains(ROOT_KEY)) return InteractionResult.PASS;
        CompoundTag link = root.getCompound(ROOT_KEY);
        GlobalPos sourcePosition = link.contains("position") ?
                GlobalPos.CODEC.decode(NbtOps.INSTANCE, link.get("position")).result()
                        .map(result -> result.getFirst()).orElse(null) :
                null;
        if (sourcePosition == null || !link.hasUUID("owner") || !(target.getLevel() instanceof ServerLevel level)) {
            return message(player, "cosmiccore.vitae_campus.link.invalid", InteractionResult.FAIL);
        }
        GlobalPos targetPosition = GlobalPos.of(level.dimension(), target.getBlockPos());
        if (!sourcePosition.dimension().equals(targetPosition.dimension())) {
            return message(player, "cosmiccore.vitae_campus.link.dimension", InteractionResult.FAIL);
        }
        if (sourcePosition.pos().distSqr(targetPosition.pos()) >
                VitaeCampusSavedData.LINK_RANGE * VitaeCampusSavedData.LINK_RANGE) {
            return message(player, "cosmiccore.vitae_campus.link.range", InteractionResult.FAIL);
        }
        if (!level.isLoaded(sourcePosition.pos())) {
            return message(player, "cosmiccore.vitae_campus.link.unloaded", InteractionResult.FAIL);
        }
        MetaMachine source = MetaMachine.getMachine(level, sourcePosition.pos());
        HemophagicTransfuserMachine core;
        ImbumentPylonMachine pylon;
        if (source instanceof HemophagicTransfuserMachine sourceCore &&
                target instanceof ImbumentPylonMachine targetPylon) {
            core = sourceCore;
            pylon = targetPylon;
        } else if (source instanceof ImbumentPylonMachine sourcePylon &&
                target instanceof HemophagicTransfuserMachine targetCore) {
                    core = targetCore;
                    pylon = sourcePylon;
                } else {
                    return message(player, "cosmiccore.vitae_campus.link.incompatible", InteractionResult.FAIL);
                }
        if (!core.isFormed() || !pylon.isFormed() ||
                !Objects.equals(core.resolveCampusOwner(), pylon.resolveCampusOwner()) ||
                !Objects.equals(core.resolveCampusOwner(), link.getUUID("owner"))) {
            return message(player, "cosmiccore.vitae_campus.link.owner", InteractionResult.FAIL);
        }
        VitaeCampusSavedData.LinkResult result = VitaeCampusSavedData.get(level.getServer()).link(
                core.resolveCampusOwner(), core.globalPosition(), pylon.globalPosition());
        if (result != VitaeCampusSavedData.LinkResult.LINKED &&
                result != VitaeCampusSavedData.LinkResult.UPDATED &&
                result != VitaeCampusSavedData.LinkResult.RELINKED) {
            String key = switch (result) {
                case OUT_OF_RANGE -> "cosmiccore.vitae_campus.link.range";
                case DIFFERENT_DIMENSION -> "cosmiccore.vitae_campus.link.dimension";
                case OWNER_MISMATCH -> "cosmiccore.vitae_campus.link.owner";
                default -> "cosmiccore.vitae_campus.link.invalid";
            };
            return message(player, key, InteractionResult.FAIL);
        }
        if (result == VitaeCampusSavedData.LinkResult.RELINKED) pylon.onCampusRelinked();
        String messageKey = result == VitaeCampusSavedData.LinkResult.RELINKED ?
                "cosmiccore.vitae_campus.link.relinked" :
                "cosmiccore.vitae_campus.link.established";
        player.sendSystemMessage(Component.translatable(messageKey)
                .withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    private static boolean isFormed(MetaMachine machine) {
        return machine instanceof HemophagicTransfuserMachine core ? core.isFormed() :
                machine instanceof ImbumentPylonMachine pylon && pylon.isFormed();
    }

    private static InteractionResult message(Player player, String key, InteractionResult result) {
        player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
        return result;
    }
}
