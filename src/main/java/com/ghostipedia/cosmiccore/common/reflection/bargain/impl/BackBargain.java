package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The /back bargain - return to your last death location.
 *
 * Even more of a trap than /home. You WILL die. You WILL want your stuff back.
 * And every time you use it, the cost grows.
 *
 * Base cost: 2 erosion
 * Quick use penalty: Doubles each time used within 15 minutes (2 -> 4 -> 8 -> 16 -> 32)
 * Ceiling: 32 erosion per use
 * Reset: After 15 minutes of no use, resets to base cost
 */
public class BackBargain extends Bargain {

    private static final ResourceLocation ID = CosmicCore.id("back");
    public static final BackBargain INSTANCE = new BackBargain();
    private static final String BARGAIN_ID = "back";

    // Track last death positions per player
    private static final Map<UUID, DeathLocation> lastDeathLocations = new HashMap<>();

    private BackBargain() {
        super(
                ID,
                BargainTier.EARLY,
                0,    // shardCost - FREE (poisoned apple - per-use costs add up)
                0,    // weight - FREE (doesn't consume soul capacity)
                100   // erosion - significant upfront cost + per-use escalation
        );
    }

    @Override
    public Component getName() {
        return ReflectionLang.bargainName(BARGAIN_ID);
    }

    @Override
    public Component getDescription() {
        return ReflectionLang.bargainDescription(BARGAIN_ID);
    }

    @Override
    public List<Component> getOfferDialogue(Player player) {
        return List.of(
                ReflectionLang.bargainDialogue(BARGAIN_ID, 0),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 1),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 2));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer("accept", ReflectionLang.answerText(BARGAIN_ID, "accept"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "accept"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "accept", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "accept", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "accept", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "accept", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (answer.id().equals("accept")) {
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("echo", "A faint afterimage follows you, showing where death last found you");
    }

    @Override
    public void tick(Player player) {
        // No passive effects
    }

    /**
     * Record a death location for a player.
     * Called from death event handler.
     */
    public static void recordDeath(ServerPlayer player) {
        lastDeathLocations.put(player.getUUID(), new DeathLocation(
                player.level().dimension(),
                player.position()));
    }

    /**
     * Execute the /back teleport.
     * Called when player uses the command (if they have the bargain).
     *
     * @return true if teleport succeeded, false otherwise
     */
    public static boolean executeBack(ServerPlayer player) {
        return ReflectionCapability.get(player).map(reflection -> {
            if (!reflection.hasBargain(ID)) {
                player.displayClientMessage(
                        Component.literal("\u00A7cYou haven't made this bargain."),
                        false);
                return false;
            }

            // Check for death location
            DeathLocation deathLoc = lastDeathLocations.get(player.getUUID());
            if (deathLoc == null) {
                player.displayClientMessage(
                        Component.literal("\u00A7cYou have no death to return to."),
                        false);
                return false;
            }

            // Get the target dimension
            ServerLevel targetLevel = player.server.getLevel(deathLoc.dimension);
            if (targetLevel == null) {
                player.displayClientMessage(
                        Component.literal("\u00A7cThat place no longer exists."),
                        false);
                return false;
            }

            // Calculate current cost based on usage
            int cost = ReflectionConstants.getCommandCost(reflection, "back");

            // Apply erosion cost
            reflection.addErosion(cost);
            reflection.recordCommandUse("back");

            // Teleport (handling cross-dimension)
            Vec3 pos = deathLoc.position;
            if (player.level().dimension() != deathLoc.dimension) {
                player.teleportTo(targetLevel, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
            } else {
                player.teleportTo(pos.x, pos.y, pos.z);
            }

            // Effects
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.5f);

            // Feedback based on cost - gets increasingly ominous
            if (cost <= 4) {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*You return to where you fell.*"),
                        true);
            } else if (cost <= 12) {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*Again you return. The ground remembers your blood.*"),
                        true);
            } else if (cost <= 24) {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*How many times have you died here? Does it matter anymore?*"),
                        true);
            } else {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*Death. Return. Death. Return. You've made this a ritual.*"),
                        true);
            }

            // Show cost in chat
            player.displayClientMessage(
                    Component.literal("\u00A78[Erosion +" + cost + "]"),
                    false);

            // Clear the death location after use (can't spam back to same spot)
            lastDeathLocations.remove(player.getUUID());

            return true;
        }).orElse(false);
    }

    /**
     * Check if a player has a death location recorded.
     */
    public static boolean hasDeathLocation(UUID playerId) {
        return lastDeathLocations.containsKey(playerId);
    }

    /**
     * Clear death location (on logout, etc.)
     */
    public static void clearDeathLocation(UUID playerId) {
        lastDeathLocations.remove(playerId);
    }

    private record DeathLocation(
                                 ResourceKey<Level> dimension,
                                 Vec3 position) {}
}
