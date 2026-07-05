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

import java.util.Comparator;
import java.util.List;

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
            List<OreField> fields = OreFieldPlacement.fieldsNear(
                    serverLevel.getSeed(), serverLevel.dimension(), center.getX(), center.getZ(), radius);
            fields.sort(Comparator.comparingInt(field -> horizontalDistance(field.core(), center)));

            if (fields.isEmpty()) {
                player.sendSystemMessage(Component.translatable("cosmiccore.dowsing.none")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                CCoreNetwork.sendToPlayer(serverPlayer,
                        RevealFieldsPacket.of(serverLevel.dimension(), fields, ROD_TIER));
                player.sendSystemMessage(Component.translatable("cosmiccore.dowsing.found", fields.size())
                        .withStyle(ChatFormatting.GOLD));
                int index = 1;
                for (OreField field : fields) {
                    int distance = horizontalDistance(field.core(), center);
                    player.sendSystemMessage(Component.literal("  " + index + ". ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(field.bundle().getLocalizedName().copy().withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(distance + "m").withStyle(ChatFormatting.WHITE)));
                    index++;
                }
                level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.0f);
            }
            player.getCooldowns().addCooldown(stack.getItem(), cooldownTicks);
        }

        return InteractionResultHolder.success(stack);
    }

    private static int horizontalDistance(BlockPos from, BlockPos to) {
        long dx = from.getX() - to.getX();
        long dz = from.getZ() - to.getZ();
        return (int) Math.sqrt(dx * dx + dz * dz);
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
