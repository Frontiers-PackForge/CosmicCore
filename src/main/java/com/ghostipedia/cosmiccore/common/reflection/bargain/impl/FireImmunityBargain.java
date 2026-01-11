package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Cinder Bargain: Fire resistance with cold vulnerability.
 *
 * POWER: 75% fire/lava damage reduction (not immunity!)
 * DRAWBACK: 2x damage from freezing/cold sources
 *
 * Thematically: Your soul burns with borrowed heat. Fire recognizes you as kin
 * and holds back its fury - but cold now bites twice as deep. You've traded
 * one element's wrath for another's.
 */
public class FireImmunityBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("cinder");
    public static final FireImmunityBargain INSTANCE = new FireImmunityBargain();
    private static final String BARGAIN_ID = "cinder";

    /** Fire damage multiplier (0.25 = 75% reduction) */
    public static final float FIRE_DAMAGE_MULTIPLIER = 0.25f;

    /** Cold damage multiplier (2.0 = double damage) */
    public static final float COLD_DAMAGE_MULTIPLIER = 2.0f;

    private FireImmunityBargain() {
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
        // Extinguish fire visual faster (still take reduced damage, but less annoying burning animation)
        if (player.getRemainingFireTicks() > 20) {
            player.setRemainingFireTicks(20);
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("ember", "Faint embers drift from the soul's form, it radiates warmth but shivers");
    }

    // =========================================================================
    // Static helper methods for damage integration
    // =========================================================================

    /**
     * Check if a player has the Cinder bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Modify fire damage for a player with this bargain.
     * Returns the modified damage amount.
     */
    public static float modifyFireDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * FIRE_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    /**
     * Modify cold/freeze damage for a player with this bargain.
     * Returns the modified damage amount.
     */
    public static float modifyColdDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * COLD_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    /**
     * Check if a damage source is fire-related.
     */
    public static boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) ||
                source.is(DamageTypes.ON_FIRE) ||
                source.is(DamageTypes.LAVA) ||
                source.is(DamageTypes.HOT_FLOOR);
    }

    /**
     * Check if a damage source is cold-related.
     */
    public static boolean isColdDamage(DamageSource source) {
        return source.is(DamageTypes.FREEZE);
    }
}
