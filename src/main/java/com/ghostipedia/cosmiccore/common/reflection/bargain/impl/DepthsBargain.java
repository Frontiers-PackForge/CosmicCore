package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class DepthsBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("depths");
    public static final DepthsBargain INSTANCE = new DepthsBargain();
    private static final String BARGAIN_ID = "depths";

    public static final float OXYGEN_CAPACITY_MULTIPLIER = 1.5f;

    private DepthsBargain() {
        super(
                ID,
                BargainTier.EARLY_MID,
                BargainCategory.DEFENSE,
                64,   // shardCost
                15,   // weight
                75    // erosion
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
                ReflectionLang.bargainDialogue(BARGAIN_ID, 4),
                ReflectionLang.bargainDialogue(BARGAIN_ID, 5));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer("embrace",
                        ReflectionLang.answerText(BARGAIN_ID, "embrace"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "embrace"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "embrace", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "embrace", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "embrace", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "embrace", 1))),
                new BargainAnswer("refuse",
                        ReflectionLang.answerText(BARGAIN_ID, "refuse"),
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
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("gills", "depths_visual");
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static float getCapacityMultiplier(Player player) {
        return hasBargain(player) ? OXYGEN_CAPACITY_MULTIPLIER : 1.0f;
    }

    public static boolean shouldInstantKillOnSuffocation(Player player) {
        return hasBargain(player);
    }

    public static void executeInstantSuffocation(ServerPlayer player) {
        player.displayClientMessage(ReflectionLang.bargainSuffocation(BARGAIN_ID), false);
        player.hurt(player.damageSources().drown(), Float.MAX_VALUE);
    }
}
