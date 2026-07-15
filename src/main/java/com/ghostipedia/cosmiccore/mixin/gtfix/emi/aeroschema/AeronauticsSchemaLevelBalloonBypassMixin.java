package com.ghostipedia.cosmiccore.mixin.gtfix.emi.aeroschema;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import brachy.modularui.drawable.schema.SchemaLevel;
import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelChunk.class, priority = 1500)
public abstract class AeronauticsSchemaLevelBalloonBypassMixin {

    @Shadow
    @Final
    private Level level;

    @TargetHandler(
                   mixin = "dev.eriksonn.aeronautics.mixin.balloon.LevelChunkMixin",
                   name = "simulated$setBlockState",
                   prefix = "wrapOperation")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void cosmiccore$skipFakeWorldBalloonUpdate(LevelChunkSection section, int x, int y, int z,
                                                       BlockState newState, Operation<BlockState> original,
                                                       CallbackInfoReturnable<BlockState> cir) {
        if (level instanceof SchemaLevel) {
            cir.setReturnValue(original.call(section, x, y, z, newState));
        }
    }
}
