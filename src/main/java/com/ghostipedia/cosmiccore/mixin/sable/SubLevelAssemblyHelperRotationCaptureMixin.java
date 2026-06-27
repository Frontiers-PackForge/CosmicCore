package com.ghostipedia.cosmiccore.mixin.sable;

import com.ghostipedia.cosmiccore.integration.sable.SableAssemblyRotationHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public abstract class SubLevelAssemblyHelperRotationCaptureMixin {

    @Inject(
            method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V",
            at = @At("HEAD"))
    private static void cosmiccore$captureRotation(ServerLevel level,
                                                   SubLevelAssemblyHelper.AssemblyTransform transform,
                                                   Iterable<BlockPos> positions, CallbackInfo ci) {
        SableAssemblyRotationHolder.set(transform.getRotation());
    }

    @Inject(
            method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V",
            at = @At("RETURN"))
    private static void cosmiccore$clearRotation(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform,
                                                 Iterable<BlockPos> positions, CallbackInfo ci) {
        SableAssemblyRotationHolder.clear();
    }
}
