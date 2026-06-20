package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.List;

public class HungerBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("satiated");
    public static final HungerBargain INSTANCE = new HungerBargain();
    private static final String BARGAIN_ID = "satiated";

    public static final float EXHAUSTION_REDUCTION = 0.75f;

    private HungerBargain() {
        super(
                ID,
                BargainTier.MID,
                BargainCategory.SUSTENANCE,
                256,  // shardCost - premium
                50,   // weight - expensive commitment
                250   // erosion
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
                new BargainAnswer("empty", ReflectionLang.answerText(BARGAIN_ID, "empty"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "empty"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "empty", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "empty", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "empty", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "empty", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public boolean isContextuallyRelevant(Player player, BargainContext context) {
        return context.isHungry();
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (answer.id().equals("empty")) {
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public void tick(Player player) {
        FoodData food = player.getFoodData();
        float exhaustion = food.getExhaustionLevel();
        if (exhaustion > 0) {
            food.setExhaustion(exhaustion * (1.0f - EXHAUSTION_REDUCTION));
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("hollow",
                "The soul's midsection is translucent, food passes through without nourishing");
    }

    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    public static boolean shouldBlockNaturalRegen(Player player) {
        return hasBargain(player);
    }
}
