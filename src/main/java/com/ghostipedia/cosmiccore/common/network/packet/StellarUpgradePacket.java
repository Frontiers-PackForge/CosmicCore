package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarIrisUpgrade;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class StellarUpgradePacket implements CustomPacketPayload {

    public static final Type<StellarUpgradePacket> TYPE = new Type<>(CosmicCore.id("stellar_upgrade"));
    public static final StreamCodec<FriendlyByteBuf, StellarUpgradePacket> CODEC = StreamCodec
            .ofMember(StellarUpgradePacket::encode, StellarUpgradePacket::new);

    private final BlockPos machinePos;
    @Nullable
    private final StellarIrisUpgrade upgrade;
    @SuppressWarnings("unused")
    private final boolean isRespec;

    public StellarUpgradePacket(BlockPos machinePos, @Nullable StellarIrisUpgrade upgrade, boolean isRespec) {
        this.machinePos = machinePos;
        this.upgrade = upgrade;
        this.isRespec = isRespec;
    }

    public StellarUpgradePacket(FriendlyByteBuf buffer) {
        this.machinePos = buffer.readBlockPos();
        this.isRespec = buffer.readBoolean();
        if (buffer.readBoolean()) {
            this.upgrade = buffer.readEnum(StellarIrisUpgrade.class);
        } else {
            this.upgrade = null;
        }
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(machinePos);
        buffer.writeBoolean(false);
        buffer.writeBoolean(upgrade != null);
        if (upgrade != null) {
            buffer.writeEnum(upgrade);
        }
    }

    public void execute(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        if (!(MetaMachine.getMachine(player.level(), machinePos) instanceof IrisMultiblockMachine machine)) {
            return;
        }

        if (player.distanceToSqr(machinePos.getX() + 0.5, machinePos.getY() + 0.5, machinePos.getZ() + 0.5) > 64) {
            return;
        }

        if (upgrade != null) {
            machine.tryUnlockUpgrade(upgrade);
        }
    }

    @Override
    public @NotNull Type<StellarUpgradePacket> type() {
        return TYPE;
    }
}
