package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import java.util.Optional;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.teleporter.LandingZoneHelper;
import com.ghostipedia.cosmiccore.common.teleporter.SafeTeleporter;
import com.ghostipedia.cosmiccore.common.teleporter.TeleportPadRegistry;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Diving Bell Machine Controller
 *
 * Detects players standing directly on top of the controller block and teleports them to the Deep Below dimension.
 * - Energy cost: 500,000 EU per teleport
 * - Cooldown: 100 ticks (5 seconds)
 * - Only teleports one player per activation
 */
public class DivingBellMachine extends WorkableElectricMultiblockMachine {


    // Configuration values
    private static final int TELEPORT_COST_EU = 500000; // 500k EU per teleport
    private static final int COOLDOWN_TICKS = 100; // 5 seconds - idk just in case players try to abuse it.

    // Teleport destination settings
    private static final String TARGET_DIMENSION = "frontiers:the_deep_below"; // Dimension to teleport to
    private static final int DESTINATION_SEARCH_START_Y = 100; // Y level to start searching for safe ground

    // State
    @Persisted
    private int cooldownRemaining = 0;

    protected TickableSubscription tickSubscription;

    public DivingBellMachine(BlockEntityCreationInfo holder, Object... args) {
        super(holder, args);
    }

    @Override
    @NotNull

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // Subscribe to server ticks when structure forms
        tickSubscription = subscribeServerTick(tickSubscription, this::checkForPlayers);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        // Unsubscribe when structure breaks
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    // Tick handler that checks for players and teleports them.
    private void checkForPlayers() {
        // Decrement cooldown
        if (cooldownRemaining > 0) {
            cooldownRemaining--;
            return;
        }

        // Check if we have enough energy
        if (!hasEnoughEnergy()) {
            return;
        }

        // Detect players on top platform
        List<ServerPlayer> players = getPlayersOnPlatform();
        if (players.isEmpty()) {
            return;
        }

        // Teleport first player only (one at a time)
        ServerPlayer target = players.get(0);
        if (teleportPlayerToDeepBelow(target)) {
            // Consume energy and start cooldown
            consumeEnergy(TELEPORT_COST_EU);
            cooldownRemaining = COOLDOWN_TICKS;
        }
    }

    // Check if there is enough energy for a teleport.
    private boolean hasEnoughEnergy() {
        long available = energyContainer.getEnergyStored();
        return available >= TELEPORT_COST_EU;
    }

    // Consume energy for teleportation.
    private void consumeEnergy(long amount) {
        energyContainer.removeEnergy(amount);
    }

    // Get all players standing directly on top of the controller block.
    private List<ServerPlayer> getPlayersOnPlatform() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return List.of();
        }

        // Detection zone is 1 block directly above the controller
        BlockPos controllerPos = getBlockPos();
        BlockPos detectionPos = controllerPos.above(1);

        // 1x1 detection area (requires players to stand on top of the controller)
        AABB detectionZone = new AABB(
                detectionPos,
                detectionPos.offset(1, 1, 1));

        return serverLevel.getEntitiesOfClass(ServerPlayer.class, detectionZone);
    }

    // Teleport a player to the Deep Below dimension.
    private boolean teleportPlayerToDeepBelow(ServerPlayer player) {
        if (!(getLevel() instanceof ServerLevel currentLevel)) {
            return false;
        }

        // Save origin data to player capability
        Optional.of(player.getData(CosmicAttachmentTypes.TELEPORT_ORIGIN)).ifPresent(cap -> {
            cap.setOriginDimension(currentLevel.dimension());
            cap.setOriginPosition(player.position());
            cap.setOriginRotation(player.getYRot(), player.getXRot());
        });

        // Get Deep Below dimension
        ResourceKey<Level> targetDim = getTargetDimension();
        ServerLevel deepBelow = player.server.getLevel(targetDim);

        if (deepBelow == null) {
            player.displayClientMessage(
                    Component.translatable("cosmiccore.divingbell.dimension_missing"), true);
            return false;
        }

        // Find or create safe landing
        BlockPos landingPos = getOrCreateSafeLanding(deepBelow, player);

        // Teleport (SafeTeleporter handles safety effects)
        player.changeDimension(deepBelow, new SafeTeleporter(landingPos));

        // Success message
        player.displayClientMessage(
                Component.translatable("cosmiccore.divingbell.descended"), true);

        return true;
    }

    // Get the target dimension (Deep Below).
    private ResourceKey<Level> getTargetDimension() {
        ResourceLocation dimLoc = ResourceLocation.parse(TARGET_DIMENSION);
        return ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimLoc);
    }

    // Find or create a safe landing platform in the Deep Below.
    private BlockPos getOrCreateSafeLanding(ServerLevel deepBelow, ServerPlayer player) {
        TeleportPadRegistry registry = TeleportPadRegistry.get(deepBelow);

        // Landing pad uses same X/Z as player's current position (vertical teleport)
        BlockPos currentPos = player.blockPosition();
        int targetX = currentPos.getX();
        int targetZ = currentPos.getZ();

        // Find safe Y level for this X/Z position
        BlockPos safePos = LandingZoneHelper.findSafeYLevel(deepBelow, targetX, targetZ, DESTINATION_SEARCH_START_Y);

        // Check if escape pad already exists at this position
        if (registry.hasPadAt(safePos) &&
                LandingZoneHelper.isPadIntact(deepBelow, safePos, CosmicBlocks.DIVING_BELL_ESCAPE_PAD.get())) {
            // Reuse existing pad
            return safePos;
        }

        // Need to create new platform
        LandingZoneHelper.buildPlatform(deepBelow, safePos, new LandingZoneHelper.PlatformOptions(
                Blocks.STONE,
                CosmicBlocks.DIVING_BELL_ESCAPE_PAD.get(),
                1 // 3x3 platform
        ));

        // Register in saved data
        registry.registerPad(safePos);

        return safePos;
    }
}
