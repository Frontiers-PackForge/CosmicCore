package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.common.compat.effortlessbuilding.EffortlessBuildingAE2CableCompat;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.hooks.RenderBlockOutlineHook;
import com.mojang.blaze3d.vertex.PoseStack;
import neoforge.nl.requios.effortlessbuilding.buildpipeline.BuildPipelineClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBlockOutlineHook.class)
public abstract class AE2PartPlacementPreviewMixin {

    @Inject(method = "showPartPlacementPreview", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$hidePreviewDuringEffortlessBuilding(
                                                                       Player player, PoseStack poseStack,
                                                                       MultiBufferSource buffers, Camera camera,
                                                                       BlockHitResult blockHitResult,
                                                                       ItemStack itemInHand, boolean insideBlock,
                                                                       CallbackInfo ci) {
        if (BuildPipelineClient.shouldInterceptPlacing() &&
                EffortlessBuildingAE2CableCompat.isCableItem(itemInHand)) {
            ci.cancel();
        }
    }
}
