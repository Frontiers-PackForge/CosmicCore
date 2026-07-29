package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockPatterns;
import com.ghostipedia.cosmiccore.common.machine.multiblock.tier.TieredMultiblockTerminal;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record BuildTieredMultiblockPacket(BlockPos machinePos, int tier,
                                          Map<BlockPos, BlockState> blockPreferences)
        implements CustomPacketPayload {

    private static final int MAX_BLOCK_PREFERENCES = 4096;

    public static final Type<BuildTieredMultiblockPacket> TYPE = new Type<>(
            CosmicCore.id("build_tiered_multiblock"));
    public static final StreamCodec<FriendlyByteBuf, BuildTieredMultiblockPacket> CODEC = StreamCodec.ofMember(
            BuildTieredMultiblockPacket::encode, BuildTieredMultiblockPacket::new);

    public BuildTieredMultiblockPacket {
        blockPreferences = Map.copyOf(blockPreferences);
        if (blockPreferences.size() > MAX_BLOCK_PREFERENCES) {
            throw new IllegalArgumentException("Too many multiblock block preferences");
        }
    }

    public BuildTieredMultiblockPacket(FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readVarInt(), decodeBlockPreferences(buffer));
    }

    private static Map<BlockPos, BlockState> decodeBlockPreferences(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_BLOCK_PREFERENCES) {
            throw new IllegalArgumentException("Invalid multiblock block preference count: " + size);
        }
        Map<BlockPos, BlockState> preferences = new HashMap<>(size);
        for (int index = 0; index < size; index++) {
            BlockPos pos = buffer.readBlockPos();
            BlockState state = Block.BLOCK_STATE_REGISTRY.byId(buffer.readVarInt());
            if (state == null || preferences.put(pos, state) != null) {
                throw new IllegalArgumentException("Invalid multiblock block preference");
            }
        }
        return preferences;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(machinePos);
        buffer.writeVarInt(tier);
        buffer.writeVarInt(blockPreferences.size());
        blockPreferences.forEach((pos, state) -> {
            buffer.writeBlockPos(pos);
            buffer.writeVarInt(Block.getId(state));
        });
    }

    public void execute(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.isCreative()) return;
            boolean hasTerminal = player.getMainHandItem().is(GTItems.TERMINAL.get()) ||
                    player.getOffhandItem().is(GTItems.TERMINAL.get());
            if (!hasTerminal || player.distanceToSqr(machinePos.getCenter()) > 64.0 ||
                    !player.level().isLoaded(machinePos)) {
                return;
            }
            if (!(MetaMachine.getMachine(player.level(),
                    machinePos) instanceof MultiblockControllerMachine controller) ||
                    !(controller instanceof ITieredMultiblockMachine tiered) || controller.isFormed() ||
                    controller instanceof IRecipeLogicMachine recipeMachine &&
                            recipeMachine.getRecipeLogic().isActive() ||
                    !TieredMultiblockPatterns.isTiered(controller.getDefinition()) ||
                    tier != TieredMultiblockPatterns.clampTier(controller.getDefinition(), tier) ||
                    !MachineOwner.canOpenOwnerMachine(player, controller)) {
                return;
            }
            tiered.setStructureTier(tier);
            if (tiered.getStructureTier() != tier) return;
            TieredMultiblockTerminal.build(player.level(), controller, tiered, blockPreferences);
        });
    }

    @Override
    public @NotNull Type<BuildTieredMultiblockPacket> type() {
        return TYPE;
    }
}
