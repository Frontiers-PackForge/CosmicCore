package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Night Vision Bargain: See in darkness, but bright light hurts.
 *
 * POWER: Permanent night vision effect
 * DRAWBACK: Bright light (sky light level 15) causes blindness and minor damage
 *
 * Thematically: Your eyes have been remade for the dark. They see perfectly
 * in shadow... but the sun is now your enemy. You've traded one limitation
 * for another - now you must hide from daylight or suffer.
 *
 * This creates interesting gameplay - underground exploration is trivial,
 * but surface travel during day requires planning or infrastructure.
 */
public class NightVisionBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("darksight");
    public static final NightVisionBargain INSTANCE = new NightVisionBargain();
    private static final String BARGAIN_ID = "darksight";

    /** Light level at which blindness starts (bright sunlight) */
    public static final int BRIGHT_LIGHT_THRESHOLD = 14;

    /** Damage dealt per tick when in bright light (0.5 = 1 heart per second at 20 ticks/sec) */
    public static final float BRIGHT_LIGHT_DAMAGE = 0.5f;

    /** How often to apply light damage (every N ticks) */
    public static final int LIGHT_DAMAGE_INTERVAL = 20;

    private NightVisionBargain() {
        super(
                ID,
                BargainTier.EARLY,
                256,  // shardCost - premium
                25,   // weight
                150   // erosion
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
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.BLINDNESS);
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public void tick(Player player) {
        // Always apply night vision
        if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 400) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, true, false, false));
        }

        // Check light level - apply blindness and damage in bright light
        BlockPos pos = player.blockPosition();
        int skyLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
        int blockLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);
        int effectiveLight = Math.max(skyLight, blockLight);

        // Only sky light at max during day causes problems (not torches)
        boolean inBrightSunlight = skyLight >= BRIGHT_LIGHT_THRESHOLD &&
                player.level().isDay() &&
                player.level().canSeeSky(pos);

        if (inBrightSunlight) {
            // Apply blindness
            if (!player.hasEffect(MobEffects.BLINDNESS) ||
                    player.getEffect(MobEffects.BLINDNESS).getDuration() < 40) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, false, true));
            }

            // Apply periodic damage
            if (player.level().getGameTime() % LIGHT_DAMAGE_INTERVAL == 0) {
                player.hurt(player.damageSources().magic(), BRIGHT_LIGHT_DAMAGE);

                // Occasional warning
                if (player.level().getGameTime() % 100 == 0) {
                    player.displayClientMessage(
                            Component.literal("\u00A7c\u00A7o*The light burns! Seek shadow!*"),
                            true);
                }
            }
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("voidEyes",
                "The soul's eyes are empty voids that somehow still see, flinching from light");
    }

    // =========================================================================
    // Static helper methods
    // =========================================================================

    /**
     * Check if a player has the Void Sight bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Check if a player is currently being hurt by light.
     */
    public static boolean isBeingHurtByLight(Player player) {
        if (!hasBargain(player)) return false;

        BlockPos pos = player.blockPosition();
        int skyLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);

        return skyLight >= BRIGHT_LIGHT_THRESHOLD &&
                player.level().isDay() &&
                player.level().canSeeSky(pos);
    }
}
