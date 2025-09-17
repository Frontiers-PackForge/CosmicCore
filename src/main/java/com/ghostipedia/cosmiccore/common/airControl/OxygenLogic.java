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

import static com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap.OXYGEN_SUPPLY;

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

            BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
            boolean eyesInFluid = !level.getFluidState(eyePos).isEmpty();
            //If the Player is inside of a fluid
            if (eyesInFluid) {
                OxygenRules.Rates thinAir = OxygenRules.QUALITY_RATES.get(OxygenRules.AirQuality.THIN).copy();
                rates.oxygenDrainPerTick = Math.max(rates.oxygenDrainPerTick, thinAir.oxygenDrainPerTick);
                rates.suffocationDamage = Math.max(rates.suffocationDamage, 2.0f);
                quality = OxygenRules.AirQuality.THIN;
            }

            long current = cap.getOxygenTicks(level.dimension());
            cap.setConsuming(level.dimension(), rates.oxygenDrainPerTick > 0);

            if (rates.oxygenDrainPerTick > 0) {
                int requested = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, rates.oxygenDrainPerTick));
                int providedByTanks = drainFromCarriedTanks(player, requested); // new helper below
                long remainingDrain = Math.max(0L, rates.oxygenDrainPerTick - providedByTanks);

                // 2) Only if tanks didn’t cover it all, drain the player’s internal oxygen
                long next = Math.max(0L, current - remainingDrain);
                cap.setOxygenTicks(level.dimension(), next);

                // existing warn/damage logic stays the same
                if (next % 20 == 0) {
                    int sec = (int) (next / 20);
                    for (int w : WARNING_SECONDS) {
                        if (sec == w) {
                            CCoreNetwork.sendToPlayer(player, new OxygenWarnPacket("cosmiccore.oxygen.warn", w));
                            break;
                        }
                    }
                }
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

            if ((level.getGameTime() % 10) == 0) {
                long remaining = cap.getOxygenTicks(level.dimension());
                boolean show = (quality != OxygenRules.AirQuality.SAFE) || remaining < MAX_OXYGEN_TICKS;
                double ratePerSecond = 0.0;
                if (rates.oxygenDrainPerTick > 0) {
                    ratePerSecond = -(rates.oxygenDrainPerTick * 20.0);
                } else if (rates.oxygenRecoveryPerTick > 0 && remaining < MAX_OXYGEN_TICKS) {
                    ratePerSecond = (rates.oxygenRecoveryPerTick * 20.0);
                }
                CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS, show, ratePerSecond));
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
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS, show, 0.0));
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
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(MAX_OXYGEN_TICKS, MAX_OXYGEN_TICKS, false, 0.0));
        });
    }

    // --- Tanks-first draining ---
    private static int drainFromCarriedTanks(ServerPlayer player, int requestTicks) {
        if (requestTicks <= 0) return 0;

        int remaining = requestTicks;

        // offhand
        remaining = drainFromStack(player.getOffhandItem(), remaining);
        // mainhand
        remaining = drainFromStack(player.getMainHandItem(), remaining);

        // hotbar 0..8
        for (int i = 0; i < 9 && remaining > 0; i++) {
            remaining = drainFromStack(player.getInventory().getItem(i), remaining);
        }
        // rest of inventory
        for (int i = 9; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            remaining = drainFromStack(player.getInventory().getItem(i), remaining);
        }

        return requestTicks - remaining;
    }

    private static int drainFromStack(net.minecraft.world.item.ItemStack stack, int requestTicks) {
        if (stack.isEmpty() || requestTicks <= 0) return requestTicks;

        // Ask for our custom oxygen-supply cap the tank behavior exposes.
        return stack.getCapability(OXYGEN_SUPPLY)
                .map(provider -> {
                    int got = Math.max(0, provider.drainOxygenTicks(stack, requestTicks));
                    // Return remaining request
                    return Math.max(0, requestTicks - got);
                })
                .orElse(requestTicks);
    }

}
