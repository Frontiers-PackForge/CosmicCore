package com.ghostipedia.cosmiccore.common.dimension;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.FirmamentTideHudPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FirmamentAscentLogic {

    private static final int ASCENT_TICKS = 40;
    private static final int FADE_START_TICK = 24;
    private static final double ASCENT_SPEED = 0.78;
    private static final Map<ServerPlayer, AscentState> ASCENTS = new WeakHashMap<>();

    private FirmamentAscentLogic() {}

    public static boolean begin(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD) || ASCENTS.containsKey(player)) return false;
        ASCENTS.put(player, new AscentState());
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0f, 1.18f);
        return true;
    }

    public static boolean isAscending(ServerPlayer player) {
        return ASCENTS.containsKey(player);
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AscentState state = ASCENTS.get(player);
        if (state == null) return;
        if (player.isDeadOrDying() || !player.level().dimension().equals(Level.OVERWORLD)) {
            cancel(player);
            return;
        }

        state.ticks++;
        player.fallDistance = 0.0f;
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x * 0.82, ASCENT_SPEED, motion.z * 0.82);
        player.hasImpulse = true;
        player.hurtMarked = true;
        if ((state.ticks & 1) == 0) emitAscentParticles(player, state.ticks);
        if (state.ticks == FADE_START_TICK) {
            CCoreNetwork.sendToPlayer(player,
                    new FirmamentTideHudPacket(FirmamentTideHudPacket.ASCENDING, 1.0f));
        }
        if (state.ticks >= ASCENT_TICKS) transfer(player);
    }

    private static void transfer(ServerPlayer player) {
        ASCENTS.remove(player);
        ServerLevel destination = player.server.getLevel(FirmamentDimension.KEY);
        if (destination == null) {
            cancelPresentation(player);
            return;
        }
        BlockPos landing = findLanding(destination, player);
        player.stopRiding();
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
        player.teleportTo(destination, landing.getX() + 0.5, landing.getY() + 0.05, landing.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        if (player.level().dimension().equals(FirmamentDimension.KEY)) {
            CCoreNetwork.sendToPlayer(player,
                    new FirmamentTideHudPacket(FirmamentTideHudPacket.ENTERED, 1.0f));
        } else {
            cancelPresentation(player);
        }
    }

    private static BlockPos findLanding(ServerLevel destination, ServerPlayer player) {
        RandomSource random = RandomSource.create(destination.getSeed() ^ player.getUUID().getLeastSignificantBits() ^
                destination.getGameTime());
        int originX = Mth.floor(player.getX());
        int originZ = Mth.floor(player.getZ());
        for (int attempt = 0; attempt < 2; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            int distance = 192 + random.nextInt(321);
            int x = originX + Mth.floor(Math.cos(angle) * distance);
            int z = originZ + Mth.floor(Math.sin(angle) * distance);
            BlockPos landing = FirmamentPortalBlock.findFirmamentLowerSurface(destination, x, z);
            if (landing != null) return landing;
        }
        BlockPos landing = FirmamentPortalBlock.findFirmamentLowerSurface(destination, originX, originZ);
        if (landing != null) return landing;
        return buildFallbackLanding(destination, originX, originZ);
    }

    private static BlockPos buildFallbackLanding(ServerLevel level, int x, int z) {
        BlockPos landing = new BlockPos(x, 64, z);
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                level.setBlock(landing.offset(offsetX, -1, offsetZ),
                        CosmicBlocks.FIRMAMENT_SAPROLITE.getDefaultState(), 3);
            }
        }
        level.setBlock(landing, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(landing.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(landing.above(2), Blocks.AIR.defaultBlockState(), 3);
        return landing;
    }

    private static void emitAscentParticles(ServerPlayer player, int tick) {
        ServerLevel level = player.serverLevel();
        double radius = 0.45 + tick * 0.015;
        double angle = tick * 0.72;
        level.sendParticles(ParticleTypes.CLOUD,
                player.getX() + Math.cos(angle) * radius,
                player.getY() + 0.15,
                player.getZ() + Math.sin(angle) * radius,
                3, 0.12, 0.08, 0.12, 0.025);
        level.sendParticles(ParticleTypes.END_ROD,
                player.getX() - Math.cos(angle) * radius,
                player.getY() + 0.35,
                player.getZ() - Math.sin(angle) * radius,
                1, 0.04, 0.12, 0.04, 0.012);
    }

    private static void cancel(ServerPlayer player) {
        ASCENTS.remove(player);
        cancelPresentation(player);
    }

    private static void cancelPresentation(ServerPlayer player) {
        CCoreNetwork.sendToPlayer(player,
                new FirmamentTideHudPacket(FirmamentTideHudPacket.HIDDEN, 0.0f));
    }

    private static final class AscentState {

        private int ticks;
    }
}
