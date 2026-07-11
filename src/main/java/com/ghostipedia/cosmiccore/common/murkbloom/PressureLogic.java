package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class PressureLogic {

    private PressureLogic() {}

    public static final float CRUSH_DAMAGE = 8.0f;
    public static final int CRUSH_INTERVAL = 100;
    public static final double CRUSH_NOISE = 5.0;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.tickCount % CRUSH_INTERVAL != 0) return;
        if (!PressureRules.crushing(player)) return;

        player.hurt(player.damageSources().drown(), CRUSH_DAMAGE);
        MurkbloomServerLogic.impulse(player, CRUSH_NOISE, false, MurkbloomServerLogic.KIND_HIT);
        player.displayClientMessage(Component.translatable("cosmiccore.abyss.crush_ascend")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
    }
}
