package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.block.crop.CosmicCrops;

import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SawBlockEntity.class)
public abstract class SoulGourdSawMixin {

    @Inject(method = "isSawable", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$allowSoulGourd(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock().asItem() == CosmicCrops.SOUL_GOURD.get()) {
            cir.setReturnValue(true);
        }
    }
}
