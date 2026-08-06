package com.ghostipedia.cosmiccore.common.data.temperature;

import com.ghostipedia.cosmiccore.common.data.worldgen.firmament.FirmamentMiddleBandLayout;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import sfiomn.legendarysurvivaloverhaul.api.temperature.ModifierBase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FirmamentTemperatureModifier extends ModifierBase {

    private static final float ARCHIPELAGO_COOLING = -8.0f;
    private static final float STORM_HEATING = 23.0f;
    private static final float WIND_HEATING = 2.0f;
    private static final float SUN_HEATING = 4.0f;
    private static final int SOLAR_RANGE = 48;
    private static final int SOLAR_SAMPLE_INTERVAL = 20;
    private static final int SOLAR_BLOCK_THRESHOLD = 8;

    private final Map<UUID, SolarSample> solarSamples = new HashMap<>();

    @Override
    public float getWorldInfluence(Player player, Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server) || !level.dimension().equals(FirmamentDimension.KEY)) {
            return 0.0f;
        }

        double storm = smooth(FirmamentMiddleBandLayout.stormEnvelope(pos.getY()));
        double wind = FirmamentMiddleBandLayout.sampleWind(pos.getX() + 0.5, pos.getZ() + 0.5).strength();
        double sunlight = solarExposure(player, server, pos);
        return (float) (ARCHIPELAGO_COOLING + STORM_HEATING * storm + WIND_HEATING * storm * wind +
                SUN_HEATING * sunlight * (1.0 - storm));
    }

    private double solarExposure(Player player, ServerLevel level, BlockPos pos) {
        if (player == null) {
            return hasEastSun(level, pos) ? 1.0 : 0.0;
        }

        long gameTime = level.getGameTime();
        SolarSample cached = solarSamples.get(player.getUUID());
        if (cached != null && gameTime - cached.gameTime() < SOLAR_SAMPLE_INTERVAL &&
                cached.position().distManhattan(pos) <= 4) {
            return cached.exposure();
        }

        double exposure = hasEastSun(level, pos) ? 1.0 : 0.0;
        solarSamples.put(player.getUUID(), new SolarSample(gameTime, pos.immutable(), exposure));
        return exposure;
    }

    private static boolean hasEastSun(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int sampleY = pos.getY() + 1;
        for (int offset = 1; offset <= SOLAR_RANGE; offset++) {
            cursor.set(pos.getX() + offset, sampleY, pos.getZ());
            if (!level.isLoaded(cursor)) {
                return false;
            }
            BlockState state = level.getBlockState(cursor);
            if (state.getLightBlock(level, cursor) >= SOLAR_BLOCK_THRESHOLD) {
                return false;
            }
        }
        return true;
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private record SolarSample(long gameTime, BlockPos position, double exposure) {}
}
