package com.ghostipedia.cosmiccore.common.airControl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.armor.SpaceArmorComponentItem;
import com.ghostipedia.cosmiccore.common.airControl.RebreatherHelper.RebreatherType;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.OxygenWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncOxygenBarPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import static com.ghostipedia.cosmiccore.common.airControl.OxygenConfig.*;
import static com.ghostipedia.cosmiccore.common.airControl.OxygenItemCap.OXYGEN_SUPPLY;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class OxygenLogic {

    private OxygenLogic() {}

    // Track the oxygen value at last HUD sync to calculate accurate rate (per-player)
    private static final java.util.Map<java.util.UUID, Long> lastSyncOxygenValue = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> lastSyncGameTime = new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;

        // Skip oxygen logic for creative/spectator players
        if (player.isCreative() || player.isSpectator()) {
            // Send hide packet if needed
            if ((player.serverLevel().getGameTime() % HUD_SYNC_INTERVAL) == 0) {
                CCoreNetwork.sendToPlayer(player,
                        new SyncOxygenBarPacket(MAX_OXYGEN_TICKS, MAX_OXYGEN_TICKS, false, 0.0));
            }
            return;
        }

        ServerLevel level = player.serverLevel();

        player.getCapability(OxygenBudgetCap.CAP).ifPresent(cap -> {
            // Initialize if needed
            if (cap.getOxygenTicks(level.dimension()) < 0) {
                cap.setOxygenTicks(level.dimension(), MAX_OXYGEN_TICKS);
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
            if (eyesInFluid) {
                OxygenRules.Rates thinAir = OxygenRules.QUALITY_RATES.get(OxygenRules.AirQuality.THIN).copy();
                rates.oxygenDrainPerTick = Math.max(rates.oxygenDrainPerTick, thinAir.oxygenDrainPerTick);
                rates.oxygenRecoveryPerTick = 0.0; // No passive regen while submerged
                rates.suffocationDamage = Math.max(rates.suffocationDamage, 2.0f);
                quality = OxygenRules.AirQuality.THIN;
            }

            long current = cap.getOxygenTicks(level.dimension());
            cap.setConsuming(level.dimension(), rates.oxygenDrainPerTick > 0);

            if (rates.oxygenDrainPerTick > 0) {
                // Clear regen buffer when draining - prevents accumulated regen from triggering later
                cap.setRegenBuffer(level.dimension(), 0.0);

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

                // Apply multiplier to drain rate
                int baseDrain = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, rates.oxygenDrainPerTick));
                int drain = (int) Math.ceil(baseDrain * drainMult);

                // Tanks can only be used with pressurized rebreather
                int providedByTanks = 0;
                if (rebreather == RebreatherType.PRESSURIZED) {
                    providedByTanks = drainFromCarriedTanks(player, drain);
                }
                int cover = Math.min(providedByTanks, drain);
                int remainingDrain = Math.max(0, drain - cover);

                long next = current - remainingDrain;
                next = Math.max(0, Math.min(MAX_OXYGEN_TICKS, next));

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
                    player.hurt(player.damageSources().drown(), rates.suffocationDamage);
                }

            } else if (rates.oxygenRecoveryPerTick > 0 && current < MAX_OXYGEN_TICKS) {
                // Passive recovery in safe air
                double buffer = cap.getRegenBuffer(level.dimension()) + (rates.oxygenRecoveryPerTick / 20.0);
                long gain = (long) (buffer * 20.0);
                double rem = buffer - (gain / 20.0);

                if (gain > 0) {
                    long next = Math.min(MAX_OXYGEN_TICKS, current + gain);
                    cap.setOxygenTicks(level.dimension(), next);
                }
                cap.setRegenBuffer(level.dimension(), rem);
            }

            // HUD sync - calculate rate based on change since last sync
            if ((level.getGameTime() % HUD_SYNC_INTERVAL) == 0) {
                long remaining = cap.getOxygenTicks(level.dimension());
                boolean show = (quality != OxygenRules.AirQuality.SAFE) || remaining < MAX_OXYGEN_TICKS;

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
                        new SyncOxygenBarPacket(remaining, MAX_OXYGEN_TICKS, show, ratePerSecond));
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

        player.getCapability(OxygenBudgetCap.CAP).ifPresent(cap -> {
            cap.setOxygenTicks(level.dimension(), MAX_OXYGEN_TICKS);
            cap.setRegenBuffer(level.dimension(), 0.0);
            cap.setConsuming(level.dimension(), false);
            CCoreNetwork.sendToPlayer(player, new SyncOxygenBarPacket(MAX_OXYGEN_TICKS, MAX_OXYGEN_TICKS, false, 0.0));
        });
    }

    // --- Oxygen provider draining ---

    /**
     * Drain oxygen from all available sources.
     * Priority: Space suit chestplate > Ad Astra suit > Curios back slot
     * Note: Tanks in inventory do NOT work - must be equipped in Curios back slot
     */
    private static int drainFromCarriedTanks(ServerPlayer player, int requestTicks) {
        if (requestTicks <= 0) return 0;

        int remaining = requestTicks;

        // 1. Check CosmicCore space suit chestplate first (highest priority)
        remaining = drainFromSpaceSuit(player, remaining);
        if (remaining <= 0) return requestTicks;

        // 2. Check vanilla Ad Astra space suit
        remaining = drainFromAdAstraSuit(player, remaining);
        if (remaining <= 0) return requestTicks;

        // 3. Check Curios back slot (oxygen tanks worn on back)
        // Tanks MUST be equipped in Curios back slot to work - inventory tanks are ignored
        remaining = drainFromCuriosBackSlot(player, remaining);

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
            var curiosHandler = curiosCap.resolve().get();
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
     * Drain from CosmicCore SpaceArmorComponentItem (nano/quantum/sanguine suits).
     * Consumes 1 mB every SPACE_SUIT_TICKS_PER_MB game ticks to slow drain rate.
     */
    private static int drainFromSpaceSuit(ServerPlayer player, int requestTicks) {
        if (requestTicks <= 0) return 0;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty()) return requestTicks;
        if (!(chestStack.getItem() instanceof SpaceArmorComponentItem suit)) return requestTicks;

        if (!suit.hasOxygen(player)) return requestTicks;

        // Only drain 1 mB every SPACE_SUIT_TICKS_PER_MB game ticks
        // This makes suits last much longer than basic tanks
        if ((player.serverLevel().getGameTime() % SPACE_SUIT_TICKS_PER_MB) == 0) {
            suit.consumeOxygen(chestStack, 1);
        }

        // Return 0 remaining if we have oxygen (suit provides full coverage)
        return suit.hasOxygen(player) ? 0 : requestTicks;
    }

    /**
     * Drain from vanilla Ad Astra SpaceSuitItem.
     * Consumes 1 mB every SPACE_SUIT_TICKS_PER_MB game ticks to slow drain rate.
     */
    private static int drainFromAdAstraSuit(ServerPlayer player, int requestTicks) {
        if (requestTicks <= 0) return 0;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty()) return requestTicks;
        if (!(chestStack.getItem() instanceof SpaceSuitItem suit)) return requestTicks;

        if (!SpaceSuitItem.hasOxygen(player)) return requestTicks;

        // Only drain 1 mB every SPACE_SUIT_TICKS_PER_MB game ticks
        if ((player.serverLevel().getGameTime() % SPACE_SUIT_TICKS_PER_MB) == 0) {
            suit.consumeOxygen(chestStack, 1);
        }

        // Return 0 remaining if suit still has oxygen
        return SpaceSuitItem.hasOxygen(player) ? 0 : requestTicks;
    }

    /**
     * Drain from GTCEu-style oxygen supply tanks via capability.
     */
    private static int drainFromStack(ItemStack stack, int requestTicks) {
        if (stack.isEmpty() || requestTicks <= 0) return requestTicks;

        return stack.getCapability(OXYGEN_SUPPLY)
                .map(provider -> {
                    int got = Math.max(0, provider.drainOxygenTicks(stack, requestTicks));
                    return Math.max(0, requestTicks - got);
                })
                .orElse(requestTicks);
    }
}
