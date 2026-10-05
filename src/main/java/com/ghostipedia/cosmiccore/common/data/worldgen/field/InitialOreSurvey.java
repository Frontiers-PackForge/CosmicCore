package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.GameRules;

import java.util.List;

public final class InitialOreSurvey {

    public static final int DEFAULT_RADIUS = 1000;
    public static final int MAX_RADIUS = 5000;
    public static final GameRules.Key<GameRules.IntegerValue> RADIUS = GameRules.register(
            "cosmiccoreStartingOreSurveyRadius", GameRules.Category.PLAYER,
            GameRules.IntegerValue.create(DEFAULT_RADIUS));

    private InitialOreSurvey() {}

    public static void init() {}

    public static int normalizeRadius(int radius) {
        return Math.clamp(radius, 0, MAX_RADIUS) / 1000 * 1000;
    }

    public static void onLogin(ServerPlayer player, FieldDiscoveryData data) {
        if (data.hasInitialSurvey(player.getUUID())) return;
        int radius = normalizeRadius(player.serverLevel().getGameRules().getInt(RADIUS));
        if (radius > 0 && player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) == 0) {
            var center = player.blockPosition();
            var fields = OreFieldTerrainResolver.resolveNear(player.serverLevel(), center.getX(), center.getZ(),
                    radius);
            List<RevealedField> revealed = RevealFieldsPacket.toRevealedFields(fields, (byte) 0);
            FieldDiscoverySharing.shareWithTeam(player, player.serverLevel().dimension(), revealed);
            player.sendSystemMessage(Component.translatable("cosmiccore.survey.initial.complete", fields.size(), radius)
                    .withStyle(ChatFormatting.GOLD));
        }
        data.markInitialSurvey(player.getUUID());
    }
}
