package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.CreateOreFieldMiningRules;
import com.ghostipedia.cosmiccore.common.compat.create.MovingDeployerContext;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldBlockRules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployerMovementBehaviour.class)
public abstract class CreateDeployerOreFieldMixin {

    @Shadow
    private DeployerFakePlayer getPlayer(MovementContext context) {
        throw new AssertionError();
    }

    @Inject(
            method = "visitNewPosition",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerMovementBehaviour;" +
                             "tryGrabbingItem(Lcom/simibubi/create/content/contraptions/behaviour/" +
                             "MovementContext;)V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void cosmiccore$gateInitialFieldMining(MovementContext context, BlockPos pos, CallbackInfo ci) {
        DeployerFakePlayer player = getPlayer(context);
        ((MovingDeployerContext) player).cosmiccore$setMovementContext(context);
        if (!cosmiccore$isFieldMiningAttempt(context, pos, player)) return;
        if (cosmiccore$canActivate(context, pos, player, true)) return;

        MovingDeployerContext bridge = (MovingDeployerContext) player;
        Pair<BlockPos, Float> progress = bridge.cosmiccore$getBlockBreakingProgress();
        if (progress == null || !progress.getLeft().equals(pos)) {
            bridge.cosmiccore$setBlockBreakingProgress(Pair.of(pos, 0f));
        }
        context.stall = true;
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$gateContinuedFieldMining(MovementContext context, CallbackInfo ci) {
        DeployerFakePlayer player = getPlayer(context);
        MovingDeployerContext bridge = (MovingDeployerContext) player;
        bridge.cosmiccore$setMovementContext(context);

        Pair<BlockPos, Float> progress = bridge.cosmiccore$getBlockBreakingProgress();
        if (progress == null) return;

        BlockPos pos = progress.getLeft();
        if (!cosmiccore$isFieldMiningAttempt(context, pos, player)) return;

        boolean activatesThisTick = context.data.getInt("Timer") >= 20;
        if (cosmiccore$canActivate(context, pos, player, activatesThisTick)) return;

        context.stall = true;
        ci.cancel();
    }

    private boolean cosmiccore$canActivate(
                                           MovementContext context, BlockPos pos, DeployerFakePlayer player,
                                           boolean activatesThisTick) {
        if (!CreateOreFieldMiningRules.hasFluid(context)) return false;
        if (!activatesThisTick || !cosmiccore$willBreakBlock(context, pos, player)) return true;
        return CreateOreFieldMiningRules.consumeFluid(context);
    }

    private boolean cosmiccore$willBreakBlock(
                                              MovementContext context, BlockPos pos, DeployerFakePlayer player) {
        BlockState state = context.world.getBlockState(pos);
        float progress = state.getDestroyProgress(player, context.world, pos) * 16;
        Pair<BlockPos, Float> current = ((MovingDeployerContext) player).cosmiccore$getBlockBreakingProgress();
        if (current != null && current.getLeft().equals(pos)) {
            progress += current.getRight();
        }
        return progress >= 1;
    }

    private boolean cosmiccore$isFieldMiningAttempt(
                                                    MovementContext context, BlockPos pos, DeployerFakePlayer player) {
        if (context.world.isClientSide || context.blockEntityData == null) return false;
        if (!"PUNCH".equalsIgnoreCase(context.blockEntityData.getString("Mode"))) return false;

        BlockState state = context.world.getBlockState(pos);
        if (!OreFieldBlockRules.isFieldOre(state)) return false;

        ItemStack tool = player.getMainHandItem();
        return !tool.isEmpty() && state.canHarvestBlock(context.world, pos, player);
    }
}
