package com.ghostipedia.cosmiccore.common.abyss;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssRegions;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.AbyssTimeWarnPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncAbyssAttunementPacket;
import com.ghostipedia.cosmiccore.common.network.packet.SyncTimeBarPacket;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

import static com.ghostipedia.cosmiccore.common.abyss.AbyssRules.WARNINGS;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class AbyssLogic {

    public static final int ATTUNEMENT_LAYER = 2;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();

        Optional.of(player.getData(CosmicAttachmentTypes.ABYSS_BUDGET)).ifPresent(cap -> {
            if (cap.getRemainingTicks(AbyssRules.DIM) < 0) {
                cap.setRemainingTicks(AbyssRules.DIM, AbyssRules.FIRST_ENTRY_TICKS);
                cap.setDecaying(AbyssRules.DIM, level.dimension().equals(AbyssRules.DIM));
            }

            boolean inAbyss = level.dimension().equals(AbyssRules.DIM);

            if (inAbyss && !player.getData(CosmicAttachmentTypes.ABYSS_ATTUNED) &&
                    AbyssRegions.layer(player.getBlockY()) >= ATTUNEMENT_LAYER) {
                player.setData(CosmicAttachmentTypes.ABYSS_ATTUNED, true);
                player.sendSystemMessage(Component.translatable("cosmiccore.abyss.seal_broken")
                        .withStyle(style -> style.withColor(0xC9AEF5).withItalic(true)));
                CCoreNetwork.sendToPlayer(player, new SyncAbyssAttunementPacket(true));
            }

            if (inAbyss) {
                if (cap.isDecaying(AbyssRules.DIM)) {
                    long left = cap.getRemainingTicks(AbyssRules.DIM);
                    if (left > 0) {
                        long next = left - 1;
                        cap.setRemainingTicks(AbyssRules.DIM, next);
                        warnPlayer(player, next / 20);
                    } else {
                        executePlayer(player, cap);
                    }
                }
            } else {
                long current = cap.getRemainingTicks(AbyssRules.DIM);
                if (current < AbyssRules.MAX_TICKS) {
                    double buffer = cap.getCleanse(AbyssRules.DIM) + (AbyssRules.REGEN_PER_SECOND / 20.0);
                    long recovered = (long) (buffer * 20.0);
                    double remainder = buffer - (recovered / 20.0);
                    if (recovered > 0) {
                        cap.setRemainingTicks(AbyssRules.DIM, Math.min(AbyssRules.MAX_TICKS, current + recovered));
                    }
                    cap.setCleanse(AbyssRules.DIM, remainder);
                }
            }

            // HUD sync (every 10 ticks)
            if ((level.getGameTime() % 10) == 0) {
                if (inAbyss) {
                    sendHUD(player, cap.getRemainingTicks(AbyssRules.DIM), AbyssRules.MAX_TICKS);
                } else {
                    hideHUD(player);
                }
            }
        });
    }

    @SubscribeEvent
    public static void onDimChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();

        Optional.of(player.getData(CosmicAttachmentTypes.ABYSS_BUDGET)).ifPresent(cap -> {
            boolean nowInAbyss = player.level().dimension().equals(AbyssRules.DIM);
            cap.setDecaying(AbyssRules.DIM, nowInAbyss);

            if (nowInAbyss) {
                sendHUD(player, cap.getRemainingTicks(AbyssRules.DIM), AbyssRules.MAX_TICKS);
            } else {
                hideHUD(player);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();

        CCoreNetwork.sendToPlayer(player,
                new SyncAbyssAttunementPacket(player.getData(CosmicAttachmentTypes.ABYSS_ATTUNED)));

        Optional.of(player.getData(CosmicAttachmentTypes.ABYSS_BUDGET)).ifPresent(cap -> {
            if (cap.getRemainingTicks(AbyssRules.DIM) < 0) {
                cap.setRemainingTicks(AbyssRules.DIM, AbyssRules.FIRST_ENTRY_TICKS);
            }
            cap.setDecaying(AbyssRules.DIM, player.level().dimension().equals(AbyssRules.DIM));

            if (player.level().dimension().equals(AbyssRules.DIM)) {
                sendHUD(player, cap.getRemainingTicks(AbyssRules.DIM), AbyssRules.MAX_TICKS);
            } else {
                hideHUD(player);
            }
        });
    }

    private static void warnPlayer(ServerPlayer player, long secondsLeft) {
        for (int s : WARNINGS) {
            if (secondsLeft == s) {
                CCoreNetwork.sendToPlayer(player,
                        new AbyssTimeWarnPacket(Component.translatable("abyss.warning.notice", s)));
                return;
            }
        }
    }

    // OBLITERATE THE OFFENDER - GET OUT! GET OUT! GET OUT! GET OUT! GET OUT! GET OUT! GET OUT! GET OUT! GET OUT! GET
    // OUT!
    private static void executePlayer(ServerPlayer player, IAbyssTimer cap) {
        cap.setDecaying(AbyssRules.DIM, false);
        hideHUD(player);
        player.hurt(player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
    }

    private static void sendHUD(ServerPlayer player, long remain, long max) {
        CCoreNetwork.sendToPlayer(player, new SyncTimeBarPacket(AbyssRules.DIM.location(), remain, max));
    }

    private static void hideHUD(ServerPlayer player) {
        CCoreNetwork.sendToPlayer(player, new SyncTimeBarPacket(AbyssRules.DIM.location(), -1, 0));
    }
}
