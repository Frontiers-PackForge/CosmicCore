package com.ghostipedia.cosmiccore.mixin.gtfix.sodium.iris;

import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class GTBloomShaderpackCarrierMixin extends AbstractBlockRenderContext {

    private static final int PHOTON_GT_BLOOM_CARRIER_BLOCK_ID = 10081;

    @WrapOperation(method = "bufferQuad",
                   at = @At(value = "INVOKE",
                            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/builder/ChunkMeshBufferBuilder;push([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;I)V"))
    private void cosmiccore$markShaderpackBloom(ChunkMeshBufferBuilder builder,
                                                ChunkVertexEncoder.Vertex[] vertices, int materialBits,
                                                Operation<Void> original,
                                                MutableQuadViewImpl quad, float[] brightnesses, Material material) {
        if (!BloomShaderManager.isBloomAvailable() && GTSodiumCompat.quadHasBloom(quad, this.quadLightData.lm)) {
            for (ChunkVertexEncoder.Vertex vertex : vertices) {
                if (vertex instanceof ChunkVertexExtension extension) {
                    extension.iris$setData(extension.getBlockEmission(), extension.getRenderType(),
                            PHOTON_GT_BLOOM_CARRIER_BLOCK_ID,
                            extension.getLocalPosX(), extension.getLocalPosY(), extension.getLocalPosZ());
                }
            }
        }
        original.call(builder, vertices, materialBits);
    }
}
