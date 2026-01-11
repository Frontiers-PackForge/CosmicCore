package com.ghostipedia.cosmiccore.common.reflection.item;

import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Behavior for Shards of Perpetuity - right-click to consume and add to soul shard balance.
 * Used as currency for bargains in the Reflection system.
 */
public class ShardConsumeBehavior implements IInteractionItem, IAddInformation {

    private final int shardValue;

    /**
     * @param shardValue How many shards this item is worth when consumed
     */
    public ShardConsumeBehavior(int shardValue) {
        this.shardValue = shardValue;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // Check if player has awakened (has the reflection capability active)
        return ReflectionCapability.get(player).map(reflection -> {
            if (!reflection.hasAwakened()) {
                player.displayClientMessage(
                        Component
                                .literal(
                                        "The shard feels... dormant. Perhaps after you've seen yourself in the mirror.")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        true);
                return InteractionResultHolder.fail(stack);
            }

            // Calculate total shards to add (stack size * value)
            int count = player.isCrouching() ? stack.getCount() : 1;
            int totalShards = count * shardValue;

            // Add shards to balance
            reflection.addShards(totalShards);

            // Consume items
            stack.shrink(count);

            // Feedback
            player.displayClientMessage(
                    Component.literal("+" + totalShards + " shards absorbed")
                            .withStyle(ChatFormatting.AQUA),
                    true);

            // Sound effect
            level.playSound(null, player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f,
                    1.2f + (level.random.nextFloat() * 0.2f));

            // Particle effect could be added here via packet

            return InteractionResultHolder.consume(stack);
        }).orElse(InteractionResultHolder.fail(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to absorb into your soul")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift+Right-click to absorb entire stack")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Value: " + shardValue + " shard" + (shardValue > 1 ? "s" : "") + " each")
                .withStyle(ChatFormatting.AQUA));
    }
}
