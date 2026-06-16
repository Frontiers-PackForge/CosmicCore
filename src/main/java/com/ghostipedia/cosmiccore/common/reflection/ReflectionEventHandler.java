package com.ghostipedia.cosmiccore.common.reflection;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainRegistry;
import com.ghostipedia.cosmiccore.common.reflection.bargain.impl.*;
import com.ghostipedia.cosmiccore.common.reflection.network.SyncQuakeMovementPacket;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class ReflectionEventHandler {

    private static final Map<UUID, RespawnEvent> pendingRespawnEvents = new HashMap<>();

    private enum RespawnEventType {
        TUTORIAL_BARGAIN,
        THRESHOLD,
        CONTEXTUAL_BARGAIN
    }

    private record RespawnEvent(RespawnEventType type, ResourceLocation bargainId, int data) {

        static RespawnEvent tutorialBargain(ResourceLocation bargainId) {
            return new RespawnEvent(RespawnEventType.TUTORIAL_BARGAIN, bargainId, 0);
        }

        static RespawnEvent threshold(int index) {
            return new RespawnEvent(RespawnEventType.THRESHOLD, null, index);
        }

        static RespawnEvent contextualBargain(ResourceLocation bargainId) {
            return new RespawnEvent(RespawnEventType.CONTEXTUAL_BARGAIN, bargainId, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        BackBargain.recordDeath(player);

        ReflectionCapability.get(player).ifPresent(reflection -> {
            int oldErosion = reflection.getErosion();

            reflection.recordDeath();
            int deathCount = reflection.getDeathCount();

            String deathCause = categorizeDeathCause(event.getSource().getMsgId());
            reflection.rememberEvent("death." + deathCause);
            reflection.rememberEvent("death.total");

            // Tutorial deaths (1-3) offer specific bargains
            ResourceLocation tutorialBargain = getTutorialBargain(deathCount, reflection);
            if (tutorialBargain != null) {
                pendingRespawnEvents.put(player.getUUID(), RespawnEvent.tutorialBargain(tutorialBargain));
                CosmicCore.LOGGER.info("Queued tutorial bargain {} for {} (death #{})",
                        tutorialBargain, player.getName().getString(), deathCount);
            } else {
                // Normal bargain flow after tutorial
                int newErosion = reflection.getErosion();
                if (ReflectionConstants.crossedNewThreshold(oldErosion, newErosion)) {
                    int newThreshold = ReflectionConstants.getThresholdIndex(newErosion);
                    if (newThreshold > reflection.getHighestThresholdSeen()) {
                        pendingRespawnEvents.put(player.getUUID(), RespawnEvent.threshold(newThreshold));
                    }
                }
            }

            CosmicCore.LOGGER.debug("Player {} died. Deaths: {}, Erosion: {}",
                    player.getName().getString(), reflection.getDeathCount(), reflection.getErosion());
        });
    }

    private static ResourceLocation getTutorialBargain(int deathCount, IReflection reflection) {
        return switch (deathCount) {
            case 1 -> {
                if (!reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId())) {
                    yield QuakeMovementBargain.INSTANCE.getId();
                }
                yield null;
            }
            case 2 -> {
                if (!reflection.hasBargain(BackBargain.INSTANCE.getId())) {
                    yield BackBargain.INSTANCE.getId();
                }
                yield null;
            }
            case 3 -> {
                if (!reflection.hasBargain(HomeBargain.INSTANCE.getId())) {
                    yield HomeBargain.INSTANCE.getId();
                }
                yield null;
            }
            default -> null;
        };
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ReflectionCapability.get(player).ifPresent(reflection -> {
            reapplyBargainEffects(player, reflection);
            syncBargainStates(player, reflection);

            RespawnEvent pendingEvent = pendingRespawnEvents.remove(player.getUUID());
            if (pendingEvent != null) {
                player.getServer().execute(() -> processRespawnEvent(player, reflection, pendingEvent));
            }
        });
    }

    private static void processRespawnEvent(ServerPlayer player, IReflection reflection, RespawnEvent event) {
        switch (event.type()) {
            case TUTORIAL_BARGAIN -> {
                CosmicCore.LOGGER.info("Offering tutorial bargain {} to {}",
                        event.bargainId(), player.getName().getString());
                VoidUIPackets.sendOpenVoidScreen(player, event.bargainId());
            }
            case THRESHOLD -> {
                int thresholdIndex = event.data();
                CosmicCore.LOGGER.info("Triggering threshold {} encounter for {}",
                        thresholdIndex, player.getName().getString());
                VoidUIPackets.sendThresholdEncounter(player, thresholdIndex);
                reflection.setHighestThresholdSeen(thresholdIndex);
            }
            case CONTEXTUAL_BARGAIN -> {
                if (event.bargainId() != null && !reflection.hasBargain(event.bargainId())) {
                    CosmicCore.LOGGER.info("Offering contextual bargain {} to {}",
                            event.bargainId(), player.getName().getString());
                    VoidUIPackets.sendOpenVoidScreen(player, event.bargainId());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        for (Bargain bargain : BargainRegistry.getActive(player)) {
            bargain.tick(player);
        }

        ReflectionCapability.get(player).ifPresent(reflection -> {
            long now = System.currentTimeMillis();

            for (String cmd : new String[] { "home", "back" }) {
                long lastUse = reflection.getLastCommandUseTime(cmd);
                if (lastUse > 0 && (now - lastUse) > ReflectionConstants.COMMAND_USAGE_RESET_TIME) {
                    reflection.resetCommandUsage(cmd);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ReflectionCapability.get(player).ifPresent(reflection -> {
            if (!reflection.hasAwakened()) {
                reflection.setAwakened(true);
            }

            CosmicCore.LOGGER.debug("Player {} logged in. Erosion: {}, Deaths: {}",
                    player.getName().getString(),
                    reflection.getErosion(),
                    reflection.getDeathCount());

            reapplyBargainEffects(player, reflection);
            syncBargainStates(player, reflection);
        });
    }

    private static void reapplyBargainEffects(ServerPlayer player, IReflection reflection) {
        if (reflection.hasBargain(SwiftnessBargain.ID)) {
            SwiftnessBargain.applySpeedBoost(player);
        }
        if (reflection.hasBargain(StepAssistBargain.ID)) {
            StepAssistBargain.applyStepAssist(player);
        }
        if (reflection.hasBargain(HealthBargain.ID)) {
            HealthBargain.applyHealthBoost(player);
        }
        if (reflection.hasBargain(StrengthBargain.ID)) {
            StrengthBargain.applyStrengthBoost(player);
        }
        if (reflection.hasBargain(ReachBargain.ID)) {
            ReachBargain.applyReachBoost(player);
        }
        if (reflection.hasBargain(ArmorBargain.ID)) {
            ArmorBargain.applyArmorBoost(player);
            ArmorBargain.applySpeedPenalty(player);
        }
    }

    private static void syncBargainStates(ServerPlayer player, IReflection reflection) {
        boolean hasQuake = reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId());
        CCoreNetwork.sendToPlayer(player, new SyncQuakeMovementPacket(hasQuake));
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float damage = event.getAmount();

        // FallImmunityBargain: 80% fall damage reduction
        if (FallImmunityBargain.isFallDamage(event.getSource())) {
            damage = FallImmunityBargain.modifyFallDamage(player, damage);
        }

        // FallImmunityBargain: 50% more damage from combat
        if (FallImmunityBargain.isCombatDamage(event.getSource())) {
            damage = FallImmunityBargain.modifyCombatDamage(player, damage);
        }

        // StrengthBargain: 25% more damage from mobs
        if (StrengthBargain.isMobDamage(event.getSource())) {
            damage = StrengthBargain.modifyMobDamage(player, damage);
        }

        // FireImmunityBargain: 75% less fire damage, 2x freeze damage
        if (FireImmunityBargain.isFireDamage(event.getSource())) {
            damage = FireImmunityBargain.modifyFireDamage(player, damage);
        }
        if (FireImmunityBargain.isColdDamage(event.getSource())) {
            damage = FireImmunityBargain.modifyColdDamage(player, damage);
        }

        event.setAmount(damage);
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float healing = event.getAmount();

        // HealthBargain: 50% less healing from potions
        healing = HealthBargain.modifyHealing(player, healing);

        // HungerBargain: Block natural regeneration (food-based healing)
        // Natural regen gives 1 HP when food >= 18 and saturation > 0
        // We detect this by checking if it's a small heal amount and player is well-fed
        if (HungerBargain.shouldBlockNaturalRegen(player)) {
            if (healing <= 1.0f && player.getFoodData().getFoodLevel() >= 18) {
                event.setCanceled(true);
                return;
            }
        }

        event.setAmount(healing);
    }

    private static String categorizeDeathCause(String msgId) {
        if (msgId == null) return "unknown";

        if (msgId.contains("fall") || msgId.contains("stalagmite")) return "fall";
        if (msgId.contains("fire") || msgId.contains("lava") || msgId.contains("burn") ||
                msgId.contains("inFire") || msgId.contains("onFire"))
            return "fire";
        if (msgId.contains("drown") || msgId.contains("suffocate") || msgId.contains("inWall")) return "suffocation";
        if (msgId.contains("void") || msgId.contains("outOfWorld")) return "void";
        if (msgId.contains("freeze") || msgId.contains("cold")) return "freeze";
        if (msgId.contains("starve")) return "starve";
        if (msgId.contains("mob") || msgId.contains("player") || msgId.contains("arrow") ||
                msgId.contains("thrown") || msgId.contains("explosion"))
            return "combat";
        if (msgId.contains("magic") || msgId.contains("wither") || msgId.contains("indirectMagic")) return "magic";

        return "other";
    }
}
