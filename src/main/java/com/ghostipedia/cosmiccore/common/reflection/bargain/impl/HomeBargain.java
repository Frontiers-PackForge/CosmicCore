package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * The /home bargain - teleport to spawn/bed.
 *
 * This is the "poisoned apple" - offered early, seems cheap, but costs escalate
 * with repeated quick use. The Reflection knows you'll become dependent on it.
 *
 * Base cost: 1 erosion (seems cheap!)
 * Quick use penalty: Doubles each time used within 15 minutes (1 -> 2 -> 4 -> 8 -> 16)
 * Ceiling: 16 erosion per use
 * Reset: After 15 minutes of no use, resets to base cost
 */
public class HomeBargain extends Bargain {

    private static final ResourceLocation ID = CosmicCore.id("home");
    public static final HomeBargain INSTANCE = new HomeBargain();
    private static final String BARGAIN_ID = "home";

    private HomeBargain() {
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
        return BargainVisual.of("threads", "Faint silver threads extending outward, always pointing home");
    }

    @Override
    public void tick(Player player) {
        // No passive effects - this is an on-demand ability
    }

    /**
     * Execute the /home teleport.
     * Called when player uses the command (if they have the bargain).
     *
     * @return true if teleport succeeded, false otherwise
     */
    public static boolean executeHome(ServerPlayer player) {
        return ReflectionCapability.get(player).map(reflection -> {
            if (!reflection.hasBargain(ID)) {
                player.displayClientMessage(
                        Component.literal("\u00A7cYou haven't made this bargain."),
                        false);
                return false;
            }

            // Calculate current cost based on usage
            int cost = ReflectionConstants.getCommandCost(reflection, "home");

            // Find home location (bed or world spawn)
            Optional<Vec3> homePos = findHomePosition(player);
            if (homePos.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("\u00A7cYou have no home to return to."),
                        false);
                return false;
            }

            Vec3 home = homePos.get();

            // Apply erosion cost
            reflection.addErosion(cost);
            reflection.recordCommandUse("home");

            // Teleport
            player.teleportTo(home.x, home.y, home.z);

            // Effects
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);

            // Feedback based on cost
            if (cost <= 2) {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*You feel the familiar pull of home.*"),
                        true);
            } else if (cost <= 8) {
                player.displayClientMessage(
                        Component.literal("\u00A77\u00A7o*The path feels... worn. Familiar. Too familiar.*"),
                        true);
            } else {
                player.displayClientMessage(
                        Component.literal(
                                "\u00A77\u00A7o*Home. Again. Always home. Do you even remember the world outside?*"),
                        true);
            }

            // Show cost in chat
            player.displayClientMessage(
                    Component.literal("\u00A78[Erosion +" + cost + "]"),
                    false);

            return true;
        }).orElse(false);
    }

    private static Optional<Vec3> findHomePosition(ServerPlayer player) {
        // Check for bed spawn first
        BlockPos bedPos = player.getRespawnPosition();
        if (bedPos != null) {
            ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());
            if (respawnLevel != null) {
                // Find safe spawn near bed
                Optional<Vec3> bedSpawn = Player.findRespawnPositionAndUseSpawnBlock(
                        respawnLevel, bedPos, player.getRespawnAngle(), true, false);
                if (bedSpawn.isPresent()) {
                    return bedSpawn;
                }
            }
        }

        // Fall back to world spawn
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            BlockPos spawnPos = overworld.getSharedSpawnPos();
            return Optional.of(new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5));
        }

        return Optional.empty();
    }
}
