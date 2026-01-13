package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Void Resistance Bargain: Survive falling into the void, but each save costs more.
 *
 * POWER: Teleport back to surface when falling into the void
 * DRAWBACK: Each void save costs escalating erosion (10 -> 20 -> 40 -> 80...)
 *
 * Thematically: The void remembers each time you escape. Each rescue costs more
 * of your soul. Eventually, the price becomes unbearable - and you must choose
 * whether to pay it or accept oblivion.
 *
 * This creates tension around void exploration - you're safe, but not free.
 * The escalating cost means reckless void-diving has consequences.
 */
public class VoidResistanceBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("void_anchor");
    public static final VoidResistanceBargain INSTANCE = new VoidResistanceBargain();
    private static final String BARGAIN_ID = "void_anchor";

    /** Base erosion cost for first void save */
    public static final int BASE_VOID_SAVE_COST = 10;

    /** Maximum cost cap (prevents infinite escalation) */
    public static final int MAX_VOID_SAVE_COST = 160;

    /** Track void save count per player for escalating costs */
    private static final Map<UUID, Integer> voidSaveCount = new ConcurrentHashMap<>();

    private VoidResistanceBargain() {
        super(
                ID,
                BargainTier.LATE,
                0,    // shardCost - FREE in shards, but massive commitment
                75,   // weight - takes up most of your soul capacity
                500   // erosion - huge transformation
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
                ReflectionLang.bargainDialogue(BARGAIN_ID, 2),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 3),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 4),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 5));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer("anchor", ReflectionLang.answerText(BARGAIN_ID, "anchor"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "anchor"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "anchor", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "anchor", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "anchor", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "anchor", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            // Reset save count on fresh bargain acceptance
            voidSaveCount.remove(player.getUUID());
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        voidSaveCount.remove(player.getUUID());
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public void tick(Player player) {
        // Check if player is in the void
        if (player.getY() < -128 && !player.isCreative() && !player.isSpectator()) {
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            // Calculate current cost
            int saveCount = voidSaveCount.getOrDefault(player.getUUID(), 0);
            int cost = Math.min(MAX_VOID_SAVE_COST, BASE_VOID_SAVE_COST * (1 << saveCount));

            // Teleport back to surface
            double x = player.getX();
            double z = player.getZ();
            double y = player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    (int) x, (int) z) + 1;

            player.teleportTo(x, y, z);

            // Apply erosion cost
            ReflectionCapability.get(player).ifPresent(reflection -> {
                reflection.addErosion(cost);
            });

            // Increment save count for next time
            voidSaveCount.put(player.getUUID(), saveCount + 1);

            // Feedback - gets more ominous as cost increases
            if (cost <= 20) {
                player.displayClientMessage(
                        Component.literal(
                                "\u00A75\u00A7o*The void rejects you. Not yet, it whispers. [Erosion +" + cost + "]*"),
                        true);
            } else if (cost <= 80) {
                player.displayClientMessage(
                        Component.literal("\u00A75\u00A7o*The anchor strains. The void's grip tightens. [Erosion +" +
                                cost + "]*"),
                        true);
            } else {
                player.displayClientMessage(
                        Component.literal(
                                "\u00A74\u00A7o*The price is almost too high. The void is patient. It will have you eventually. [Erosion +" +
                                        cost + "]*"),
                        true);
            }
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("anchored",
                "A dark chain extends from the soul downward, links multiplying with each save");
    }

    // =========================================================================
    // Static helper methods
    // =========================================================================

    /**
     * Get the current void save cost for a player.
     */
    public static int getCurrentCost(Player player) {
        int saveCount = voidSaveCount.getOrDefault(player.getUUID(), 0);
        return Math.min(MAX_VOID_SAVE_COST, BASE_VOID_SAVE_COST * (1 << saveCount));
    }

    /**
     * Get the number of times a player has been saved from the void.
     */
    public static int getSaveCount(Player player) {
        return voidSaveCount.getOrDefault(player.getUUID(), 0);
    }

    /**
     * Clear save count for a player (on logout, etc.).
     */
    public static void clearSaveCount(UUID playerId) {
        voidSaveCount.remove(playerId);
    }
}
