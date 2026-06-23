package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldPlacement.OreField;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.RevealFieldsPacket;

import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class DowsingRodBehavior implements IInteractionItem, IAddInformation {

    private static final byte ROD_TIER = 0;

    private final int radius;
    private final int cooldownTicks;

    public DowsingRodBehavior(int radius, int cooldownTicks) {
        this.radius = radius;
        this.cooldownTicks = cooldownTicks;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel &&
                player instanceof ServerPlayer serverPlayer) {
            BlockPos center = player.blockPosition();
            Optional<OreField> nearest = OreFieldPlacement.nearestField(
                    serverLevel.getSeed(), serverLevel.dimension(), null, center.getX(), center.getZ(), radius);

            if (nearest.isPresent()) {
                CCoreNetwork.sendToPlayer(serverPlayer,
                        RevealFieldsPacket.of(serverLevel.dimension(), List.of(nearest.get()), ROD_TIER));
                player.sendSystemMessage(Component.translatable("cosmiccore.dowsing.found")
                        .withStyle(ChatFormatting.GOLD));
                level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.0f);
            } else {
                player.sendSystemMessage(Component.translatable("cosmiccore.dowsing.none")
                        .withStyle(ChatFormatting.GRAY));
            }
            player.getCooldowns().addCooldown(stack.getItem(), cooldownTicks);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("cosmiccore.dowsing.tooltip.radius", radius)
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("cosmiccore.dowsing.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
