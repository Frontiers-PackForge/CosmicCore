package com.ghostipedia.cosmiccore.mixin.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HarvesterMovementBehaviour.class)
public abstract class SoulGourdHarvesterMixin {

    private static final ResourceLocation SOUL_GOURD_BLOOM = ResourceLocation.fromNamespaceAndPath("cosmiccore",
            "soul_gourd_bloom");
    private static final ResourceLocation SOUL_GOURD_CROP = ResourceLocation.fromNamespaceAndPath("cosmiccore",
            "soul_gourd_crop");
    private static final ResourceLocation SOUL_GOURD_ATTACHED_STEM = ResourceLocation.fromNamespaceAndPath("cosmiccore",
            "soul_gourd_attached_stem");

    @Inject(method = "isValidCrop", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$excludeSoulGourdStems(
                                                  Level level, BlockPos pos, BlockState state,
                                                  CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId.equals(SOUL_GOURD_CROP) || blockId.equals(SOUL_GOURD_ATTACHED_STEM)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isValidOther", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$recognizeSoulGourdFruit(
                                                    Level level, BlockPos pos, BlockState state,
                                                    CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId.equals(SOUL_GOURD_BLOOM)) {
            cir.setReturnValue(true);
        } else if (blockId.equals(SOUL_GOURD_CROP) || blockId.equals(SOUL_GOURD_ATTACHED_STEM)) {
            cir.setReturnValue(false);
        }
    }
}
