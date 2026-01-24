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
 * Behavior for Large Shards of Perpetuity - consume to expand soul capacity.
 */
public class CapacityShardBehavior implements IInteractionItem, IAddInformation {

    private final int capacityGain;

    public CapacityShardBehavior(int capacityGain) {
        this.capacityGain = capacityGain;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

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

            int count = player.isCrouching() ? stack.getCount() : 1;
            int gained = count * capacityGain;
            int oldCapacity = reflection.getBaseCapacity();

            reflection.setBaseCapacity(oldCapacity + gained);
            stack.shrink(count);

            player.displayClientMessage(
                    Component.literal("Soul expanded: " + oldCapacity + " \u2192 " + (oldCapacity + gained))
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    true);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6f, 0.9f);

            return InteractionResultHolder.consume(stack);
        }).orElse(InteractionResultHolder.fail(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to expand soul capacity")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift+Right-click to consume entire stack")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("+" + capacityGain + " capacity each")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
