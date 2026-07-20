package com.ghostipedia.cosmiccore.client.compat.modularui;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.RenderSystemAccessor;

import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.GlStateBackup;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

public final class SchemaRenderState {

    private final Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
    private final Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewStack());
    private final Matrix4f texture = new Matrix4f(RenderSystem.getTextureMatrix());
    private final Matrix4f savedProjection = new Matrix4f(RenderSystemAccessor.cosmiccore$getSavedProjectionMatrix());
    private final VertexSorting vertexSorting = RenderSystem.getVertexSorting();
    private final VertexSorting savedVertexSorting = RenderSystemAccessor.cosmiccore$getSavedVertexSorting();
    private final float[] shaderColor = RenderSystem.getShaderColor().clone();
    private final float shaderGlintAlpha = RenderSystem.getShaderGlintAlpha();
    private final float shaderFogStart = RenderSystem.getShaderFogStart();
    private final float shaderFogEnd = RenderSystem.getShaderFogEnd();
    private final float[] shaderFogColor = RenderSystem.getShaderFogColor().clone();
    private final FogShape shaderFogShape = RenderSystem.getShaderFogShape();
    private final float shaderLineWidth = RenderSystem.getShaderLineWidth();
    private final Vector3f[] shaderLights = copyLights(RenderSystemAccessor.cosmiccore$getShaderLightDirections());
    private final int[] shaderTextures = new int[GlStateManager.TEXTURE_COUNT];
    private final int activeTexture = GlStateManager._getActiveTexture();
    private final int viewportX = GlStateManager.Viewport.x();
    private final int viewportY = GlStateManager.Viewport.y();
    private final int viewportWidth = GlStateManager.Viewport.width();
    private final int viewportHeight = GlStateManager.Viewport.height();
    private final GlStateBackup glState = new GlStateBackup();
    private final ShaderInstance shader = RenderSystem.getShader();
    private final Map<ShaderInstance, ShaderUniformState> shaderStates = new IdentityHashMap<>();

    public SchemaRenderState() {
        RenderSystem.backupGlState(this.glState);
        for (int i = 0; i < this.shaderTextures.length; i++) {
            this.shaderTextures[i] = RenderSystem.getShaderTexture(i);
        }
        captureShader(this.shader);
    }

    public void captureShader(ShaderInstance shader) {
        if (shader != null) {
            this.shaderStates.computeIfAbsent(shader, ShaderUniformState::new);
        }
    }

    public void restore() {
        ShaderInstance leakedShader = RenderSystem.getShader();
        if (leakedShader != null) {
            leakedShader.clear();
        }
        VertexBuffer.unbind();

        RenderSystem.setProjectionMatrix(this.projection, this.vertexSorting);
        RenderSystem.getModelViewStack().set(this.modelView);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setTextureMatrix(this.texture);
        RenderSystemAccessor.cosmiccore$setSavedProjectionMatrix(new Matrix4f(this.savedProjection));
        RenderSystemAccessor.cosmiccore$setSavedVertexSorting(this.savedVertexSorting);

        RenderSystem.setShaderColor(this.shaderColor[0], this.shaderColor[1], this.shaderColor[2], this.shaderColor[3]);
        RenderSystem.setShaderGlintAlpha(this.shaderGlintAlpha);
        RenderSystem.setShaderFogStart(this.shaderFogStart);
        RenderSystem.setShaderFogEnd(this.shaderFogEnd);
        RenderSystem.setShaderFogColor(
                this.shaderFogColor[0], this.shaderFogColor[1], this.shaderFogColor[2], this.shaderFogColor[3]);
        RenderSystem.setShaderFogShape(this.shaderFogShape);
        RenderSystem.lineWidth(this.shaderLineWidth);

        Vector3f[] renderSystemLights = RenderSystemAccessor.cosmiccore$getShaderLightDirections();
        for (int i = 0; i < renderSystemLights.length; i++) {
            renderSystemLights[i] = this.shaderLights[i] == null ? null : new Vector3f(this.shaderLights[i]);
        }

        for (int i = 0; i < this.shaderTextures.length; i++) {
            RenderSystem.setShaderTexture(i, this.shaderTextures[i]);
        }
        this.shaderStates.values().forEach(ShaderUniformState::restore);

        RenderSystem.setShader(() -> this.shader);
        if (this.shader != null) {
            for (int i = 0; i < this.shaderTextures.length; i++) {
                this.shader.setSampler("Sampler" + i, this.shaderTextures[i]);
            }
            this.shader.apply();
        }

        RenderSystem.viewport(this.viewportX, this.viewportY, this.viewportWidth, this.viewportHeight);
        RenderSystem.restoreGlState(this.glState);
        RenderSystem.activeTexture(this.activeTexture);
    }

    private static Vector3f[] copyLights(Vector3f[] lights) {
        Vector3f[] copy = new Vector3f[lights.length];
        for (int i = 0; i < lights.length; i++) {
            copy[i] = lights[i] == null ? null : new Vector3f(lights[i]);
        }
        return copy;
    }

    private static final class ShaderUniformState {

        private final UniformValue[] values;

        private ShaderUniformState(ShaderInstance shader) {
            Uniform[] uniforms = {
                    shader.MODEL_VIEW_MATRIX,
                    shader.PROJECTION_MATRIX,
                    shader.TEXTURE_MATRIX,
                    shader.SCREEN_SIZE,
                    shader.COLOR_MODULATOR,
                    shader.LIGHT0_DIRECTION,
                    shader.LIGHT1_DIRECTION,
                    shader.GLINT_ALPHA,
                    shader.FOG_START,
                    shader.FOG_END,
                    shader.FOG_COLOR,
                    shader.FOG_SHAPE,
                    shader.LINE_WIDTH,
                    shader.GAME_TIME,
                    shader.CHUNK_OFFSET
            };
            this.values = new UniformValue[uniforms.length];
            for (int i = 0; i < uniforms.length; i++) {
                this.values[i] = uniforms[i] == null ? null : new UniformValue(uniforms[i]);
            }
        }

        private void restore() {
            for (UniformValue value : this.values) {
                if (value != null) {
                    value.restore();
                }
            }
        }
    }

    private static final class UniformValue {

        private final Uniform uniform;
        private final int[] intValues;
        private final float[] floatValues;

        private UniformValue(Uniform uniform) {
            this.uniform = uniform;
            if (uniform.getType() <= 3) {
                IntBuffer source = uniform.getIntBuffer();
                this.intValues = new int[uniform.getCount()];
                this.floatValues = null;
                for (int i = 0; i < this.intValues.length; i++) {
                    this.intValues[i] = source.get(i);
                }
            } else {
                FloatBuffer source = uniform.getFloatBuffer();
                this.intValues = null;
                this.floatValues = new float[uniform.getCount()];
                for (int i = 0; i < this.floatValues.length; i++) {
                    this.floatValues[i] = source.get(i);
                }
            }
        }

        private void restore() {
            if (this.intValues == null) {
                this.uniform.set(this.floatValues);
                return;
            }
            switch (this.intValues.length) {
                case 1 -> this.uniform.set(this.intValues[0]);
                case 2 -> this.uniform.set(this.intValues[0], this.intValues[1]);
                case 3 -> this.uniform.set(this.intValues[0], this.intValues[1], this.intValues[2]);
                case 4 -> this.uniform.set(
                        this.intValues[0], this.intValues[1], this.intValues[2], this.intValues[3]);
                default -> throw new IllegalStateException("Unsupported shader integer uniform width");
            }
        }
    }
}
