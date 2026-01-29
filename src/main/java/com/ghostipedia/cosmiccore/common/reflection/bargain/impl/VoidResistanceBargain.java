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

public class VoidResistanceBargain extends Bargain {

    public static final ResourceLocation ID = CosmicCore.id("void_anchor");
    public static final VoidResistanceBargain INSTANCE = new VoidResistanceBargain();
    private static final String BARGAIN_ID = "void_anchor";

    public static final int BASE_VOID_SAVE_COST = 10;
    public static final int MAX_VOID_SAVE_COST = 160;

    private VoidResistanceBargain() {
        super(
                ID,
                BargainTier.LATE,
                BargainCategory.DEATH,
                0,    // shardCost - FREE in shards, but massive commitment
                75,   // weight - takes up most of your soul capacity
                500   // erosion - huge transformation
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
                new BargainAnswer("anchor", ReflectionLang.answerText(BARGAIN_ID, "anchor"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "anchor"))
                        .withDetails(
                                List.of(
                                        ReflectionLang.answerPower(BARGAIN_ID, "anchor", 0),
                                        ReflectionLang.answerPower(BARGAIN_ID, "anchor", 1)),
                                List.of(
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "anchor", 0),
                                        ReflectionLang.answerDrawback(BARGAIN_ID, "anchor", 1))),
                new BargainAnswer("refuse", ReflectionLang.answerText(BARGAIN_ID, "refuse"),
                        ReflectionLang.answerResponse(BARGAIN_ID, "refuse"))
                        .withReducedPower());
    }

    @Override
    public void onAccept(Player player, BargainAnswer answer) {
        if (!answer.id().equals("refuse")) {
            ReflectionCapability.get(player).ifPresent(reflection -> reflection.resetVoidSaveCount());
            player.displayClientMessage(ReflectionLang.bargainOnAccept(BARGAIN_ID), false);
        }
    }

    @Override
    public void onDefy(Player player) {
        ReflectionCapability.get(player).ifPresent(reflection -> reflection.resetVoidSaveCount());
        player.displayClientMessage(ReflectionLang.bargainOnDefy(BARGAIN_ID), false);
    }

    @Override
    public void tick(Player player) {
        if (player.getY() < -128 && !player.isCreative() && !player.isSpectator()) {
            if (!(player instanceof ServerPlayer)) return;

            ReflectionCapability.get(player).ifPresent(reflection -> {
                int saveCount = reflection.getVoidSaveCount();
                int cost = Math.min(MAX_VOID_SAVE_COST, BASE_VOID_SAVE_COST * (1 << saveCount));

                double x = player.getX();
                double z = player.getZ();
                double y = player.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        (int) x, (int) z) + 1;

                player.teleportTo(x, y, z);
                reflection.addErosion(cost);
                reflection.incrementVoidSaveCount();

                if (cost <= 20) {
                    player.displayClientMessage(
                            Component.literal(
                                    "\u00A75\u00A7o*The void rejects you. Not yet, it whispers. [Erosion +" + cost +
                                            "]*"),
                            true);
                } else if (cost <= 80) {
                    player.displayClientMessage(
                            Component
                                    .literal("\u00A75\u00A7o*The anchor strains. The void's grip tightens. [Erosion +" +
                                            cost + "]*"),
                            true);
                } else {
                    player.displayClientMessage(
                            Component.literal(
                                    "\u00A74\u00A7o*The price is almost too high. The void is patient. It will have you eventually. [Erosion +" +
                                            cost + "]*"),
                            true);
                }
            });
        }
    }

    @Override
    public BargainVisual getSoulVisual() {
        return BargainVisual.of("anchored",
                "A dark chain extends from the soul downward, links multiplying with each save");
    }

    public static int getCurrentCost(Player player) {
        return ReflectionCapability.get(player).map(reflection -> {
            int saveCount = reflection.getVoidSaveCount();
            return Math.min(MAX_VOID_SAVE_COST, BASE_VOID_SAVE_COST * (1 << saveCount));
        }).orElse(BASE_VOID_SAVE_COST);
    }

    public static int getSaveCount(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.getVoidSaveCount())
                .orElse(0);
    }
}
