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

/**
 * Depths Bargain: Enhanced lung capacity, but instant death on suffocation.
 *
 * POWER: +50% oxygen capacity (90 seconds -> 135 seconds base air)
 * DRAWBACK: When oxygen reaches 0, you instantly die instead of taking gradual damage.
 *
 * Thematically: Your lungs have been remade by the depths. They can hold more,
 * but they've forgotten how to struggle - when they fail, they fail completely.
 *
 * This replaces the old "infinite water breathing" bargain with something more
 * interesting and balanced - a meaningful power boost with a meaningful risk.
 */
public class DepthsBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("depths");
    public static final DepthsBargain INSTANCE = new DepthsBargain();
    private static final String BARGAIN_ID = "depths";

    /** Multiplier for max oxygen capacity (1.5 = 50% more) */
    public static final float OXYGEN_CAPACITY_MULTIPLIER = 1.5f;

    private DepthsBargain() {
        super(
                ID,
                BargainTier.EARLY_MID,
                64,   // shardCost
                15,   // weight
                75    // erosion
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
                new BargainAnswer("embrace",
                        ReflectionLang.answerText(BARGAIN_ID, "embrace"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "embrace"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "embrace", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "embrace", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "embrace", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "embrace", 1))),
                new BargainAnswer("refuse",
                        ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("gills", "depths_visual");
    }

    // =========================================================================
    // Static helper methods for OxygenLogic integration
    // =========================================================================

    /**
     * Check if a player has the Depths bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Get the oxygen capacity multiplier for a player.
     * Returns 1.0 if they don't have the bargain.
     */
    public static float getCapacityMultiplier(Player player) {
        return hasBargain(player) ? OXYGEN_CAPACITY_MULTIPLIER : 1.0f;
    }

    /**
     * Check if this player should die instantly when oxygen reaches 0.
     * Returns true if they have the bargain (the drawback).
     */
    public static boolean shouldInstantKillOnSuffocation(Player player) {
        return hasBargain(player);
    }

    /**
     * Execute instant death for suffocation (for use in OxygenLogic).
     */
    public static void executeInstantSuffocation(ServerPlayer player) {
        player.displayClientMessage(ReflectionLang.bargainSuffocation(BARGAIN_ID), false);
        // Deal massive damage to ensure death - using drown damage type
        player.hurt(player.damageSources().drown(), Float.MAX_VALUE);
    }
}
