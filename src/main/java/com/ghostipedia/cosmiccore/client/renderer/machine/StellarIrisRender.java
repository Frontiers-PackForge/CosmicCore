package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Quaternionf;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.ghostipedia.cosmiccore.client.renderer.machine.StarBallastRender.random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StellarIrisRender extends DynamicRender<IrisMultiblockMachine, StellarIrisRender> {

    public static final StellarIrisRender INSTANCE = new StellarIrisRender();
    // Default star color - will be overridden by machine's customStarColor if set
    public static final String DEFAULT_STAR_COLOR = "#FFCC44"; // Golden yellow
    public static final Codec<StellarIrisRender> CODEC = Codec.unit(StellarIrisRender.INSTANCE);

    // Current hex color being used for rendering (set per-render from machine or default)
    private String hexColor = DEFAULT_STAR_COLOR;
    public static final DynamicRenderType<IrisMultiblockMachine, StellarIrisRender> TYPE = new DynamicRenderType<>(
            StellarIrisRender.CODEC);

    public static final ResourceLocation IRIS_MODEL_CORE = CosmicCore.id("block/iris/iris_sphere");
    public static final ResourceLocation IRIS_MODEL_RING = CosmicCore.id("block/iris/iris_ring");
    public static final ResourceLocation IRIS_MODEL_RING_WHITE = CosmicCore.id("block/iris/iris_ring_white");
    public static final ResourceLocation STAR_MODEL_CORE = CosmicCore.id("block/iris/star_sphere");
    public static final ResourceLocation STAR_MODEL_OUTER = CosmicCore.id("block/iris/star_sphere_outer");
    public static final ResourceLocation STAR_MODEL_INNER = CosmicCore.id("block/iris/star_sphere_inner");
    IrisMultiblockMachine.Stage newStage;
    private static BakedModel irisCoreModel = null;
    private static BakedModel irisRingModel = null;
    private static BakedModel irisSmallRingModel = null;
    private static BakedModel starCoreModel = null;
    private static BakedModel outerStarSphereModel = null;
    private static BakedModel innerStarSphereModel = null;

    private StellarIrisRender() {
        ModelUtils.registerBakeEventListener(true, event -> {
            irisCoreModel = event.getModels().get(IRIS_MODEL_CORE);
            irisRingModel = event.getModels().get(IRIS_MODEL_RING);
            irisSmallRingModel = event.getModels().get(IRIS_MODEL_RING_WHITE);

            starCoreModel = event.getModels().get(STAR_MODEL_CORE);
            outerStarSphereModel = event.getModels().get(STAR_MODEL_OUTER);
            innerStarSphereModel = event.getModels().get(STAR_MODEL_INNER);
        });
    }

    @Override
    public DynamicRenderType<IrisMultiblockMachine, StellarIrisRender> getType() {
        return TYPE;
    }

    /**
     * Gets the star color from the machine, converting from int to hex string.
     * Returns DEFAULT_STAR_COLOR if machine has no custom color set (-1).
     */
    private String getStarColorFromMachine(IrisMultiblockMachine machine) {
        int customColor = machine.getCustomStarColor();
        if (customColor == -1) {
            return DEFAULT_STAR_COLOR;
        }
        // Convert int color (0xRRGGBB) to hex string (#RRGGBB)
        return String.format("#%06X", customColor & 0xFFFFFF);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(IrisMultiblockMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        // if (!machine.isFormed()) return;

        // Set the star color from the machine's custom color (or default)
        this.hexColor = getStarColorFromMachine(machine);

        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        poseStack.pushPose();

        // Controller is now on top spire - star renders 23 blocks below controller
        // Center offset: controller is at top, star center is 23 blocks down
        float x0ffset = 0.5f;
        float y0ffset = -23f;
        float z0ffset = 0.5f;

        poseStack.translate(x0ffset, y0ffset, z0ffset);
        poseStack.scale(7.0f, 7, 7);

        if (machine.getStage() == IrisMultiblockMachine.Stage.STAR) {

            renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.GROWING) {
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotateAxis(-totalTick * Mth.TWO_PI / 80f, 0, 1, 0));
            renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

            renderMultiStarSystemRandomized(
                    machine, poseStack, buffer, totalTick, packedLight, packedOverlay,
                    10,
                    4.5f,
                    0.75f,
                    5f,
                    0.12f, 0.45f,
                    true);
            poseStack.popPose();
        } else if (machine.getStage() == IrisMultiblockMachine.Stage.SUPERSTAR) {
            poseStack.scale(2, 2, 2);
            renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.BLACK_HOLE) {
            renderIris(poseStack, consumer, packedLight, packedOverlay);
            renderAccretionDisk(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

            renderRingSmall(machine, poseStack, consumer, totalTick, packedLight, packedOverlay);

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.DEATH) {
            renderRings(machine.getFrontFacing().getAxis(), totalTick, poseStack, buffer);
            renderRingsSecondary(machine.getFrontFacing().getAxis(), totalTick, poseStack, buffer);
            float scale = erraticPulseEffect(0.7f, 1.6f, partialTick, 0.3f, machine);
            poseStack.scale(scale, scale, scale);
            renderIris(poseStack, consumer, packedLight, packedOverlay);
            poseStack.popPose();

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.DEATH_GRACEFUL) {
            BlockPos pos = machine.getPos();

            if (!irisFadeStartSec.containsKey(pos)) {
                PoseStack.Pose pose = poseStack.last();
                java.util.List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
                for (BakedQuad quad : quads) {
                    consumer.putBulkData(pose, quad, 0f, 0f, 0f, 1f, packedLight, packedOverlay, false);
                }

                startIrisFade(pos, partialTick);
            } else {
                boolean vanished = renderIrisFading(
                        poseStack, consumer, packedLight, packedOverlay,
                        pos, partialTick);
                if (vanished) {
                    machine.setStage(IrisMultiblockMachine.Stage.STAR);
                }
            }
            poseStack.popPose();
        } else {
            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public boolean shouldRenderOffScreen(IrisMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(IrisMultiblockMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance(), 16, getViewDistance());
    }

    @OnlyIn(Dist.CLIENT)
    public void renderIris(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.0f, 0.0f, 0.0f, packedLight, packedOverlay);
        }
    }

    private final java.util.Map<BlockPos, Float> irisFadeStartSec = new java.util.HashMap<>();
    private static final float IRIS_FADE_DURATION_SEC = 10f;

    private void startIrisFade(BlockPos pos, float partialTick) {
        float tSec = (Minecraft.getInstance().player.tickCount + partialTick) / 20.0f;
        irisFadeStartSec.put(pos, tSec);
    }

    private static float clamp01(float x) {
        return x < 0f ? 0f : (x > 1f ? 1f : x);
    }

    private static float easeOutQuint(float x) {
        x = clamp01(x);
        float inv = 1f - x;
        return 1f - inv * inv * inv * inv * inv;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean renderIrisFading(PoseStack poseStack, VertexConsumer consumer,
                                     int packedLight, int packedOverlay,
                                     BlockPos pos, float partialTick) {
        Float t0 = irisFadeStartSec.get(pos);
        if (t0 == null) {
            PoseStack.Pose pose = poseStack.last();
            java.util.List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
            for (BakedQuad quad : quads) {
                consumer.putBulkData(pose, quad, 0, 0, 0, 1f, packedLight, packedOverlay, false);
            }
            return false;
        }

        float tSec = (Minecraft.getInstance().player.tickCount + partialTick) / 20.0f;
        float elapsed = tSec - t0;
        if (elapsed >= IRIS_FADE_DURATION_SEC) {
            irisFadeStartSec.remove(pos);
            return true;
        }

        float k = clamp01(elapsed / IRIS_FADE_DURATION_SEC);
        float scale = 0.02f + (1f - easeOutQuint(k)) * 0.98f;
        float alpha = (1f - k) * (1f - k);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        PoseStack.Pose pose = poseStack.last();
        java.util.List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0, 0, 0, alpha, packedLight, packedOverlay, false);
        }
        poseStack.popPose();

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRing(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);

        List<BakedQuad> quads = irisRingModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    /**
     * Renders the main accretion disk with rotation around the black hole.
     */
    @OnlyIn(Dist.CLIENT)
    public void renderAccretionDisk(PoseStack poseStack, VertexConsumer consumer,
                                    float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Offset slightly down to avoid z-fighting with the fast spinning ring
        poseStack.translate(0, -0.05f, 0);

        // Slow rotation around Y axis (orbital motion)
        float rotationSpeed = totalTick * Mth.TWO_PI / 200f; // Full rotation every 10 seconds
        poseStack.mulPose(new Quaternionf().rotateY(rotationSpeed));

        poseStack.scale(2.0f, 2.0f, 2.0f);

        List<BakedQuad> quads = irisRingModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderRingSmall(IrisMultiblockMachine machine, PoseStack poseStack, VertexConsumer consumer,
                                float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Controller is now on top spire - ring renders at star center (23 blocks below controller)
        float x0ffset = 0.5f;
        float y0ffset = -23f;
        float z0ffset = 0.5f;

        poseStack.translate(x0ffset, y0ffset, z0ffset);
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 20, 0, 1, 0));
        poseStack.scale(13.0f, 13.0f, 13.0f);

        List<BakedQuad> quads = irisSmallRingModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderStarInsides(PoseStack poseStack, VertexConsumer consumer,
                                  float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.65f, 0.0f, 0.35f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 0f);
        poseStack.mulPose(rot);
        poseStack.scale(1.05f, 1.05f, 1.05f);
        float[] c = hexToRgba(hexColor);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = innerStarSphereModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, c[0], c[1], c[2], 0.5f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderStarAt(PoseStack poseStack, VertexConsumer consumer,
                              float totalTick, int packedLight, int packedOverlay,
                              float sizeMul, String tempHexColor, boolean spinSelf) {
        String old = this.hexColor;
        this.hexColor = tempHexColor != null ? tempHexColor : old;

        float spinTick = spinSelf ? totalTick : 0f;

        poseStack.pushPose();
        poseStack.scale(sizeMul, sizeMul, sizeMul);
        renderStar(poseStack, consumer, spinTick, packedLight, packedOverlay);
        renderStarInsides(poseStack, consumer, spinTick, packedLight, packedOverlay);
        renderStarShell(poseStack, consumer, spinTick, packedLight, packedOverlay);
        poseStack.popPose();

        this.hexColor = old;
    }

    @OnlyIn(Dist.CLIENT)
    private void renderMultiStarSystemRandomized(IrisMultiblockMachine machine,
                                                 PoseStack poseStack, MultiBufferSource buffer,
                                                 float totalTick, int packedLight, int packedOverlay,
                                                 int count,
                                                 float meanRadius,
                                                 float radiusJitter,
                                                 float periodSec,
                                                 float starMin, float starMax,
                                                 boolean spinSelf) {
        if (count < 1) return;
        if (count > 5) count = 5;

        long seed = hashPos(machine.getPos()) ^ (count * 0x9E3779B97F4A7C15L);
        long[] S = new long[] { seed };

        String[] palette = new String[] { "#ffd28a", "#9ad0ff", "#ff9fb0", "#fff6a4", "#b4ffea", "#d2a0ff" };
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());

        float innerRadius = 2.5f;
        float outerRadius = meanRadius * 0.40f;

        float maxStarSize = starMax;
        float minSpacing = maxStarSize * 1.8f;

        float totalSpace = outerRadius - innerRadius;
        float neededSpace = minSpacing * (count - 1);
        float radiusStep = Math.max(minSpacing, totalSpace / Math.max(1, count - 1));

        float tSec = totalTick / 20.0f;

        for (int i = 0; i < count; i++) {
            float radius = innerRadius + (i * radiusStep);

            float orbitTilt = (rand01(S) - 0.5f) * Mth.PI * 0.8f;
            float orbitRotation = rand01(S) * Mth.TWO_PI;

            float direction = rand01(S) > 0.5f ? 1f : -1f;

            float speedMult = (0.3f + rand01(S) * 2.2f) * direction;

            float startAngle = rand01(S) * Mth.TWO_PI;

            float orbitalPeriod = periodSec * (1.0f / Math.abs(speedMult));
            float angle = startAngle + (tSec / orbitalPeriod) * Mth.TWO_PI * Math.signum(speedMult);

            float orbitX = radius * Mth.cos(angle);
            float orbitY = 0f;
            float orbitZ = radius * Mth.sin(angle);

            float cosTilt = Mth.cos(orbitTilt);
            float sinTilt = Mth.sin(orbitTilt);
            float rotX1 = orbitX;
            float rotY1 = orbitY * cosTilt - orbitZ * sinTilt;
            float rotZ1 = orbitY * sinTilt + orbitZ * cosTilt;

            float cosRot = Mth.cos(orbitRotation);
            float sinRot = Mth.sin(orbitRotation);
            float finalX = rotX1 * cosRot + rotZ1 * sinRot;
            float finalY = rotY1;
            float finalZ = -rotX1 * sinRot + rotZ1 * cosRot;

            float sizeRand = rand01(S);
            float starSize = Mth.lerp(sizeRand, starMin, starMax);
            starSize = Mth.clamp(starSize, starMin, starMax);

            String color = palette[i % palette.length];

            float spinSpeed = 0.5f + rand01(S) * 2.0f;
            float spinOffset = rand01(S) * 1000f;
            float uniqueSpinTick = spinSelf ? (totalTick * spinSpeed + spinOffset) : 0f;

            poseStack.pushPose();
            poseStack.translate(finalX, finalY, finalZ);
            renderStarAt(poseStack, consumer, uniqueSpinTick, packedLight, packedOverlay,
                    starSize, color, spinSelf);
            poseStack.popPose();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderStar(PoseStack poseStack, VertexConsumer consumer,
                           float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.65f, 0.0f, 0.35f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 0f);
        poseStack.mulPose(rot);
        poseStack.scale(1.03f, 1.03f, 1.03f);
        float[] c = hexToRgba(hexColor);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = starCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, c[0], c[1], c[2], 0.98f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderStarShell(PoseStack poseStack, VertexConsumer consumer,
                                float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.65f, 0.0f, 0.35f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 0f);
        poseStack.mulPose(rot);
        poseStack.scale(1.09f, 1.09f, 1.09f);
        float[] c = hexToRgba(hexColor);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = outerStarSphereModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, c[0], c[1], c[2], 1f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    private float prevTSec = Float.NaN;
    private float spikeEnv = 0f;
    private float nextSpikeT = 0f;
    private long pulseSeed = 0L;
    private boolean seedInit = false;

    private static long hashPos(BlockPos p) {
        long x = p.getX(), y = p.getY(), z = p.getZ();
        long h = x * 0x9E3779B97F4A7C15L ^ (y + 0xC2B2AE3D27D4EB4FL) ^ (z * 0x94D049BB133111EBL);
        h ^= (h >>> 30);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 27);
        h *= 0x94D049BB133111EBL;
        h ^= (h >>> 31);
        return h;
    }

    private static final long A = 6364136223846793005L, C = 1442695040888963407L;

    private static long lcg(long s) {
        return s * A + C;
    }

    private static float rand01(long[] s) {
        s[0] = lcg(s[0]);
        return ((s[0] >>> 8) & 0xFFFFFF) / (float) (1 << 24);
    }

    private static float expSample(long[] s, float lambda) {
        float u = Math.max(1e-6f, rand01(s));
        return (float) (-Math.log(u) / lambda);
    }

    @OnlyIn(Dist.CLIENT)
    private float erraticPulseEffect(float min, float max, float partial, float intensity,
                                     IrisMultiblockMachine machine) {
        float tSec = (Minecraft.getInstance().player.tickCount + partial) / 20.0f;

        if (!seedInit) {
            pulseSeed = hashPos(machine.getPos());
            seedInit = true;
            nextSpikeT = tSec + 0.2f;
        }

        float dt;
        if (Float.isNaN(prevTSec)) dt = 0f;
        else {
            dt = tSec - prevTSec;
            if (dt < 0f) dt = 0f;
            if (dt > 0.25f) dt = 0.25f;
        }
        prevTSec = tSec;

        intensity = Mth.clamp(intensity, 0f, 1f);
        float rateHz = Mth.lerp(intensity, 0.3f, 3.0f);
        float gain = Mth.lerp(intensity, 0.25f, 0.9f);
        float decayTau = Mth.lerp(intensity, 0.60f, 0.20f);

        if (dt > 0f) {
            long[] s = new long[] { pulseSeed };
            while (tSec >= nextSpikeT) {
                float amp = (0.5f + 0.5f * rand01(s)) * gain;
                spikeEnv += amp;
                float inter = expSample(s, rateHz);
                nextSpikeT += inter;
                pulseSeed = s[0];
            }

            float decay = (float) Math.exp(-dt / decayTau);
            spikeEnv *= decay;
        }

        float w = tSec;
        float jitter = 0.04f * (float) Math.sin(7.23 * w + 0.3) + 0.03f * (float) Math.sin(11.1 * w + 1.7) +
                0.02f * (float) Math.sin(4.7 * w * w + 0.5);
        jitter = Mth.clamp(jitter, -0.15f, 0.15f);

        float pulse01 = Mth.clamp(0.12f + spikeEnv + jitter, 0f, 1f);

        return Mth.lerp(pulse01, min, max);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRings(Direction.Axis upAxis, float totalTick, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        float xRot = totalTick / 15;
        float zRot = Mth.HALF_PI + totalTick / 30;
        float yRot = totalTick / 20;
        float sinX = Mth.sin(xRot), cosX = Mth.cos(xRot);
        float sinY = Mth.sin(yRot), cosY = Mth.cos(yRot);
        float sinZ = Mth.sin(zRot), cosZ = Mth.cos(zRot);

        float min = 0.5f;
        float max = 1.0f;
        float amplitude = (max - min) / 2.0f;
        float offset = min + amplitude;
        float scale = (float) Math.sin(totalTick * 0.1f) * amplitude + offset;

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(sinX, cosY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                6.3f, 0.3F, 10, 36,
                0F, 0, 0F, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                5.75f, 0.3F, 10, 36,
                0F, 0, 0F, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosZ, -sinY, 0));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                3.75f, 0.3F, 10, 36,
                0F, 0, 0F, 1, upAxis);
        poseStack.popPose();
    }

    private void renderRingsSecondary(Direction.Axis upAxis, float totalTick, PoseStack poseStack,
                                      MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        float xRot = totalTick / 17f;
        float zRot = Mth.HALF_PI + 0.35f + totalTick / 29f;
        float yRot = totalTick / 21f;
        float sinX = Mth.sin(xRot), cosX = Mth.cos(xRot);
        float sinY = Mth.sin(yRot), cosY = Mth.cos(yRot);
        float sinZ = Mth.sin(zRot), cosZ = Mth.cos(zRot);

        float min = 0.45f;
        float max = 1.15f;
        float amplitude = (max - min) / 2.0f;
        float offset = min + amplitude;
        float scale = (float) Math.sin(totalTick * 0.123f + 1.1f) * amplitude + offset;

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(sinX, cosY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                5.10f + 0.25f * Mth.sin(totalTick * 0.9f + 0.3f),
                0.26F + 0.06F * Mth.sin(totalTick * 0.291f + 1.7f),
                12, 40,
                0F, 0, 0F,
                1,
                upAxis);
        poseStack.scale(scale * 2.5f, scale * 3.2f, scale * 2.7f);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                4.55f + 0.30f * Mth.sin(totalTick * 0.377f + 0.9f),
                0.34F + 0.05F * Mth.sin(totalTick * 0.291f + 1.6f),
                11, 30,
                0F, 0, 0F,
                1,
                upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateZ(cosZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                5.05f + 0.22f * Mth.sin(totalTick * 0.065f + 1.9f),
                0.28F + 0.05F * Mth.sin(totalTick * 0.15f + 0.2f),
                13, 32,
                0F, 0, 0F,
                1,
                upAxis);
        poseStack.popPose();
    }

    public static float[] hexToRgba(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        int r, g, b, a;

        if (hex.length() == 6) {
            r = Integer.parseInt(hex.substring(0, 2), 16);
            g = Integer.parseInt(hex.substring(2, 4), 16);
            b = Integer.parseInt(hex.substring(4, 6), 16);
            a = 255;
        } else if (hex.length() == 8) {
            r = Integer.parseInt(hex.substring(0, 2), 16);
            g = Integer.parseInt(hex.substring(2, 4), 16);
            b = Integer.parseInt(hex.substring(4, 6), 16);
            a = Integer.parseInt(hex.substring(6, 8), 16);
        } else {
            throw new IllegalArgumentException("Hex must be in format #RRGGBB or #RRGGBBAA");
        }

        return new float[] {
                r / 255.0f,
                g / 255.0f,
                b / 255.0f,
                a / 255.0f
        };
    }
}
