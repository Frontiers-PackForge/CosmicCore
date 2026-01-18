package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
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
 * Swiftness Bargain: +20% movement speed
 *
 * A minor stat boost for those who want to move faster.
 * Less dramatic than Quake Movement, but always active.
 */
public class SwiftnessBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("swiftness");
    public static final SwiftnessBargain INSTANCE = new SwiftnessBargain();
    private static final String BARGAIN_ID = "swiftness";
    private static final UUID MODIFIER_UUID = UUID.fromString("c0e7d8f6-3456-6789-abcd-ef0123456789");

    private SwiftnessBargain() {
        super(
                ID,
                BargainTier.EARLY,
                16,   // shardCost - cheap starter
                10,   // weight
                50    // erosion
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
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "accept", 0))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            applySpeedBoost(player);
        }
    }

    @Override
    public void onDefy(Player player) {
        removeSpeedBoost(player);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("blur", "The soul's legs appear slightly blurred, always in motion");
    }

    public static void applySpeedBoost(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    MODIFIER_UUID, "Reflection Swiftness", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    public static void removeSpeedBoost(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
        }
    }
}
