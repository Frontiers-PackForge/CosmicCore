package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.CreateOreFieldMiningRules;
import com.ghostipedia.cosmiccore.common.data.worldgen.field.OreFieldBlockRules;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.simibubi.create.content.kinetics.drill.DrillMovementBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBreakingMovementBehaviour.class)
public abstract class CreateDrillOreFieldMixin {

    @Inject(method = "tickBreaker", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$stallWithoutFieldDrillingFluid(MovementContext context, CallbackInfo ci) {
        if (!cosmiccore$isFieldDrill(context)) return;
        if (CreateOreFieldMiningRules.hasFluid(context)) return;

        context.stall = true;
        ci.cancel();
    }

    @Inject(
            method = "tickBreaker",
            at = @At(
                     value = "INVOKE",
                     target = "Lcom/simibubi/create/content/kinetics/base/BlockBreakingMovementBehaviour;" +
                             "destroyBlock(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;" +
                             "Lnet/minecraft/core/BlockPos;)V",
                     shift = At.Shift.BEFORE),
            cancellable = true)
    private void cosmiccore$consumeFieldDrillingFluid(MovementContext context, CallbackInfo ci) {
        if (!cosmiccore$isFieldDrill(context)) return;
        if (CreateOreFieldMiningRules.consumeFluid(context)) return;

        context.data.putInt("Progress", 9);
        context.stall = true;
        ci.cancel();
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$reduceFieldOreYield(MovementContext context, BlockPos breakingPos, CallbackInfo ci) {
        if (!((Object) this instanceof DrillMovementBehaviour)) return;
        if (!OreFieldBlockRules.isFieldOre(context.world.getBlockState(breakingPos))) return;

        boolean yieldsOre = context.world.random.nextFloat() < CreateOreFieldMiningRules.YIELD_CHANCE;
        MovementBehaviour movementBehaviour = (MovementBehaviour) (Object) this;
        BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> {
            if (yieldsOre) {
                movementBehaviour.collectOrDropItem(context, stack);
            }
        });
        ci.cancel();
    }

    private boolean cosmiccore$isFieldDrill(MovementContext context) {
        if (!((Object) this instanceof DrillMovementBehaviour)) return false;
        if (context.world.isClientSide || !context.data.contains("BreakingPos")) return false;

        BlockPos breakingPos = NBTHelper.readBlockPos(context.data, "BreakingPos");
        BlockState state = context.world.getBlockState(breakingPos);
        return OreFieldBlockRules.isFieldOre(state);
    }
}
