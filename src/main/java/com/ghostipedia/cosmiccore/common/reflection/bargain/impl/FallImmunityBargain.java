package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class FallImmunityBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("soft_landing");
    public static final FallImmunityBargain INSTANCE = new FallImmunityBargain();
    private static final String BARGAIN_ID = "soft_landing";

    public static final float FALL_DAMAGE_MULTIPLIER = 0.2f;
    public static final float COMBAT_DAMAGE_MULTIPLIER = 1.5f;

    private FallImmunityBargain() {
        super(
                ID,
                BargainTier.MID,
                BargainCategory.MOBILITY,
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
                new BargainAnswer("yes", ReflectionLang.answerText(BARGAIN_ID, "yes"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "yes"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "yes", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "yes", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "yes", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "yes", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public boolean isContextuallyRelevant(Player player, BargainContext context) {
        return context.lastDeathCause().map(cause -> cause.contains("fall")).orElse(false);
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (answer.id().equals("yes")) {
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("floating", "The soul hovers slightly, its form diffuse and yielding");
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static float modifyFallDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * FALL_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    public static float modifyCombatDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * COMBAT_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    public static boolean isFallDamage(DamageSource source) {
        return source.is(DamageTypes.FALL) ||
                source.is(DamageTypes.FLY_INTO_WALL);
    }

    public static boolean isCombatDamage(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) ||
                source.is(DamageTypes.EXPLOSION) ||
                source.is(DamageTypes.PLAYER_EXPLOSION) ||
                source.getEntity() instanceof Player;
    }
}
