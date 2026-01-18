package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarIrisUpgrade;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;

/**
 * Packet for managing stellar IRIS upgrades.
 * Sent from client to server to unlock or level up upgrades.
 * Upgrades are permanent and cannot be refunded.
 */
public class StellarUpgradePacket implements CCoreNetwork.INetPacket {

    private final BlockPos machinePos;
    @Nullable
    private final StellarIrisUpgrade upgrade;
    @SuppressWarnings("unused")
    private final boolean isRespec; // Kept for backwards compatibility but ignored

    public StellarUpgradePacket(BlockPos machinePos, @Nullable StellarIrisUpgrade upgrade, boolean isRespec) {
        this.machinePos = machinePos;
        this.upgrade = upgrade;
        this.isRespec = isRespec;
    }

    public StellarUpgradePacket(FriendlyByteBuf buffer) {
        this.machinePos = buffer.readBlockPos();
        this.isRespec = buffer.readBoolean(); // Read but ignore
        if (buffer.readBoolean()) {
            this.upgrade = buffer.readEnum(StellarIrisUpgrade.class);
        } else {
            this.upgrade = null;
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(machinePos);
        buffer.writeBoolean(false); // Respec no longer supported
        buffer.writeBoolean(upgrade != null);
        if (upgrade != null) {
            buffer.writeEnum(upgrade);
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return;

        // Get the machine using GTCEu's MetaMachine pattern
        if (!(MetaMachine.getMachine(player.level(), machinePos) instanceof IrisMultiblockMachine machine)) {
            return;
        }

        // Verify player is close enough
        if (player.distanceToSqr(machinePos.getX() + 0.5, machinePos.getY() + 0.5, machinePos.getZ() + 0.5) > 64) {
            return;
        }

        // Respec is no longer supported - upgrades are permanent
        if (upgrade != null) {
            machine.tryUnlockUpgrade(upgrade);
        }
    }
}
