package com.ghostipedia.cosmiccore.common.reflection.item;

import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;
import com.ghostipedia.cosmiccore.common.reflection.ui.ScarSelectionPackets;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

/**
 * Behavior for Clusters of Perpetuity - used to remove defiance scars.
 * When a bargain is defied, it leaves a scar preventing re-acceptance.
 * Clusters can heal these scars, allowing the bargain to be taken again.
 *
 * Usage: Right-click opens a selection UI to choose which scar to mend.
 */
public class ScarRemovalBehavior implements IInteractionItem, IAddInformation {

    public ScarRemovalBehavior() {}

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        // Server-side only
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        return ReflectionCapability.get(player).map(reflection -> {
            if (!reflection.hasAwakened()) {
                player.displayClientMessage(
                        Component.literal(
                                "The cluster feels... dormant. Perhaps after you've seen yourself in the mirror.")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        true);
                return InteractionResultHolder.fail(stack);
            }

            Set<ResourceLocation> scars = reflection.getDefianceScars();

            if (scars.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("Your soul bears no scars to mend.")
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        true);
                return InteractionResultHolder.fail(stack);
            }

            // Open the scar selection UI
            // The actual cluster consumption happens when the player selects a scar
            ScarSelectionPackets.sendOpenScarSelection(serverPlayer, scars);

            return InteractionResultHolder.success(stack);
        }).orElse(InteractionResultHolder.fail(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                               TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click to mend a defiance scar")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Scars mark bargains you've defied.")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("This cluster can heal one, letting you")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("accept that bargain once more.")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
