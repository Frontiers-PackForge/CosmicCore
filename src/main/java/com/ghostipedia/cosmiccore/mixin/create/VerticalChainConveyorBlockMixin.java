package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.VerticalChainDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainConveyorBlock.class, remap = false)
public abstract class VerticalChainConveyorBlockMixin {

    private static final String[] DIRECTION_KEYS = {
            "cosmiccore.direction.north",
            "cosmiccore.direction.east",
            "cosmiccore.direction.south",
            "cosmiccore.direction.west"
    };

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$cycleVerticalDirection(
                                                   ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult,
                                                   CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!AllItems.WRENCH.isIn(stack) || player.isShiftKeyDown()) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity blockEntity) ||
                !(blockEntity instanceof VerticalChainDirection verticalChain)) {
            return;
        }
        if (!level.isClientSide) {
            verticalChain.cosmiccore$cycleVerticalDirection();
            int direction = verticalChain.cosmiccore$getVerticalDirection();
            player.displayClientMessage(
                    Component.translatable(
                            "cosmiccore.create.chain_conveyor.vertical_direction",
                            Component.translatable(DIRECTION_KEYS[direction])),
                    true);
        }
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}
