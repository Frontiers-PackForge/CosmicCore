package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class NightVisionBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("darksight");
    public static final NightVisionBargain INSTANCE = new NightVisionBargain();
    private static final String BARGAIN_ID = "darksight";

    public static final int BRIGHT_LIGHT_THRESHOLD = 14;
    public static final float BRIGHT_LIGHT_DAMAGE = 0.5f;
    public static final int LIGHT_DAMAGE_INTERVAL = 20;

    private NightVisionBargain() {
        super(
                ID,
                BargainTier.EARLY,
                BargainCategory.UTILITY,
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
        if (!player.hasEffect(MobEffects.NIGHT_VISION) ||
                player.getEffect(MobEffects.NIGHT_VISION).getDuration() < 400) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, true, false, false));
        }

        BlockPos pos = player.blockPosition();
        int skyLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);

        boolean inBrightSunlight = skyLight >= BRIGHT_LIGHT_THRESHOLD &&
                player.level().isDay() &&
                player.level().canSeeSky(pos);

        if (inBrightSunlight) {
            if (!player.hasEffect(MobEffects.BLINDNESS) ||
                    player.getEffect(MobEffects.BLINDNESS).getDuration() < 40) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, true, false, true));
            }

            if (player.level().getGameTime() % LIGHT_DAMAGE_INTERVAL == 0) {
                player.hurt(player.damageSources().magic(), BRIGHT_LIGHT_DAMAGE);

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

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static boolean isBeingHurtByLight(Player player) {
        if (!hasBargain(player)) return false;

        BlockPos pos = player.blockPosition();
        int skyLight = player.level().getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);

        return skyLight >= BRIGHT_LIGHT_THRESHOLD &&
                player.level().isDay() &&
                player.level().canSeeSky(pos);
    }
}
