package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * The First Bargain: Quake Movement
 *
 * Offered after the player dies a few times and realizes they're immortal.
 * They're running from the realization of what they are.
 *
 * Power: Bunny hopping, air strafing, momentum preservation.
 * Cost: Chose denial - some self-understanding paths locked.
 */
public class QuakeMovementBargain extends Bargain {

    public static final QuakeMovementBargain INSTANCE = new QuakeMovementBargain();
    private static final String BARGAIN_ID = "quake_movement";

    private QuakeMovementBargain() {
        super(
                CosmicCore.id("quake_movement"),
                BargainTier.EARLY,
                0,    // shardCost - FREE (the hook to get players into the system)
                0,    // weight - FREE (doesn't consume soul capacity)
                0     // erosion - FREE (truly no cost)
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
                ReflectionLang.bargainDialogue(BARGAIN_ID, 3));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer(
                        "yes",
                        ReflectionLang.answerText(BARGAIN_ID, "yes"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "yes")).withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "yes", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "yes", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "yes", 0))),
                new BargainAnswer(
                        "refuse",
                        ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse")).withReducedPower());
    }

    @Override
    public boolean isContextuallyRelevant(Player player, BargainContext context) {
        // This bargain is always relevant for the first encounter
        return true;
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        // The quake movement effect is applied via the movement handler
        // This just marks the bargain as accepted
        CosmicCore.LOGGER.info("Player {} accepted Quake Movement bargain with answer: {}",
                player.getName().getString(), answer.id());
    }

    @Override
    public void onDefy(Player player) {
        CosmicCore.LOGGER.info("Player {} defied Quake Movement bargain", player.getName().getString());
        // Movement returns to normal, but they still remember running
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of(
                "quake_legs",
                "Legs elongated, joints bent wrong, always slightly blurred");
    }

    @Override
    public List<Component> getAcceptDialogue(Player player, BargainAnswer answer) {
        return List.of(
                ReflectionLang.bargainAccept(BARGAIN_ID, 0),
                ReflectionLang.bargainAccept(BARGAIN_ID, 1),
                ReflectionLang.bargainAccept(BARGAIN_ID, 2));
    }

    @Override
    public List<Component> getRefuseDialogue(Player player) {
        return List.of(
                ReflectionLang.bargainRefuse(BARGAIN_ID, 0),
                ReflectionLang.bargainRefuse(BARGAIN_ID, 1),
                ReflectionLang.bargainRefuse(BARGAIN_ID, 2));
    }
}
