package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

public record SetMultiblockStructureTierPacket(BlockPos machinePos, int tier) implements CustomPacketPayload {

    public static final Type<SetMultiblockStructureTierPacket> TYPE = new Type<>(
            CosmicCore.id("set_multiblock_structure_tier"));
    public static final StreamCodec<FriendlyByteBuf, SetMultiblockStructureTierPacket> CODEC = StreamCodec.ofMember(
            SetMultiblockStructureTierPacket::encode, SetMultiblockStructureTierPacket::new);

    public SetMultiblockStructureTierPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(machinePos);
        buffer.writeVarInt(tier);
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            boolean hasTerminal = player.getMainHandItem().is(GTItems.TERMINAL.get()) ||
                    player.getOffhandItem().is(GTItems.TERMINAL.get());
            if (!hasTerminal || player.distanceToSqr(machinePos.getCenter()) > 64.0) return;
            if (!(MetaMachine.getMachine(player.level(),
                    machinePos) instanceof MultiblockControllerMachine controller) ||
                    !(controller instanceof ITieredMultiblockMachine tiered) ||
                    !TieredMultiblockPatterns.isTiered(controller.getDefinition()) ||
                    !MachineOwner.canOpenOwnerMachine(player, controller)) {
                return;
            }
            tiered.setStructureTier(TieredMultiblockPatterns.clampTier(controller.getDefinition(), tier));
        });
    }

    @Override
    public @NotNull Type<SetMultiblockStructureTierPacket> type() {
        return TYPE;
    }
}
