package com.ghostipedia.cosmiccore.common.reflection.item;

import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionConstants;
import com.ghostipedia.cosmiccore.common.reflection.ui.VoidUIPackets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Mirror - a handheld item that lets you face your Reflection.
 *
 * Right-click to gaze into the void and commune with your soul.
 * The Reflection will offer bargains, show your erosion state,
 * and speak to you about your journey.
 *
 * "It's just a mirror. Why does it feel like it's looking back?"
 */
public class MirrorItem extends Item {

    public MirrorItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!ReflectionConstants.ENABLED) return InteractionResultHolder.pass(stack);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Check if the reflection has awakened
            boolean awakened = ReflectionCapability.get(serverPlayer)
                    .map(r -> r.hasAwakened())
                    .orElse(false);

            if (!awakened) {
                // The mirror shows nothing yet
                serverPlayer.displayClientMessage(
                        Component.literal("§7§oYou see only yourself. Nothing stirs within."),
                        true);
                // Play subtle sound
                level.playSound(null, player.blockPosition(),
                        SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.5f, 0.8f);
            } else {
                // Open the mirror hub UI
                VoidUIPackets.sendOpenHub(serverPlayer);

                // Play ominous sound
                level.playSound(null, player.blockPosition(),
                        SoundEvents.AMBIENT_CAVE.value(), SoundSource.PLAYERS, 0.3f, 0.5f);
            }
        }

        // Cooldown to prevent spam
        player.getCooldowns().addCooldown(this, 20); // 1 second

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.literal("§7A polished surface that reflects more than light."));
        tooltip.add(Component.literal("§8§oRight-click to gaze into the void."));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Enchanted glint when holding - looks mysterious
        return true;
    }
}
