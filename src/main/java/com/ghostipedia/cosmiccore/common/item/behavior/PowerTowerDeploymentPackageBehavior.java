package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentService;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentTarget;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

// The Deployment behavior of specifically the power tower, for logical linking and other fun BS later on.
public final class PowerTowerDeploymentPackageBehavior implements IInteractionItem, IAddInformation {

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer serverPlayer) {
            LeylineDeploymentService.begin(serverPlayer, context.getHand(), LeylineDeploymentTarget.trace(player, 1));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag flag) {
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.0")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.1")
                .withStyle(ChatFormatting.DARK_AQUA));
        lines.add(Component.translatable("cosmiccore.deployment.power_tower.tooltip.2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
