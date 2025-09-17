package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OxygenLogic {

    private OxygenLogic() {}

    private static final long MAX_OXYGEN_TICKS = 20L * 90; // 90s
    private static final int[] WARNING_SECONDS = {60, 30, 15, 10, 5};

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        ServerLevel level = player.serverLevel();

        player.getCapability(OxygenBudgetCap.CAP).ifPresent(cap -> {
            // seed per-dimension bucket
            if (cap.getOxygenTicks(level.dimension()) < 0) {
                cap.setOxygenTicks(level.dimension(), MAX_OXYGEN_TICKS);
                cap.setRegenBuffer(level.dimension(), 0.0);
            }

            int yValue = player.blockPosition().getY();
            OxygenRules.AirRanges range = OxygenRules.getRanges(level.dimension(), yValue);

            OxygenRules.AirQuality quality;
            OxygenRules.Rates rates;
            if (range == null) {
                quality = OxygenRules.AirQuality.SAFE;
                rates = OxygenRules.QUALITY_RATES.get(quality).copy();
            } else {
                quality = range.quality;
                rates = range.airRangeRates();
            }

            // If the player's eyes are inside ANY fluid, force at least NO_AIR behavior
            BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
            boolean eyesInFluid = !level.getFluidState(eyePos).isEmpty();
            if (eyesInFluid) {
                OxygenRules.Rates noAir = OxygenRules.QUALITY_RATES.get(OxygenRules.AirQuality.NO_AIR).copy();
                // take the stricter drain and damage
                rates.oxygenDrainPerTick = Math.max(rates.oxygenDrainPerTick, noAir.oxygenDrainPerTick);
                rates.suffocationDamage = Math.max(rates.suffocationDamage, 2.0f);
                quality = OxygenRules.AirQuality.NO_AIR;
            }

            long current = cap.getOxygenTicks(level.dimension());
            cap.setConsuming(level.dimension(), rates.oxygenDrainPerTick > 0);
            if (rates.oxygenDrainPerTick > 0) {
                long next = Math.max(0, current - rates.oxygenDrainPerTick);
                cap.setOxygenTicks(level.dimension(), next);

                if (next % 20 == 0) {
                    int sec = (int) (next / 20);
                    for (int w : WARNING_SECONDS) {
                        if (sec == w) {
                            CCoreNetwork.sendToPlayer(player, new OxygenWarnPacket("cosmiccore.oxygen.warn", w));
                            break;
                        }
                    }
                }
                //TODO; Our Own damage source and death message?
                if (next <= 0 && rates.suffocationDamage > 0f && (level.getGameTime() % 20) == 0) {
                    player.hurt(player.damageSources().drown(), rates.suffocationDamage);
                }

            } else if (rates.oxygenRecoveryPerTick > 0 && current < MAX_OXYGEN_TICKS) {
                double buffer = cap.getRegenBuffer(level.dimension()) + (rates.oxygenRecoveryPerTick / 20.0);
                long gain = (long) (buffer * 20.0);
                double rem = buffer - (gain / 20.0);

                if (gain > 0) {
                    cap.setOxygenTicks(level.dimension(), Math.min(MAX_OXYGEN_TICKS, current + gain));
                }
                cap.setRegenBuffer(level.dimension(), rem);
            }

            // HUD update (every 10 ticks)
            if ((level.getGameTime() % 10) == 0) {
                long remaining = cap.getOxygenTicks(level.dimension());
                boolean show = (quality != OxygenRules.AirQuality.SAFE) || remaining < MAX_OXYGEN_TICKS;
                CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS, show));
            }
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();

        player.getCapability(OxygenBudgetCap.CAP).ifPresent(cap -> {
            if (cap.getOxygenTicks(level.dimension()) < 0) {
                cap.setOxygenTicks(level.dimension(), MAX_OXYGEN_TICKS);
                cap.setRegenBuffer(level.dimension(), 0.0);
            }
            long remaining = cap.getOxygenTicks(level.dimension());
            boolean show = remaining < MAX_OXYGEN_TICKS;
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS, show));
        });
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level   = player.serverLevel();

        player.getCapability(OxygenBudgetCap.CAP).ifPresent(cap -> {
            cap.setOxygenTicks(level.dimension(), MAX_OXYGEN_TICKS);
            cap.setRegenBuffer(level.dimension(), 0.0);
            cap.setConsuming(level.dimension(), false);

            CCoreNetwork.sendToPlayer(player,
                    new SyncOxygenBarPacket(MAX_OXYGEN_TICKS, MAX_OXYGEN_TICKS, false));
        });
    }
}
