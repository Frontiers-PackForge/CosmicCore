package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.client.murkbloom.SonarPulseRenderer;
import com.ghostipedia.cosmiccore.common.murkbloom.MurkbloomServerLogic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AbyssalSonarItem extends Item {

    public static final int COOLDOWN_TICKS = 100;

    public AbyssalSonarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS,
                    0.8f, 1.5f);
            if (player instanceof ServerPlayer serverPlayer) {
                MurkbloomServerLogic.sonarPing(serverPlayer);
                serverPlayer.displayClientMessage(Component.translatableWithFallback(
                        "item.cosmiccore.abyssal_sonar.sing",
                        "You sing into the abyss; the abyss sings back."), true);
            }
        } else {
            SonarPulseRenderer.firePulse(player.getEyePosition());
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
