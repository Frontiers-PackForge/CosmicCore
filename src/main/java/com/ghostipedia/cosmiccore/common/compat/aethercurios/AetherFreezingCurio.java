package com.ghostipedia.cosmiccore.common.compat.aethercurios;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import top.theillusivec4.curios.api.SlotContext;

public class AetherFreezingCurio extends AetherAccessoryCurio {

    public AetherFreezingCurio(ResourceLocation equipSound) {
        super(equipSound);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof ServerPlayer player)) return;
        if (player.isSpectator() || player.getAbilities().flying) return;
        if (player.isInFluidType()) return;

        ServerLevel level = (ServerLevel) player.level();
        BlockPos center = player.blockPosition();
        int frozen = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    FluidState fluid = state.getFluidState();
                    if (!fluid.isSource()) continue;
                    if (fluid.is(FluidTags.WATER) && state.is(Blocks.WATER)) {
                        level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                        frozen++;
                    } else if (fluid.is(FluidTags.LAVA) && state.is(Blocks.LAVA)) {
                        level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                        frozen++;
                    }
                }
            }
        }
        if (frozen > 0) {
            stack.hurtAndBreak(frozen / 3, level, player, item -> {});
        }
    }
}
