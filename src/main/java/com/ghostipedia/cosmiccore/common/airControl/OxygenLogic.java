package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.airControl.RebreatherHelper.RebreatherType;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.DepthsBargain;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Optional;

import static com.ghostipedia.cosmiccore.common.airControl.OxygenConfig.*;
import static com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap.OXYGEN_SUPPLY;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OxygenLogic {

    private OxygenLogic() {}

    private static final ThreadLocal<Boolean> CONSUME_BYPASS = ThreadLocal.withInitial(() -> false);

    public static boolean isConsumeBypass() {
        return CONSUME_BYPASS.get();
    }

    private static final java.util.Map<java.util.UUID, Long> lastSyncOxygenValue = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> lastSyncGameTime = new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();

        // Skip oxygen logic for creative/spectator players
        if (player.isCreative() || player.isSpectator()) {
            // Send hide packet if needed
            if ((player.serverLevel().getGameTime() % HUD_SYNC_INTERVAL) == 0) {
                long playerMaxOxygen = getMaxOxygenTicks(player);
                CCoreNetwork.sendToPlayer(player,
                        new SyncOxygenBarPacket(playerMaxOxygen, playerMaxOxygen, false, 0.0));
            }
            return;
        }

        ServerLevel level = player.serverLevel();

        Optional.of(player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET)).ifPresent(cap -> {
            // Get player-specific max capacity (may be modified by bargains)
            long playerMaxOxygen = getMaxOxygenTicks(player);

            // Initialize if needed
            if (cap.getOxygenTicks(level.dimension()) < 0) {
                cap.setOxygenTicks(level.dimension(), playerMaxOxygen);
                cap.setRegenBuffer(level.dimension(), 0.0);
            }

            // Get player's Y and determine air quality
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

            // Check if player is in a fluid (eyes submerged)
            BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
            boolean eyesInFluid = !level.getFluidState(eyePos).isEmpty();
            if (eyesInFluid && !player.hasEffect(MobEffects.WATER_BREATHING)) {
                OxygenRules.Rates thinAir = OxygenRules.QUALITY_RATES.get(OxygenRules.AirQuality.THIN).copy();
                rates.oxygenDrainPerTick = Math.max(rates.oxygenDrainPerTick, thinAir.oxygenDrainPerTick);
                rates.oxygenRecoveryPerTick = 0.0; // No passive regen while submerged
                rates.suffocationDamage = Math.max(rates.suffocationDamage, 2.0f);
                quality = OxygenRules.AirQuality.THIN;
            }

            long current = cap.getOxygenTicks(level.dimension());
            cap.setConsuming(level.dimension(), rates.oxygenDrainPerTick > 0);

            if (rates.oxygenDrainPerTick > 0) {
                // Check for rebreather equipment and apply drain modifiers
                RebreatherType rebreather = RebreatherHelper.getEquippedRebreather(player);
                double drainMult = 1.0;

                // Apply rebreather effects based on air quality
                if (quality == OxygenRules.AirQuality.THIN) {
                    // Both rebreathers work in THIN air
                    if (rebreather == RebreatherType.PRESSURIZED) {
                        drainMult = PRESSURIZED_REBREATHER_DRAIN_MULT;
                    } else if (rebreather == RebreatherType.SIMPLE) {
                        drainMult = SIMPLE_REBREATHER_DRAIN_MULT;
                    }
                } else if (quality == OxygenRules.AirQuality.NO_AIR) {
                    // Only pressurized rebreather works in NO_AIR
                    if (rebreather == RebreatherType.PRESSURIZED) {
                        drainMult = PRESSURIZED_REBREATHER_DRAIN_MULT;
                    }
                }
                // TOXIC and ABYSS are not affected by rebreathers

                // Apply multiplier to drain rate using fractional accumulation
                // This ensures rebreathers actually reduce effective drain (e.g., 0.5x means drain every other tick)
                double baseDrain = rates.oxygenDrainPerTick;
                double fractionalDrain = baseDrain * drainMult;

                // Use the regen buffer to accumulate fractional drain (stored as negative when draining)
                double buffer = cap.getRegenBuffer(level.dimension());
                // If buffer was positive (from regen), reset it since we're now draining
                if (buffer > 0) buffer = 0;
                // Buffer is stored as negative during drain, so negate to get positive accumulator
                double drainAccum = -buffer + fractionalDrain;
                int drain = (int) drainAccum; // Integer part is actual drain this tick
                double remainder = drainAccum - drain; // Fractional part carries over
                cap.setRegenBuffer(level.dimension(), -remainder); // Store negative to indicate drain mode

                int provided = drainFromOxygenSources(player, drain, rebreather);
                int remainingDrain = Math.max(0, drain - provided);

                long next = current - remainingDrain;
                next = Math.max(0, Math.min(playerMaxOxygen, next));

                cap.setOxygenTicks(level.dimension(), next);

                // Warnings & damage
                if (next % 20 == 0) {
                    int sec = (int) (next / 20);
                    for (int w : WARNING_SECONDS) {
                        if (sec == w) {
                            CCoreNetwork.sendToPlayer(player, new OxygenWarnPacket("cosmiccore.oxygen.warn", w));
                            break;
                        }
                    }
                }
                if (next <= 0 && rates.suffocationDamage > 0f &&
                        (level.getGameTime() % SUFFOCATION_DAMAGE_INTERVAL) == 0) {
                    // Check for Depths bargain - instant death instead of gradual damage
                    if (DepthsBargain.shouldInstantKillOnSuffocation(player)) {
                        DepthsBargain.executeInstantSuffocation(player);
                    } else {
                        player.hurt(player.damageSources().drown(), rates.suffocationDamage);
                    }
                }

            } else if (rates.oxygenRecoveryPerTick > 0 && current < playerMaxOxygen) {
                // Passive recovery in safe air
                double buffer = cap.getRegenBuffer(level.dimension());
                // If buffer was negative (from drain mode), reset it since we're now recovering
                if (buffer < 0) buffer = 0;
                buffer += (rates.oxygenRecoveryPerTick / 20.0);
                long gain = (long) (buffer * 20.0);
                double rem = buffer - (gain / 20.0);

                if (gain > 0) {
                    long next = Math.min(playerMaxOxygen, current + gain);
                    cap.setOxygenTicks(level.dimension(), next);
                }
                cap.setRegenBuffer(level.dimension(), rem);
            }

            // HUD sync - calculate rate based on change since last sync
            if ((level.getGameTime() % HUD_SYNC_INTERVAL) == 0) {
                long remaining = cap.getOxygenTicks(level.dimension());
                boolean show = (quality != OxygenRules.AirQuality.SAFE) || remaining < playerMaxOxygen;

                // Calculate rate based on actual change over the sync interval
                java.util.UUID playerId = player.getUUID();
                double ratePerSecond = 0.0;
                long currentGameTime = level.getGameTime();
                Long prevOxygen = lastSyncOxygenValue.get(playerId);
                Long prevTime = lastSyncGameTime.get(playerId);
                if (prevOxygen != null && prevTime != null) {
                    long ticksElapsed = currentGameTime - prevTime;
                    if (ticksElapsed > 0) {
                        long oxygenChange = remaining - prevOxygen;
                        // Convert to per-second rate: (change / ticks) * 20 ticks/second
                        ratePerSecond = (oxygenChange * 20.0) / ticksElapsed;
                    }
                }

                // Update tracking for next sync
                lastSyncOxygenValue.put(playerId, remaining);
                lastSyncGameTime.put(playerId, currentGameTime);

                CCoreNetwork.sendToPlayer(player,
                        new SyncOxygenBarPacket(remaining, playerMaxOxygen, show, ratePerSecond));
            }
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();

        // Reset rate tracking for fresh rate calculation
        java.util.UUID playerId = player.getUUID();
        lastSyncOxygenValue.remove(playerId);
        lastSyncGameTime.remove(playerId);

        Optional.of(player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET)).ifPresent(cap -> {
            long playerMaxOxygen = getMaxOxygenTicks(player);
            if (cap.getOxygenTicks(level.dimension()) < 0) {
                cap.setOxygenTicks(level.dimension(), playerMaxOxygen);
                cap.setRegenBuffer(level.dimension(), 0.0);
            }
            long remaining = cap.getOxygenTicks(level.dimension());
            boolean show = remaining < playerMaxOxygen;
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(remaining, playerMaxOxygen, show, 0.0));
        });
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up tracking maps to prevent memory leaks
        java.util.UUID playerId = event.getEntity().getUUID();
        lastSyncOxygenValue.remove(playerId);
        lastSyncGameTime.remove(playerId);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();

        Optional.of(player.getData(CosmicAttachmentTypes.OXYGEN_BUDGET)).ifPresent(cap -> {
            long playerMaxOxygen = getMaxOxygenTicks(player);
            cap.setOxygenTicks(level.dimension(), playerMaxOxygen);
            cap.setRegenBuffer(level.dimension(), 0.0);
            cap.setConsuming(level.dimension(), false);
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(playerMaxOxygen, playerMaxOxygen, false, 0.0));
        });
    }

    // --- Oxygen provider draining ---

    /**
     * Drain oxygen from all available sources.
     * Priority: Space suit chestplate > Ad Astra suit > Curios back slot tanks
     * Suits always provide protection. Tanks require pressurized rebreather.
     */
    private static int drainFromOxygenSources(ServerPlayer player, int requestTicks, RebreatherType rebreather) {
        if (requestTicks <= 0) return 0;

        int remaining = requestTicks;

        // TODO(stellaris): re-add space-suit oxygen drain when the AA/Botarium fluid-suit system is reworked (bead
        // cosmiccore-42.13)
        if (rebreather == RebreatherType.PRESSURIZED) {
            remaining = drainFromCuriosBackSlot(player, remaining);
        }

        return requestTicks - remaining;
    }

    /**
     * Drain from oxygen tanks in Curios back slot.
     */
    private static int drainFromCuriosBackSlot(ServerPlayer player, int requestTicks) {
        if (requestTicks <= 0) return 0;

        int remaining = requestTicks;

        var curiosCap = CuriosApi.getCuriosInventory(player);
        if (curiosCap.isPresent()) {
            var curiosHandler = curiosCap.get();
            var backHandler = curiosHandler.getStacksHandler("back");
            if (backHandler.isPresent()) {
                IDynamicStackHandler stacks = backHandler.get().getStacks();
                for (int i = 0; i < stacks.getSlots() && remaining > 0; i++) {
                    ItemStack stack = stacks.getStackInSlot(i);
                    remaining = drainFromStack(stack, remaining);
                }
            }
        }

        return remaining;
    }

    /**
     * Drain from GTCEu-style oxygen supply tanks via capability.
     */
    private static int drainFromStack(ItemStack stack, int requestTicks) {
        if (stack.isEmpty() || requestTicks <= 0) return requestTicks;

        IOxygenSupplyItem provider = stack.getCapability(OXYGEN_SUPPLY);
        if (provider == null) return requestTicks;
        int got = Math.max(0, provider.drainOxygenTicks(stack, requestTicks));
        return Math.max(0, requestTicks - got);
    }
}
