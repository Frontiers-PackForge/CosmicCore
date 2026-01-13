package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.BackBargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.QuakeMovementBargain;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;
import com.ghostipedia.cosmiccore.common.reflection.whisper.WhisperSystem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Reflection system events - deaths, ticks, thresholds, awakening.
 */
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class ReflectionEventHandler {

    // Track players who just died so we can whisper on respawn
    private static final Map<UUID, String> pendingDeathWhispers = new HashMap<>();

    // Track pending respawn events (what UI to open after respawn)
    private static final Map<UUID, RespawnEvent> pendingRespawnEvents = new HashMap<>();

    /**
     * Types of respawn events that trigger UI
     */
    private enum RespawnEventType {
        AWAKENING,           // First 3rd death - offer quake movement
        THRESHOLD,           // Crossed erosion milestone
        CONTEXTUAL_BARGAIN   // Offer relevant bargain (e.g., /back after death)
    }

    private record RespawnEvent(RespawnEventType type, int data) {

        static RespawnEvent awakening() {
            return new RespawnEvent(RespawnEventType.AWAKENING, 0);
        }

        static RespawnEvent threshold(int index) {
            return new RespawnEvent(RespawnEventType.THRESHOLD, index);
        }

        static RespawnEvent contextualBargain(int bargainType) {
            // 0 = back bargain, 1 = home bargain (can expand later)
            return new RespawnEvent(RespawnEventType.CONTEXTUAL_BARGAIN, bargainType);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Record death location for /back bargain
        BackBargain.recordDeath(player);

        ReflectionCapability.get(player).ifPresent(reflection -> {
            int oldErosion = reflection.getErosion();
            int oldDeathCount = reflection.getDeathCount();

            // Record death - adds 1 erosion
            reflection.recordDeath();

            // Remember the death cause for whispers/context
            String deathCause = categorizeDeathCause(event.getSource().getMsgId());
            reflection.rememberEvent("death." + deathCause);
            reflection.rememberEvent("death.total");

            // Determine what event to queue for respawn
            // Priority: Awakening > Threshold > Contextual Bargain

            // Check for awakening (first time reaching 3 deaths)
            boolean justAwakened = false;
            if (!reflection.hasAwakened() && reflection.getDeathCount() >= ReflectionConstants.DEATHS_TO_AWAKEN) {
                reflection.setAwakened(true);
                justAwakened = true;
                CosmicCore.LOGGER.info("Reflection awakened for player {}", player.getName().getString());
            }

            // Queue respawn event
            if (justAwakened && !reflection.hasCompletedAwakeningSequence()) {
                // Priority 1: Awakening sequence (first time player reaches 3 deaths)
                pendingRespawnEvents.put(player.getUUID(), RespawnEvent.awakening());
                CosmicCore.LOGGER.info("Queued awakening sequence for {}", player.getName().getString());
            } else {
                // Check for threshold crossing
                int newErosion = reflection.getErosion();
                if (ReflectionConstants.crossedNewThreshold(oldErosion, newErosion)) {
                    int newThreshold = ReflectionConstants.getThresholdIndex(newErosion);
                    if (newThreshold > reflection.getHighestThresholdSeen()) {
                        // Priority 2: Threshold encounter
                        pendingRespawnEvents.put(player.getUUID(), RespawnEvent.threshold(newThreshold));
                        CosmicCore.LOGGER.info("Queued threshold {} encounter for {}",
                                newThreshold, player.getName().getString());
                    }
                } else if (reflection.hasAwakened() && !reflection.hasBargain(BackBargain.INSTANCE.getId())) {
                    // Priority 3: Contextual bargain offer (offer /back if they don't have it)
                    pendingRespawnEvents.put(player.getUUID(), RespawnEvent.contextualBargain(0));
                    CosmicCore.LOGGER.info("Queued contextual /back bargain offer for {}",
                            player.getName().getString());
                }
            }

            // Queue whisper for after respawn (if awakened and no UI event pending)
            if (reflection.hasAwakened() && !pendingRespawnEvents.containsKey(player.getUUID())) {
                pendingDeathWhispers.put(player.getUUID(), deathCause);
            }

            CosmicCore.LOGGER.debug("Player {} died. Deaths: {}, Erosion: {}",
                    player.getName().getString(), reflection.getDeathCount(), reflection.getErosion());
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ReflectionCapability.get(player).ifPresent(reflection -> {
            // Sync bargain states to client after respawn
            syncBargainStates(player, reflection);

            // Check for pending respawn event
            RespawnEvent pendingEvent = pendingRespawnEvents.remove(player.getUUID());
            if (pendingEvent != null) {
                // Delay the UI slightly to ensure player is fully loaded
                player.getServer().execute(() -> {
                    processRespawnEvent(player, reflection, pendingEvent);
                });
                // Clear any whisper since we're showing UI
                pendingDeathWhispers.remove(player.getUUID());
            } else {
                // Send death whisper after respawn (if no UI event)
                String deathCause = pendingDeathWhispers.remove(player.getUUID());
                if (deathCause != null && reflection.hasAwakened()) {
                    // Schedule whisper for next tick
                    player.getServer().execute(() -> {
                        WhisperSystem.triggerEvent(player, WhisperSystem.WhisperEvent.DEATH);
                    });
                }
            }
        });
    }

    /**
     * Process a pending respawn event - opens the appropriate UI.
     */
    private static void processRespawnEvent(ServerPlayer player, IReflection reflection, RespawnEvent event) {
        switch (event.type()) {
            case AWAKENING -> {
                // Awakening sequence: offer Quake Movement bargain
                CosmicCore.LOGGER.info("Triggering awakening sequence for {}", player.getName().getString());
                VoidUIPackets.sendOpenVoidScreen(player, QuakeMovementBargain.INSTANCE.getId());
                reflection.setAwakeningSequenceCompleted(true);
            }
            case THRESHOLD -> {
                // Threshold encounter: show milestone dialogue
                int thresholdIndex = event.data();
                CosmicCore.LOGGER.info("Triggering threshold {} encounter for {}",
                        thresholdIndex, player.getName().getString());
                VoidUIPackets.sendThresholdEncounter(player, thresholdIndex);
                reflection.setHighestThresholdSeen(thresholdIndex);
            }
            case CONTEXTUAL_BARGAIN -> {
                // Contextual bargain offer
                int bargainType = event.data();
                if (bargainType == 0 && !reflection.hasBargain(BackBargain.INSTANCE.getId())) {
                    // Offer /back bargain
                    CosmicCore.LOGGER.info("Triggering contextual /back bargain offer for {}",
                            player.getName().getString());
                    VoidUIPackets.sendOpenVoidScreen(player, BackBargain.INSTANCE.getId());
                }
                // Can add more contextual bargains here (e.g., bargainType == 1 for /home)
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // Tick active bargains
        for (Bargain bargain : BargainRegistry.getActive(player)) {
            bargain.tick(player);
        }

        // Command usage cooldown check
        ReflectionCapability.get(player).ifPresent(reflection -> {
            long now = System.currentTimeMillis();

            // Reset command usage if cooldown expired
            for (String cmd : new String[] { "home", "back" }) {
                long lastUse = reflection.getLastCommandUseTime(cmd);
                if (lastUse > 0 && (now - lastUse) > ReflectionConstants.COMMAND_USAGE_RESET_TIME) {
                    reflection.resetCommandUsage(cmd);
                }
            }
        });

        // Whisper system tick (runs periodically, not every tick)
        if (player.tickCount % 100 == 0) { // Every 5 seconds
            WhisperSystem.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ReflectionCapability.get(player).ifPresent(reflection -> {
            CosmicCore.LOGGER.debug("Player {} logged in. Erosion: {}, Deaths: {}, Awakened: {}",
                    player.getName().getString(),
                    reflection.getErosion(),
                    reflection.getDeathCount(),
                    reflection.hasAwakened());

            // Sync bargain states to client
            syncBargainStates(player, reflection);
        });
    }

    /**
     * Sync all client-side bargain states to a player.
     * Called on login and respawn to ensure client knows about active bargains.
     */
    private static void syncBargainStates(ServerPlayer player, IReflection reflection) {
        // Sync Quake Movement bargain
        boolean hasQuake = reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId());
        CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(hasQuake));

        // Add more bargain syncs here as they're implemented
    }

    /**
     * Categorize death cause for memory tracking.
     */
    private static String categorizeDeathCause(String msgId) {
        if (msgId == null) return "unknown";

        // Fall damage
        if (msgId.contains("fall") || msgId.contains("stalagmite")) return "fall";

        // Fire/lava
        if (msgId.contains("fire") || msgId.contains("lava") || msgId.contains("burn") || msgId.contains("inFire") ||
                msgId.contains("onFire"))
            return "fire";

        // Drowning/suffocation
        if (msgId.contains("drown") || msgId.contains("suffocate") || msgId.contains("inWall")) return "suffocation";

        // Void
        if (msgId.contains("void") || msgId.contains("outOfWorld")) return "void";

        // Freezing
        if (msgId.contains("freeze") || msgId.contains("cold")) return "freeze";

        // Starving
        if (msgId.contains("starve")) return "starve";

        // Combat
        if (msgId.contains("mob") || msgId.contains("player") || msgId.contains("arrow") || msgId.contains("thrown") ||
                msgId.contains("explosion"))
            return "combat";

        // Magic
        if (msgId.contains("magic") || msgId.contains("wither") || msgId.contains("indirectMagic")) return "magic";

        return "other";
    }
}
