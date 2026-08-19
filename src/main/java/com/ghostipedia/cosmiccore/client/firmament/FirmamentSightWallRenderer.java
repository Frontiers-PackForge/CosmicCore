package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.client.compat.IrisCompat;
import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;
import com.ghostipedia.cosmiccore.common.firmament.FirmamentEnvironment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FirmamentSightWallRenderer {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation
            .withDefaultNamespace("textures/misc/white.png");
    private static final RenderType FALLBACK_WALL_TYPE = RenderType.entityTranslucentEmissive(WHITE_TEXTURE);
    private static final int OCEAN_CURRENT_MARKER = 2;
    static final float SHADERPACK_OCEAN_UV_OFFSET = 67_108_864.0f;
    static final int SEA_Y = FirmamentEnvironment.AMMONIA_SEA_Y;
    private static final float HALF_EXTENT = 384.0f;
    private static final float GRID_STEP = 16.0f;
    private static final int GRID_CELLS = 50;
    private static final float GRID_HALF_EXTENT = GRID_STEP * GRID_CELLS * 0.5f;
    private static final int VISIBLE_RANGE = 256;
    private static final float VISIBILITY_FADE_START = 224.0f;
    private static final long STORM_TIME_WRAP_TICKS = 1_228_800L;
    private static final float FALLBACK_ALPHA = 0.78f;
    private static final StormLayer SURFACE_LAYER = new StormLayer(0.0f, 0.42f);
    private static final StormLayer MIDDLE_LAYER = new StormLayer(0.5f, 0.23f);
    private static final StormLayer DEEP_LAYER = new StormLayer(1.0f, 0.16f);
    private static final StormLayer[] HIGH_LAYER_ORDER = { DEEP_LAYER, MIDDLE_LAYER, SURFACE_LAYER };
    private static final StormLayer[] UPPER_MIDDLE_LAYER_ORDER = { DEEP_LAYER, SURFACE_LAYER, MIDDLE_LAYER };
    private static final StormLayer[] LOWER_MIDDLE_LAYER_ORDER = { SURFACE_LAYER, DEEP_LAYER, MIDDLE_LAYER };
    private static final StormLayer[] LOW_LAYER_ORDER = { SURFACE_LAYER, MIDDLE_LAYER, DEEP_LAYER };

    private FirmamentSightWallRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().equals(FirmamentDimension.KEY)) return;

        var camera = event.getCamera().getPosition();
        float passFade = seaVisibility(camera.y);
        if (passFade <= 0.01f) return;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = poseStack.last().pose();

        ShaderInstance shader = CosmicCoreClient.getFirmamentStormCurrentShader();
        if (IrisCompat.shadersActive() || shader != null) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
            float stormTime = stormTime(minecraft.level.getGameTime(), partialTick);
            renderStormWall(minecraft, matrix, camera.x, camera.y, camera.z, passFade, stormTime, shader);
        } else {
            renderFallbackWall(minecraft, matrix, camera.x, camera.z, passFade);
        }
        poseStack.popPose();
    }

    private static void renderStormWall(Minecraft minecraft, Matrix4f matrix, double cameraX, double cameraY,
                                        double cameraZ,
                                        float passFade, float stormTime, ShaderInstance shader) {
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType renderType = CosmicCoreRenderTypes.firmamentStormCurrent();
        VertexConsumer consumer = buffers.getBuffer(renderType);
        drawStormVolume(consumer, matrix, cameraX, cameraY, cameraZ, passFade, IrisCompat.shadersActive());

        if (shader != null) {
            var stormTimeUniform = shader.getUniform("StormTime");
            if (stormTimeUniform != null) {
                stormTimeUniform.set(stormTime);
            }
            var cameraUniform = shader.getUniform("CameraXZ");
            if (cameraUniform != null) {
                cameraUniform.set((float) cameraX, (float) cameraZ);
            }
            var cameraYUniform = shader.getUniform("CameraY");
            if (cameraYUniform != null) {
                cameraYUniform.set((float) cameraY);
            }
            var edgeRadiusUniform = shader.getUniform("EdgeRadius");
            if (edgeRadiusUniform != null) {
                edgeRadiusUniform.set(HALF_EXTENT);
            }
            var horizonPassUniform = shader.getUniform("HorizonPass");
            if (horizonPassUniform != null) {
                horizonPassUniform.set(0.0f);
            }
        }
        buffers.endBatch(renderType);
    }

    static float seaVisibility(double cameraY) {
        float distance = (float) Math.abs(cameraY - SEA_Y);
        if (distance >= VISIBLE_RANGE) return 0.0f;
        float progress = Mth.clamp(
                (distance - VISIBILITY_FADE_START) / (VISIBLE_RANGE - VISIBILITY_FADE_START), 0.0f, 1.0f);
        float fade = progress * progress * (3.0f - 2.0f * progress);
        return 1.0f - fade;
    }

    static float stormTime(long gameTime, float partialTick) {
        return ((gameTime % STORM_TIME_WRAP_TICKS) + partialTick) / 20.0f;
    }

    static float layerBase(float depth) {
        return -24.0f * depth + 5.0f * depth * depth;
    }

    static int layerOrder(double cameraY) {
        if (cameraY >= 18.625) return 0;
        if (cameraY >= 14.5) return 1;
        if (cameraY >= 9.125) return 2;
        return 3;
    }

    private static void drawStormVolume(VertexConsumer consumer, Matrix4f matrix, double cameraX, double cameraY,
                                        double cameraZ, float passFade, boolean shaderPackCarrier) {
        StormLayer[] layers = switch (layerOrder(cameraY)) {
            case 0 -> HIGH_LAYER_ORDER;
            case 1 -> UPPER_MIDDLE_LAYER_ORDER;
            case 2 -> LOWER_MIDDLE_LAYER_ORDER;
            default -> LOW_LAYER_ORDER;
        };
        for (StormLayer layer : layers) {
            drawStormLayer(consumer, matrix, cameraX, cameraZ, passFade, layer, shaderPackCarrier);
        }
    }

    private static void drawStormLayer(VertexConsumer consumer, Matrix4f matrix, double cameraX, double cameraZ,
                                       float passFade, StormLayer layer, boolean shaderPackCarrier) {
        float centerX = (float) (Math.floor(cameraX / GRID_STEP + 0.5) * GRID_STEP);
        float centerZ = (float) (Math.floor(cameraZ / GRID_STEP + 0.5) * GRID_STEP);
        float startX = centerX - GRID_HALF_EXTENT;
        float startZ = centerZ - GRID_HALF_EXTENT;
        int layerValue = Math.round(layer.depth() * 255.0f);
        int alpha = Math.round(layer.alpha() * passFade * 255.0f);
        for (int zCell = 0; zCell < GRID_CELLS; zCell++) {
            float z0 = startZ + zCell * GRID_STEP;
            float z1 = z0 + GRID_STEP;
            for (int xCell = 0; xCell < GRID_CELLS; xCell++) {
                float x0 = startX + xCell * GRID_STEP;
                float x1 = x0 + GRID_STEP;
                addStormVertex(consumer, matrix, x0, z0, layerValue, alpha, shaderPackCarrier);
                addStormVertex(consumer, matrix, x0, z1, layerValue, alpha, shaderPackCarrier);
                addStormVertex(consumer, matrix, x1, z1, layerValue, alpha, shaderPackCarrier);
                addStormVertex(consumer, matrix, x1, z0, layerValue, alpha, shaderPackCarrier);
            }
        }
    }

    private static void addStormVertex(VertexConsumer consumer, Matrix4f matrix, float x, float z,
                                       int layerValue, int alpha, boolean shaderPackCarrier) {
        consumer.addVertex(matrix, x, SEA_Y, z)
                .setUv(x + (shaderPackCarrier ? SHADERPACK_OCEAN_UV_OFFSET : 0.0f), z)
                .setColor(layerValue, 255, OCEAN_CURRENT_MARKER, alpha);
    }

    private static void renderFallbackWall(Minecraft minecraft, Matrix4f matrix, double cameraX, double cameraZ,
                                           float passFade) {
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(FALLBACK_WALL_TYPE);
        float x0 = (float) cameraX - HALF_EXTENT;
        float x1 = (float) cameraX + HALF_EXTENT;
        float z0 = (float) cameraZ - HALF_EXTENT;
        float z1 = (float) cameraZ + HALF_EXTENT;
        int alpha = Math.round(FALLBACK_ALPHA * passFade * 255.0f);
        addFallbackVertex(consumer, matrix, x0, z0, 0.0f, 0.0f, alpha);
        addFallbackVertex(consumer, matrix, x0, z1, 0.0f, 1.0f, alpha);
        addFallbackVertex(consumer, matrix, x1, z1, 1.0f, 1.0f, alpha);
        addFallbackVertex(consumer, matrix, x1, z0, 1.0f, 0.0f, alpha);
        buffers.endBatch(FALLBACK_WALL_TYPE);
    }

    private static void addFallbackVertex(VertexConsumer consumer, Matrix4f matrix, float x, float z,
                                          float u, float v, int alpha) {
        consumer.addVertex(matrix, x, SEA_Y, z)
                .setColor(7, 31, 78, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    private record StormLayer(float depth, float alpha) {}
}
