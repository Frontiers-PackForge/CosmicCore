package com.ghostipedia.cosmiccore.client.firmament;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.CosmicCoreClient;
import com.ghostipedia.cosmiccore.client.compat.IrisCompat;
import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.data.worldgen.firmament.FirmamentMiddleBandLayout;
import com.ghostipedia.cosmiccore.common.dimension.FirmamentDimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FirmamentWindCurrentRenderer {

    private static final int CURRENT_CELL = 24;
    private static final int CURRENT_RANGE = 192;
    private static final int CURRENT_SEGMENTS = 10;
    private static final int STORM_CELL = 48;
    private static final int STORM_RANGE = 260;
    private static final int STORM_SEGMENTS = 10;
    private static final int STORM_LAYER_SPACING = 20;
    private static final int UPDRAFT_CELL = 192;
    private static final int UPDRAFT_RANGE = 320;
    private static final int UPDRAFT_SEGMENTS = 18;
    private static final long CURRENT_TIME_WRAP_TICKS = 1_280L;
    private static final long CURRENT_DECORATION_SALT = 0x3C6EF372FE94F82BL;
    private static final long STORM_DECORATION_SALT = 0x510E527FADE682D1L;
    private static final long UPDRAFT_DECORATION_SALT = 0xA54FF53A5F1D36F1L;
    private static final float SHADERPACK_WIND_UV_OFFSET = 4096.0f;

    private FirmamentWindCurrentRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().equals(FirmamentDimension.KEY)) return;
        ShaderInstance shader = CosmicCoreClient.getFirmamentWindCurrentShader();
        if (!IrisCompat.shadersActive() && shader == null) return;

        var camera = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        try {
            poseStack.translate(-camera.x, -camera.y, -camera.z);
            Matrix4f matrix = poseStack.last().pose();
            MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
            RenderType renderType = CosmicCoreRenderTypes.firmamentWindCurrent();
            VertexConsumer consumer = buffers.getBuffer(renderType);
            drawStormBody(consumer, matrix, camera.x, camera.z);
            drawHorizontalCurrents(consumer, matrix, camera.x, camera.z);
            drawUpdrafts(consumer, matrix, camera.x, camera.z);

            if (shader != null) {
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
                var timeUniform = shader.getUniform("CurrentTime");
                if (timeUniform != null) {
                    timeUniform.set(currentTime(minecraft.level.getGameTime(), partialTick));
                }
                var cameraUniform = shader.getUniform("CameraPos");
                if (cameraUniform != null) {
                    cameraUniform.set((float) camera.x, (float) camera.y, (float) camera.z);
                }
            }
            buffers.endBatch(renderType);
        } finally {
            poseStack.popPose();
        }
    }

    private static void drawHorizontalCurrents(VertexConsumer consumer, Matrix4f matrix,
                                               double cameraX, double cameraZ) {
        int minCellX = Mth.floor((cameraX - CURRENT_RANGE) / CURRENT_CELL);
        int maxCellX = Mth.floor((cameraX + CURRENT_RANGE) / CURRENT_CELL);
        int minCellZ = Mth.floor((cameraZ - CURRENT_RANGE) / CURRENT_CELL);
        int maxCellZ = Mth.floor((cameraZ + CURRENT_RANGE) / CURRENT_CELL);
        for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
            for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                double centerX = (cellX + 0.5) * CURRENT_CELL;
                double centerZ = (cellZ + 0.5) * CURRENT_CELL;
                FirmamentMiddleBandLayout.WindCorridor current = FirmamentMiddleBandLayout.sampleWind(centerX, centerZ);
                if (current.strength() < 0.28) continue;

                long localSeed = FirmamentMiddleBandLayout.mix(CURRENT_DECORATION_SALT, cellX, cellZ);
                float phase = unit(localSeed, 0);
                float length = 56.0f + unit(localSeed, 9) * 28.0f;
                float width = 2.4f + (float) current.strength() * 3.8f;
                float centerY = (float) FirmamentMiddleBandLayout.MIDDLE_BAND_CENTER_Y +
                        (unit(localSeed, 21) - 0.5f) * 54.0f;
                drawCurrentCross(
                        consumer, matrix,
                        (float) centerX, centerY, (float) centerZ,
                        (float) current.directionX(), (float) current.directionZ(),
                        length, width, (float) current.strength(), phase);
            }
        }
    }

    private static void drawCurrentCross(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerY,
                                         float centerZ, float directionX, float directionZ, float length, float width,
                                         float strength, float phase) {
        float normalX = -directionZ;
        float normalZ = directionX;
        for (int segment = 0; segment < CURRENT_SEGMENTS; segment++) {
            float progress0 = segment / (float) CURRENT_SEGMENTS;
            float progress1 = (segment + 1) / (float) CURRENT_SEGMENTS;
            float along0 = (progress0 - 0.5f) * length;
            float along1 = (progress1 - 0.5f) * length;
            float sway0 = Mth.sin(progress0 * Mth.TWO_PI + phase * Mth.TWO_PI) * 1.7f;
            float sway1 = Mth.sin(progress1 * Mth.TWO_PI + phase * Mth.TWO_PI) * 1.7f;
            float rise0 = Mth.sin(progress0 * Mth.TWO_PI * 1.5f + phase * 5.1f) * 1.4f;
            float rise1 = Mth.sin(progress1 * Mth.TWO_PI * 1.5f + phase * 5.1f) * 1.4f;
            float x0 = centerX + directionX * along0 + normalX * sway0;
            float z0 = centerZ + directionZ * along0 + normalZ * sway0;
            float x1 = centerX + directionX * along1 + normalX * sway1;
            float z1 = centerZ + directionZ * along1 + normalZ * sway1;
            float y0 = centerY + rise0;
            float y1 = centerY + rise1;
            float u0 = phase * 31.0f + progress0 * length;
            float u1 = phase * 31.0f + progress1 * length;
            addRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    normalX * width, 0.0f, normalZ * width, u0, u1, strength, phase, false);
            addRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    0.0f, width * 0.72f, 0.0f, u0, u1, strength * 0.82f, phase + 0.37f, false);
        }
    }

    private static void drawStormBody(VertexConsumer consumer, Matrix4f matrix,
                                      double cameraX, double cameraZ) {
        int minCellX = Mth.floor((cameraX - STORM_RANGE) / STORM_CELL);
        int maxCellX = Mth.floor((cameraX + STORM_RANGE) / STORM_CELL);
        int minCellZ = Mth.floor((cameraZ - STORM_RANGE) / STORM_CELL);
        int maxCellZ = Mth.floor((cameraZ + STORM_RANGE) / STORM_CELL);
        int minLayer = Mth.ceil((float) FirmamentMiddleBandLayout.WIND_MIN_Y / STORM_LAYER_SPACING);
        int maxLayer = Mth.floor((float) FirmamentMiddleBandLayout.WIND_MAX_Y / STORM_LAYER_SPACING);
        for (int layer = minLayer; layer <= maxLayer; layer++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                    long salt = STORM_DECORATION_SALT ^ ((long) layer * 0x9E3779B97F4A7C15L);
                    long localSeed = FirmamentMiddleBandLayout.mix(salt, cellX, cellZ);
                    float prominence = stormProminence(unit(localSeed, 3));
                    if (prominence == 0.0f) continue;
                    float centerX = (cellX + 0.12f + unit(localSeed, 11) * 0.76f) * STORM_CELL;
                    float centerZ = (cellZ + 0.12f + unit(localSeed, 23) * 0.76f) * STORM_CELL;
                    float centerY = layer * STORM_LAYER_SPACING + (unit(localSeed, 35) - 0.5f) * 11.0f;
                    float envelope = (float) FirmamentMiddleBandLayout.stormEnvelope(centerY);
                    if (envelope <= 0.04f) continue;

                    FirmamentMiddleBandLayout.WindCorridor current = FirmamentMiddleBandLayout.sampleWind(centerX,
                            centerZ);
                    float angleOffset = (unit(localSeed, 47) - 0.5f) * 0.24f;
                    float cosine = Mth.cos(angleOffset);
                    float sine = Mth.sin(angleOffset);
                    float directionX = (float) current.directionX() * cosine -
                            (float) current.directionZ() * sine;
                    float directionZ = (float) current.directionX() * sine +
                            (float) current.directionZ() * cosine;
                    float strength = envelope * (0.52f + unit(localSeed, 7) * 0.18f +
                            (float) current.strength() * 0.28f) * Mth.lerp(prominence, 0.62f, 1.0f);
                    float length = (128.0f + unit(localSeed, 17) * 112.0f) *
                            Mth.lerp(prominence, 0.78f, 1.14f);
                    float width = (4.0f + unit(localSeed, 29) * 4.5f) *
                            Mth.lerp(prominence, 0.58f, 1.04f);
                    float phase = unit(localSeed, 41);
                    drawStormRibbon(consumer, matrix, centerX, centerY, centerZ,
                            directionX, directionZ, length, width, strength, phase, prominence);
                    if (prominence >= 0.88f) {
                        float normalX = -directionZ;
                        float normalZ = directionX;
                        float spread = width * 1.45f;
                        drawStormRibbon(consumer, matrix,
                                centerX + normalX * spread, centerY + width * 0.32f,
                                centerZ + normalZ * spread,
                                directionX, directionZ, length * 0.84f, width * 0.30f,
                                strength * 0.70f, phase + 0.19f, prominence * 0.68f);
                        drawStormRibbon(consumer, matrix,
                                centerX - normalX * spread, centerY - width * 0.26f,
                                centerZ - normalZ * spread,
                                directionX, directionZ, length * 0.76f, width * 0.24f,
                                strength * 0.62f, phase + 0.57f, prominence * 0.60f);
                    }
                }
            }
        }
    }

    private static void drawStormRibbon(VertexConsumer consumer, Matrix4f matrix,
                                        float centerX, float centerY, float centerZ,
                                        float directionX, float directionZ, float length, float width,
                                        float strength, float phase, float prominence) {
        float normalX = -directionZ;
        float normalZ = directionX;
        float opacity = 0.10f + prominence * 0.50f;
        for (int segment = 0; segment < STORM_SEGMENTS; segment++) {
            float progress0 = segment / (float) STORM_SEGMENTS;
            float progress1 = (segment + 1) / (float) STORM_SEGMENTS;
            float along0 = (progress0 - 0.5f) * length;
            float along1 = (progress1 - 0.5f) * length;
            float sway0 = Mth.sin(progress0 * Mth.TWO_PI * 1.15f + phase * 8.7f) * width * 0.38f;
            float sway1 = Mth.sin(progress1 * Mth.TWO_PI * 1.15f + phase * 8.7f) * width * 0.38f;
            float rise0 = Mth.sin(progress0 * Mth.TWO_PI * 1.42f + phase * 11.3f) * width * 0.52f;
            float rise1 = Mth.sin(progress1 * Mth.TWO_PI * 1.42f + phase * 11.3f) * width * 0.52f;
            float x0 = centerX + directionX * along0 + normalX * sway0;
            float z0 = centerZ + directionZ * along0 + normalZ * sway0;
            float x1 = centerX + directionX * along1 + normalX * sway1;
            float z1 = centerZ + directionZ * along1 + normalZ * sway1;
            float y0 = centerY + rise0;
            float y1 = centerY + rise1;
            float u0 = phase * 59.0f + progress0 * length;
            float u1 = phase * 59.0f + progress1 * length;
            float taper0 = stormTaper(progress0);
            float taper1 = stormTaper(progress1);
            addVariableRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    normalX * width * taper0, 0.0f, normalZ * width * taper0,
                    normalX * width * taper1, 0.0f, normalZ * width * taper1,
                    u0, u1, strength, phase, 0.5f, opacity);
            addVariableRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    0.0f, width * 0.68f * taper0, 0.0f,
                    0.0f, width * 0.68f * taper1, 0.0f,
                    u0, u1, strength * 0.82f, phase + 0.31f, 0.5f, opacity * 0.82f);
        }
    }

    private static void drawUpdrafts(VertexConsumer consumer, Matrix4f matrix,
                                     double cameraX, double cameraZ) {
        int minCellX = Mth.floor((cameraX - UPDRAFT_RANGE) / UPDRAFT_CELL);
        int maxCellX = Mth.floor((cameraX + UPDRAFT_RANGE) / UPDRAFT_CELL);
        int minCellZ = Mth.floor((cameraZ - UPDRAFT_RANGE) / UPDRAFT_CELL);
        int maxCellZ = Mth.floor((cameraZ + UPDRAFT_RANGE) / UPDRAFT_CELL);
        for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
            for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                long localSeed = FirmamentMiddleBandLayout.mix(UPDRAFT_DECORATION_SALT, cellX, cellZ);
                if (unit(localSeed, 0) < 0.74f) continue;
                float centerX = (cellX + 0.18f + unit(localSeed, 11) * 0.64f) * UPDRAFT_CELL;
                float centerZ = (cellZ + 0.18f + unit(localSeed, 23) * 0.64f) * UPDRAFT_CELL;
                FirmamentMiddleBandLayout.WindCorridor current = FirmamentMiddleBandLayout.sampleWind(centerX, centerZ);
                if (current.strength() < 0.38) continue;
                drawUpdraft(consumer, matrix, centerX, centerZ,
                        108.0f + unit(localSeed, 35) * 18.0f,
                        222.0f + unit(localSeed, 47) * 10.0f,
                        3.5f + (float) current.strength() * 3.0f,
                        (float) current.strength(), unit(localSeed, 7));
            }
        }
    }

    private static void drawUpdraft(VertexConsumer consumer, Matrix4f matrix, float centerX, float centerZ,
                                    float minY, float maxY, float radius, float strength, float phase) {
        for (int segment = 0; segment < UPDRAFT_SEGMENTS; segment++) {
            float progress0 = segment / (float) UPDRAFT_SEGMENTS;
            float progress1 = (segment + 1) / (float) UPDRAFT_SEGMENTS;
            float angle0 = progress0 * Mth.TWO_PI * 1.35f + phase * Mth.TWO_PI;
            float angle1 = progress1 * Mth.TWO_PI * 1.35f + phase * Mth.TWO_PI;
            float radial0 = radius * (0.72f + 0.28f * Mth.sin(progress0 * Mth.PI));
            float radial1 = radius * (0.72f + 0.28f * Mth.sin(progress1 * Mth.PI));
            float x0 = centerX + Mth.cos(angle0) * radial0;
            float z0 = centerZ + Mth.sin(angle0) * radial0;
            float x1 = centerX + Mth.cos(angle1) * radial1;
            float z1 = centerZ + Mth.sin(angle1) * radial1;
            float y0 = Mth.lerp(progress0, minY, maxY);
            float y1 = Mth.lerp(progress1, minY, maxY);
            float normalX = Mth.cos((angle0 + angle1) * 0.5f);
            float normalZ = Mth.sin((angle0 + angle1) * 0.5f);
            float width = 2.2f + 1.4f * Mth.sin(progress0 * Mth.PI);
            float u0 = phase * 43.0f + progress0 * (maxY - minY);
            float u1 = phase * 43.0f + progress1 * (maxY - minY);
            addRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    normalX * width, 0.0f, normalZ * width, u0, u1, strength, phase, true);
            addRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                    -normalZ * width, 0.0f, normalX * width, u0, u1,
                    strength * 0.78f, phase + 0.43f, true);
        }
    }

    private static void addRibbonQuad(VertexConsumer consumer, Matrix4f matrix,
                                      float x0, float y0, float z0, float x1, float y1, float z1,
                                      float offsetX, float offsetY, float offsetZ, float u0, float u1,
                                      float strength, float phase, boolean updraft) {
        addRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1, offsetX, offsetY, offsetZ,
                u0, u1, strength, phase, updraft ? 1.0f : 0.0f);
    }

    private static void addRibbonQuad(VertexConsumer consumer, Matrix4f matrix,
                                      float x0, float y0, float z0, float x1, float y1, float z1,
                                      float offsetX, float offsetY, float offsetZ, float u0, float u1,
                                      float strength, float phase, float mode) {
        float opacity = mode > 0.25f && mode < 0.75f ? 144.0f / 255.0f :
                mode >= 0.75f ? 148.0f / 255.0f : 124.0f / 255.0f;
        addVariableRibbonQuad(consumer, matrix, x0, y0, z0, x1, y1, z1,
                offsetX, offsetY, offsetZ, offsetX, offsetY, offsetZ,
                u0, u1, strength, phase, mode, opacity);
    }

    private static void addVariableRibbonQuad(VertexConsumer consumer, Matrix4f matrix,
                                              float x0, float y0, float z0, float x1, float y1, float z1,
                                              float offset0X, float offset0Y, float offset0Z,
                                              float offset1X, float offset1Y, float offset1Z,
                                              float u0, float u1, float strength, float phase, float mode,
                                              float opacity) {
        int red = Math.round(Mth.clamp(strength, 0.0f, 1.0f) * 255.0f);
        int green = Math.round((phase - Mth.floor(phase)) * 255.0f);
        int blue = Math.round(Mth.clamp(mode, 0.0f, 1.0f) * 255.0f);
        int alpha = Math.round(Mth.clamp(opacity, 0.0f, 1.0f) * 255.0f);
        addVertex(consumer, matrix, x0 - offset0X, y0 - offset0Y, z0 - offset0Z,
                u0, 0.0f, red, green, blue, alpha);
        addVertex(consumer, matrix, x0 + offset0X, y0 + offset0Y, z0 + offset0Z,
                u0, 1.0f, red, green, blue, alpha);
        addVertex(consumer, matrix, x1 + offset1X, y1 + offset1Y, z1 + offset1Z,
                u1, 1.0f, red, green, blue, alpha);
        addVertex(consumer, matrix, x1 - offset1X, y1 - offset1Y, z1 - offset1Z,
                u1, 0.0f, red, green, blue, alpha);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
                                  float u, float v, int red, int green, int blue, int alpha) {
        consumer.addVertex(matrix, x, y, z)
                .setUv(u, v + SHADERPACK_WIND_UV_OFFSET)
                .setColor(red, green, blue, alpha);
    }

    private static float unit(long value, int rotation) {
        return (float) ((Long.rotateRight(value, rotation) >>> 40) & 0xFFFFFFL) / 0xFFFFFF;
    }

    static float stormProminence(float sample) {
        if (sample < 0.34f) return 0.0f;
        if (sample < 0.78f) return Mth.lerp((sample - 0.34f) / 0.44f, 0.18f, 0.34f);
        if (sample < 0.96f) return Mth.lerp((sample - 0.78f) / 0.18f, 0.52f, 0.72f);
        return Mth.lerp((sample - 0.96f) / 0.04f, 0.88f, 1.0f);
    }

    private static float stormTaper(float progress) {
        return 0.08f + 0.92f * (float) Math.pow(Math.max(Mth.sin(progress * Mth.PI), 0.0f), 0.65);
    }

    static float currentTime(long gameTime, float partialTick) {
        return (Math.floorMod(gameTime, CURRENT_TIME_WRAP_TICKS) + partialTick) / 20.0f;
    }
}
