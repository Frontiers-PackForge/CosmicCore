package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

public class StrengthBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("violence");
    public static final StrengthBargain INSTANCE = new StrengthBargain();
    private static final String BARGAIN_ID = "violence";
    private static final UUID MODIFIER_UUID = UUID.fromString("b9d6c7e5-2345-5678-9abc-def012345678");

    public static final float MOB_DAMAGE_MULTIPLIER = 1.25f;

    private StrengthBargain() {
        super(
                ID,
                BargainTier.EARLY_MID,
                BargainCategory.OFFENSE,
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
        if (!answer.id().equals("refuse")) {
            applyStrengthBoost(player);
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        removeStrengthBoost(player);
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("claws", "The soul's hands end in dark, sharp points, surrounded by hostile energy");
    }

    public static void applyStrengthBoost(Player player) {
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    MODIFIER_UUID, "Reflection Violence", 4.0, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void removeStrengthBoost(Player player) {
        var attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
        }
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static float modifyMobDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * MOB_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    public static boolean isMobDamage(DamageSource source) {
        return source.getEntity() instanceof Monster;
    }
}
