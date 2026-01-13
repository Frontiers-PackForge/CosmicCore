package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

/**
 * Flight Bargain: Creative-style flight, but slower when grounded.
 *
 * POWER: True flight like creative mode
 * DRAWBACK: 30% slower movement speed when not flying
 *
 * Thematically: You've abandoned your connection to the ground. Gravity no longer
 * binds you... but walking has become foreign. Your legs have forgotten their
 * purpose. The ground feels wrong beneath feet that were made to soar.
 *
 * This creates interesting gameplay - you're incredibly mobile in the air,
 * but if you're grounded (out of stamina, in a no-fly zone, etc.) you're
 * sluggish and vulnerable.
 */
public class FlightBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("ascension");
    public static final FlightBargain INSTANCE = new FlightBargain();
    private static final String BARGAIN_ID = "ascension";
    private static final UUID GROUND_SLOW_UUID = UUID.fromString("f0a1b2c3-4567-89ab-cdef-012345678901");

    /** Movement speed reduction when grounded (0.3 = 30% slower) */
    public static final float GROUND_SPEED_PENALTY = -0.3f;

    private FlightBargain() {
        super(
                ID,
                BargainTier.EXTREME,
                1000,  // shardCost - VERY expensive, late-game
                50,    // weight - takes up half your soul capacity
                1000   // erosion - massive transformation
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
                new BargainAnswer("ready", ReflectionLang.answerText(BARGAIN_ID, "ready"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "ready"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "ready", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "ready", 1),
                                        ReflectionLang.answerPower(BARGAIN_ID, "ready", 2),
                                        ReflectionLang.answerPower(BARGAIN_ID, "ready", 3)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "ready", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "ready", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        removeGroundPenalty(player);
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public void tick(Player player) {
        // Ensure flight stays enabled
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        // Apply/remove ground penalty based on flying state
        if (player.getAbilities().flying) {
            // Flying - remove ground penalty
            removeGroundPenalty(player);
        } else {
            // Grounded - apply speed penalty
            applyGroundPenalty(player);
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("weightless", "The soul floats high, tethers to earth severed, legs atrophied");
    }

    // =========================================================================
    // Ground penalty methods
    // =========================================================================

    private static void applyGroundPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null && attribute.getModifier(GROUND_SLOW_UUID) == null) {
            attribute.addTransientModifier(new AttributeModifier(
                    GROUND_SLOW_UUID, "Ascension Ground Penalty", GROUND_SPEED_PENALTY,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static void removeGroundPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(GROUND_SLOW_UUID);
        }
    }

    // =========================================================================
    // Static helper methods
    // =========================================================================

    /**
     * Check if a player has the Ascension bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Check if a player is currently suffering the ground penalty.
     */
    public static boolean hasGroundPenalty(Player player) {
        if (!hasBargain(player)) return false;
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        return attribute != null && attribute.getModifier(GROUND_SLOW_UUID) != null;
    }
}
