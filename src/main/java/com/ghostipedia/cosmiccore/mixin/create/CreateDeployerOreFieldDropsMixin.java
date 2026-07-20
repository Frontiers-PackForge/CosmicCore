package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.CreateOreFieldMiningRules;
import com.ghostipedia.cosmiccore.common.compat.create.MovingDeployerContext;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldBlockRules;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(DeployerHandler.class)
public abstract class CreateDeployerOreFieldDropsMixin {

    @Redirect(
              method = "tryHarvestBlock",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/world/level/block/Block;getDrops(" +
                               "Lnet/minecraft/world/level/block/state/BlockState;" +
                               "Lnet/minecraft/server/level/ServerLevel;" +
                               "Lnet/minecraft/core/BlockPos;" +
                               "Lnet/minecraft/world/level/block/entity/BlockEntity;" +
                               "Lnet/minecraft/world/entity/Entity;" +
                               "Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static List<ItemStack> cosmiccore$reduceMovingFieldOreYield(
                                                                        BlockState state,
                                                                        ServerLevel level,
                                                                        BlockPos pos,
                                                                        BlockEntity blockEntity,
                                                                        Entity entity,
                                                                        ItemStack tool) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, entity, tool);
        if (!(entity instanceof DeployerFakePlayer player)) return drops;
        if (((MovingDeployerContext) player).cosmiccore$getMovementContext() == null) return drops;
        if (!OreFieldBlockRules.isFieldOre(state)) return drops;
        if (level.random.nextFloat() < CreateOreFieldMiningRules.YIELD_CHANCE) return drops;
        return List.of();
    }
}
