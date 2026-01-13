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
 * Armor Bargain: More armor, but slower movement.
 *
 * POWER: +8 armor (equivalent to full iron armor)
 * DRAWBACK: 15% slower movement speed
 *
 * Thematically: Your flesh has calcified, hardened against blows. But that
 * hardness comes with weight. Every step is heavier. You're safer... but
 * slower. A walking fortress that can't flee.
 *
 * This creates tank gameplay - you can stand and fight but can't escape.
 * Good for holding ground, bad for kiting or hit-and-run.
 */
public class ArmorBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("carapace");
    public static final ArmorBargain INSTANCE = new ArmorBargain();
    private static final String BARGAIN_ID = "carapace";
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("e2a9b0c8-5678-8901-cdef-012345678901");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("e2a9b0c8-5678-8901-cdef-012345678902");

    /** Movement speed penalty (0.15 = 15% slower) */
    public static final float SPEED_PENALTY = -0.15f;

    private ArmorBargain() {
        super(
                ID,
                BargainTier.MID,
                64,   // shardCost
                25,   // weight
                100   // erosion
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
                ReflectionLang.bargainDialogue(BARGAIN_ID, 4));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer("survive", ReflectionLang.answerText(BARGAIN_ID, "survive"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "survive"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "survive", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "survive", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "survive", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "survive", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (answer.id().equals("survive")) {
            applyArmorBoost(player);
            applySpeedPenalty(player);
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        removeArmorBoost(player);
        removeSpeedPenalty(player);
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("shell", "The soul's surface appears calcified, plated, moving slowly");
    }

    public static void applyArmorBoost(Player player) {
        var attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(ARMOR_MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    ARMOR_MODIFIER_UUID, "Reflection Carapace", 8.0, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void removeArmorBoost(Player player) {
        var attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(ARMOR_MODIFIER_UUID);
        }
    }

    public static void applySpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_UUID, "Carapace Weight", SPEED_PENALTY, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void removeSpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_MODIFIER_UUID);
        }
    }

    // =========================================================================
    // Static helper methods
    // =========================================================================

    /**
     * Check if a player has the Calcified Flesh bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }
}
