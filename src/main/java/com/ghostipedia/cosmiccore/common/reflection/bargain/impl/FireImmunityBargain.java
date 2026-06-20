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

public class FireImmunityBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("cinder");
    public static final FireImmunityBargain INSTANCE = new FireImmunityBargain();
    private static final String BARGAIN_ID = "cinder";

    public static final float FIRE_DAMAGE_MULTIPLIER = 0.25f;
    public static final float COLD_DAMAGE_MULTIPLIER = 2.0f;

    private FireImmunityBargain() {
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
                new BargainAnswer("burn", ReflectionLang.answerText(BARGAIN_ID, "burn"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "burn"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "burn", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "burn", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "burn", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "burn", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public boolean isContextuallyRelevant(Player player, BargainContext context) {
        return context.isBurning();
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
    public void tick(Player player) {
        if (player.getRemainingFireTicks() > 20) {
            player.setRemainingFireTicks(20);
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("ember", "Faint embers drift from the soul's form, it radiates warmth but shivers");
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static float modifyFireDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * FIRE_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    public static float modifyColdDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * COLD_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    public static boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) ||
                source.is(DamageTypes.ON_FIRE) ||
                source.is(DamageTypes.LAVA) ||
                source.is(DamageTypes.HOT_FLOOR);
    }

    public static boolean isColdDamage(DamageSource source) {
        return source.is(DamageTypes.FREEZE);
    }
}
