package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.common.deployment.*;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public final class LeylinePrefabItem extends Item {

    private final boolean pattern;

    public LeylinePrefabItem(Properties properties, boolean pattern) {
        super(properties);
        this.pattern = pattern;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (pattern) return InteractionResult.PASS;
        if (context.getPlayer() instanceof ServerPlayer player)
            LeylineDeploymentService.begin(player, context.getHand(), LeylineDeploymentTarget.trace(player, 1));
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.level.Level level,
                              net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            try {
                LeylinePrefab.migrate(stack, serverLevel);
            } catch (RuntimeException ignored) {}
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        try {
            var descriptor = LeylinePrefab.descriptor(stack);
            if (descriptor.isEmpty()) return;
            int blocks = descriptor.getInt("blocks");
            lines.add(Component.translatable("cosmiccore.leyline.machine", LeylinePrefab.machineName(stack))
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("cosmiccore.leyline.duration", blocks / 20.0)
                    .withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable("cosmiccore.leyline.energy", 128, blocks * 128L)
                    .withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("cosmiccore.leyline.blocks", blocks)
                    .withStyle(ChatFormatting.GRAY));
        } catch (RuntimeException ignored) {
            lines.add(Component.translatable("cosmiccore.leyline.invalid").withStyle(ChatFormatting.RED));
        }
    }
}
