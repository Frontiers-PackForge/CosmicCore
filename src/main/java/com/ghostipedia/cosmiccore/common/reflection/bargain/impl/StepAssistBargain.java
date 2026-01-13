package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

/**
 * Step Assist Bargain: Auto-step up 1.5 blocks
 *
 * Small obstacles no longer impede you.
 * A minor convenience with a minor cost.
 */
public class StepAssistBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("stride");
    public static final StepAssistBargain INSTANCE = new StepAssistBargain();
    private static final String BARGAIN_ID = "stride";
    private static final UUID MODIFIER_UUID = UUID.fromString("f3b0c1d9-6789-9012-def0-123456789012");

    private StepAssistBargain() {
        super(
                ID,
                BargainTier.EARLY,
                16,   // shardCost - cheap starter
                10,   // weight
                50    // erosion
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
                new BargainAnswer("accept", ReflectionLang.answerText(BARGAIN_ID, "accept"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "accept"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "accept", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "accept", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "accept", 0))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            applyStepAssist(player);
        }
    }

    @Override
    public void onDefy(Player player) {
        removeStepAssist(player);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("tallLegs", "The soul's legs appear slightly elongated");
    }

    public static void applyStepAssist(Player player) {
        var attribute = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
            attribute.addPermanentModifier(new AttributeModifier(
                    MODIFIER_UUID, "Reflection Stride", 1.0, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void removeStepAssist(Player player) {
        var attribute = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_UUID);
        }
    }
}
