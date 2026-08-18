package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ToolHelper.class, remap = false)
public abstract class GTToolAoEBreakEffectScopeFixMixin {

    @WrapMethod(
                method = "destroyBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;Z)Z")
    private static boolean cosmiccore$scopeAoeBreakEffects(ServerPlayer player, ItemStack tool, BlockPos pos,
                                                           boolean playSound,
                                                           Operation<Boolean> original) {
        boolean previousBreakEffects = ToolHelper.DO_BLOCK_BREAK_SOUND_PARTICLES.get();
        boolean previousAoeBreak = ToolHelper.IS_AOE_BREAKING_BLOCKS.get();
        try {
            return original.call(player, tool, pos, playSound);
        } finally {
            ToolHelper.DO_BLOCK_BREAK_SOUND_PARTICLES.set(previousBreakEffects);
            ToolHelper.IS_AOE_BREAKING_BLOCKS.set(previousAoeBreak);
        }
    }
}
