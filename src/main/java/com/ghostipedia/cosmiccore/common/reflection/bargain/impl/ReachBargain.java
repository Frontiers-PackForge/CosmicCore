package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public class ReachBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("reach");
    public static final ReachBargain INSTANCE = new ReachBargain();
    private static final String BARGAIN_ID = "reach";
    private static final ResourceLocation REACH_MODIFIER_ID = CosmicCore.id("reflection_block_reach");
    private static final ResourceLocation ATTACK_MODIFIER_ID = CosmicCore.id("reflection_entity_reach");

    private ReachBargain() {
        super(
                ID,
                BargainTier.MID,
                BargainCategory.OFFENSE,
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
                ReflectionLang.bargainDialogue(BARGAIN_ID, 3));
    }

    @Override
    public Component getQuestion() {
        return ReflectionLang.bargainQuestion(BARGAIN_ID);
    }

    @Override
    public List<BargainAnswer> getAnswers() {
        return List.of(
                new BargainAnswer("further", ReflectionLang.answerText(BARGAIN_ID, "further"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "further"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "further", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "further", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "further", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "further", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            applyReachBoost(player);
        }
    }

    @Override
    public void onDefy(Player player) {
        removeReachBoost(player);
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("longArms", "The soul's arms extend slightly too far, joints in wrong places");
    }

    public static void applyReachBoost(Player player) {
        var reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_ID);
            reachAttr.addPermanentModifier(new AttributeModifier(
                    REACH_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
        }

        var attackAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_MODIFIER_ID);
            attackAttr.addPermanentModifier(new AttributeModifier(
                    ATTACK_MODIFIER_ID, 2.0, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static void removeReachBoost(Player player) {
        var reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_ID);
        }

        var attackAttr = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_MODIFIER_ID);
        }
    }
}
