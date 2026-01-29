package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionLang;
import com.ghostipedia.cosmiccore.common.reflection.bargain.Bargain;
import com.ghostipedia.cosmiccore.common.reflection.bargain.BargainCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class ReachBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("reach");
    public static final ReachBargain INSTANCE = new ReachBargain();
    private static final String BARGAIN_ID = "reach";
    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("d1f8e907-4567-7890-bcde-f01234567890");
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("d1f8e907-4567-7890-bcde-f01234567891");

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
        var reachAttr = player.getAttribute(ForgeMod.BLOCK_REACH.get());
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_UUID);
            reachAttr.addPermanentModifier(new AttributeModifier(
                    REACH_MODIFIER_UUID, "Reflection Reach", 2.0, AttributeModifier.Operation.ADDITION));
        }

        var attackAttr = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_MODIFIER_UUID);
            attackAttr.addPermanentModifier(new AttributeModifier(
                    ATTACK_MODIFIER_UUID, "Reflection Attack Reach", 2.0, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void removeReachBoost(Player player) {
        var reachAttr = player.getAttribute(ForgeMod.BLOCK_REACH.get());
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_UUID);
        }

        var attackAttr = player.getAttribute(ForgeMod.ENTITY_REACH.get());
        if (attackAttr != null) {
            attackAttr.removeModifier(ATTACK_MODIFIER_UUID);
        }
    }
}
