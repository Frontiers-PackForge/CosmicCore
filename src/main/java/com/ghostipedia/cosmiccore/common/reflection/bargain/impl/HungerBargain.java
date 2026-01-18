package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.List;

/**
 * Hunger Bargain: Reduced hunger drain, but food doesn't restore health.
 *
 * POWER: Hunger depletes 75% slower - you rarely need to eat
 * DRAWBACK: Food no longer triggers natural regeneration - you can't heal by eating
 *
 * Thematically: Your body has forgotten hunger... but also forgotten how to draw
 * life from food. You can eat, but it only fills the stomach - it doesn't nourish.
 * You'll need potions, golden apples, or other means to heal.
 *
 * This keeps hunger as a minor concern while creating an interesting healing
 * limitation that changes combat strategy.
 */
public class HungerBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("satiated");
    public static final HungerBargain INSTANCE = new HungerBargain();
    private static final String BARGAIN_ID = "satiated";

    /** How often to restore hunger (every N ticks, prevents hunger drain) */
    public static final int HUNGER_RESTORE_INTERVAL = 80; // Every 4 seconds

    private HungerBargain() {
        super(
                ID,
                BargainTier.MID,
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
        // Periodically restore some hunger to simulate 75% slower drain
        // Instead of keeping it full, we restore 1 hunger every 4 seconds
        // This means hunger still exists but drains very slowly
        if (player.level().getGameTime() % HUNGER_RESTORE_INTERVAL == 0) {
            FoodData food = player.getFoodData();
            if (food.getFoodLevel() < 20) {
                food.setFoodLevel(Math.min(20, food.getFoodLevel() + 1));
            }
            // Keep saturation moderate (but not max - allows some natural drain)
            if (food.getSaturationLevel() < 2.0f) {
                food.setSaturation(2.0f);
            }
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("hollow",
                "The soul's midsection is translucent, food passes through without nourishing");
    }

    // =========================================================================
    // Static helper methods for healing integration
    // =========================================================================

    /**
     * Check if a player has the Hollow Satiation bargain active.
     */
    public static boolean hasBargain(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(ID))
                .orElse(false);
    }

    /**
     * Check if natural regeneration should be blocked for this player.
     * Returns true if player has bargain and should NOT heal from food.
     */
    public static boolean shouldBlockNaturalRegen(Player player) {
        return hasBargain(player);
    }
}
