package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class ArmorBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("carapace");
    public static final ArmorBargain INSTANCE = new ArmorBargain();
    private static final String BARGAIN_ID = "carapace";
    private static final ResourceLocation ARMOR_MODIFIER_ID = CosmicCore.id("reflection_armor");
    private static final ResourceLocation SPEED_MODIFIER_ID = CosmicCore.id("reflection_speed_penalty");

    public static final float SPEED_PENALTY = -0.15f;

    private ArmorBargain() {
        super(
                ID,
                BargainTier.MID,
                BargainCategory.DEFENSE,
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
            attribute.removeModifier(ARMOR_MODIFIER_ID);
            attribute.addPermanentModifier(new AttributeModifier(
                    ARMOR_MODIFIER_ID, 8.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void removeArmorBoost(Player player) {
        var attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute != null) {
            attribute.removeModifier(ARMOR_MODIFIER_ID);
        }
    }

    public static void applySpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_MODIFIER_ID);
            attribute.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID, SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void removeSpeedPenalty(Player player) {
        var attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }
}
