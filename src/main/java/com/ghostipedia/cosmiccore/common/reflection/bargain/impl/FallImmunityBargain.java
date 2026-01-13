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
 * Fall Damage Bargain: Reduced fall damage with fly-swatter vulnerability.
 *
 * POWER: 80% fall damage reduction - falls barely hurt you
 * DRAWBACK: 50% increased damage from player attacks and explosions
 *
 * Thematically: Your body has learned to absorb impact, becoming soft and
 * yielding on landing. But that softness makes you more vulnerable to
 * intentional force - a sword, an arrow, an explosion. The ground forgives
 * you, but other players won't.
 *
 * This creates interesting PvP dynamics - the bargain holder is great at
 * exploration but vulnerable in combat.
 */
public class FallImmunityBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("soft_landing");
    public static final FallImmunityBargain INSTANCE = new FallImmunityBargain();
    private static final String BARGAIN_ID = "soft_landing";

    /** Fall damage multiplier (0.2 = 80% reduction) */
    public static final float FALL_DAMAGE_MULTIPLIER = 0.2f;

    /** Combat damage multiplier (1.5 = 50% more damage from players/explosions) */
    public static final float COMBAT_DAMAGE_MULTIPLIER = 1.5f;

    private FallImmunityBargain() {
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

    // =========================================================================
    // Static helper methods for damage integration
    // =========================================================================

    /**
     * Check if a player has the Phantom Weight bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Modify fall damage for a player with this bargain.
     */
    public static float modifyFallDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * FALL_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    /**
     * Modify combat damage (player attacks, explosions) for a player with this bargain.
     */
    public static float modifyCombatDamage(Player player, float originalDamage) {
        if (hasBargain(player)) {
            return originalDamage * COMBAT_DAMAGE_MULTIPLIER;
        }
        return originalDamage;
    }

    /**
     * Check if a damage source is fall-related.
     */
    public static boolean isFallDamage(DamageSource source) {
        return source.is(DamageTypes.FALL) ||
                source.is(DamageTypes.FLY_INTO_WALL);
    }

    /**
     * Check if a damage source is combat-related (player attacks, explosions).
     */
    public static boolean isCombatDamage(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) ||
                source.is(DamageTypes.EXPLOSION) ||
                source.is(DamageTypes.PLAYER_EXPLOSION) ||
                source.getEntity() instanceof Player;
    }
}
