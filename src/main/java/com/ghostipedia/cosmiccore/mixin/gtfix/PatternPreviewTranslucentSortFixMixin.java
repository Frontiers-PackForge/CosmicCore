package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer$RenderCompileTask", remap = false)
public abstract class PatternPreviewTranslucentSortFixMixin {

    @Shadow
    @Final
    PatternPreviewRenderer this$0;

    @Redirect(
              method = "compileBlockBuffers",
              at = @At(
                       value = "INVOKE",
                       target = "Lcom/mojang/blaze3d/vertex/MeshData;sortQuads(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexSorting;)Lcom/mojang/blaze3d/vertex/MeshData$SortState;"),
              remap = false)
    private MeshData.SortState cosmiccore$sortTranslucentAgainstRealCamera(MeshData meshData,
                                                                           ByteBufferBuilder buffer,
                                                                           VertexSorting ignoredOriginSorting,
                                                                           @Local(argsOnly = true) Vec3 cameraPos) {
        BlockPos controllerPos = ((PatternPreviewControllerPosAccessor) (Object) this.this$0)
                .cosmiccore$getControllerPos();
        if (controllerPos == null) {
            return meshData.sortQuads(buffer, ignoredOriginSorting);
        }
        float x = (float) (cameraPos.x - controllerPos.getX());
        float y = (float) (cameraPos.y - controllerPos.getY());
        float z = (float) (cameraPos.z - controllerPos.getZ());
        return meshData.sortQuads(buffer, VertexSorting.byDistance(x, y, z));
    }
}

@Mixin(value = PatternPreviewRenderer.class, remap = false)
interface PatternPreviewControllerPosAccessor {

    @Accessor("controllerPos")
    BlockPos cosmiccore$getControllerPos();
}
