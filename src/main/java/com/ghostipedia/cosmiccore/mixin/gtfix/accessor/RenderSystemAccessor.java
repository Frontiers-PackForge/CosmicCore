package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor {

    @Accessor("savedProjectionMatrix")
    static Matrix4f cosmiccore$getSavedProjectionMatrix() {
        throw new AssertionError();
    }

    @Accessor("savedProjectionMatrix")
    static void cosmiccore$setSavedProjectionMatrix(Matrix4f matrix) {
        throw new AssertionError();
    }

    @Accessor("savedVertexSorting")
    static VertexSorting cosmiccore$getSavedVertexSorting() {
        throw new AssertionError();
    }

    @Accessor("savedVertexSorting")
    static void cosmiccore$setSavedVertexSorting(VertexSorting vertexSorting) {
        throw new AssertionError();
    }

    @Accessor("shaderLightDirections")
    static Vector3f[] cosmiccore$getShaderLightDirections() {
        throw new AssertionError();
    }
}
