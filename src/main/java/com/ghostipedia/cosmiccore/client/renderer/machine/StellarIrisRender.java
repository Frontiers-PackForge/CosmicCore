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
    public String hexColor = "#a262e3";
    public static final Codec<StellarIrisRender> CODEC = Codec.unit(StellarIrisRender.INSTANCE);
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

            // Todo : Figure out why these don't render the ball.
            starCoreModel = event.getModels().get(STAR_MODEL_CORE);
            outerStarSphereModel = event.getModels().get(STAR_MODEL_OUTER);
            innerStarSphereModel = event.getModels().get(STAR_MODEL_INNER);
        });
    }

    @Override
    public DynamicRenderType<IrisMultiblockMachine, StellarIrisRender> getType() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(IrisMultiblockMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.isFormed()) return;

        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());
        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();

        float x0ffset = 0, y0ffset = -2.5f, z0ffset = 0;

        if (front.getAxis() == Direction.Axis.X) {
            if (front.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                x0ffset = -45.5f;
                z0ffset = 0.5f;
            } else {
                x0ffset = 46.5f;
                z0ffset = 0.5f;
            }
        }

        if (front.getAxis() == Direction.Axis.Z) {
            if (front.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                z0ffset = -45.55f;
                x0ffset = 0.5f;
            } else {
                z0ffset = 46.5f;
                x0ffset = 0.5f;
            }
        }

        poseStack.translate(x0ffset, y0ffset, z0ffset);
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 80, 0, 1, 0));
        poseStack.scale(10.0f, 10.0f, 10.0f);

        if (machine.getStage() == IrisMultiblockMachine.Stage.STAR) {
            renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.SUPERSTAR) {
            poseStack.scale(2, 2, 2);
            renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
            renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
            poseStack.popPose();

        } else if (machine.getStage() == IrisMultiblockMachine.Stage.BLACK_HOLE) {
            renderIris(poseStack, consumer, packedLight, packedOverlay);
            renderRing(poseStack, consumer, packedLight, packedOverlay);
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
                // First frame: draw fully visible and start fade timer
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
                    machine.setStage(IrisMultiblockMachine.Stage.STAR); // TODO: This does NOT set the stage of the
                                                                        // multi, it sets the render stage to star which
                                                                        // desyncs it figure out how to set the MACHINES
                                                                        // STATE and set BOTH to EMPTY! ! !
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

    // Start time per machine position, in seconds
    private final java.util.Map<BlockPos, Float> irisFadeStartSec = new java.util.HashMap<>();
    private static final float IRIS_FADE_DURATION_SEC = 10f;

    // Call this once to begin the fading shrink
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
        poseStack.scale(scale, scale, scale); // shrink while fading
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

    @OnlyIn(Dist.CLIENT)
    public void renderRingSmall(IrisMultiblockMachine machine, PoseStack poseStack, VertexConsumer consumer,
                                float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        float x0ffset = 0, y0ffset = -2.3f, z0ffset = 0;

        if (front.getAxis() == Direction.Axis.X) {
            if (front.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                x0ffset = -46.5f;
                z0ffset = -0.5f;
            } else {
                x0ffset = 46.5f;
                z0ffset = 0.5f;
            }
        }

        if (front.getAxis() == Direction.Axis.Z) {
            if (front.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                z0ffset = -46.5f;
                x0ffset = -0.5f;
            } else {
                z0ffset = 46.5f;
                x0ffset = 0.5f;
            }
        }
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

    // Deterministic hash (like xorshift) to make a seed from block pos
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

    // Simple LCG for reproducible floats [0,1)
    private static final long A = 6364136223846793005L, C = 1442695040888963407L;

    private static long lcg(long s) {
        return s * A + C;
    }

    private static float rand01(long[] s) {
        s[0] = lcg(s[0]);
        return ((s[0] >>> 8) & 0xFFFFFF) / (float) (1 << 24);
    }

    // Exponential RNG with mean = 1/lambda (Poisson inter-arrival); clamp U to avoid log(0)
    private static float expSample(long[] s, float lambda) {
        float u = Math.max(1e-6f, rand01(s));
        return (float) (-Math.log(u) / lambda);
    }

    @OnlyIn(Dist.CLIENT)
    private float erraticPulseEffect(float min, float max, float partial, float intensity,
                                     IrisMultiblockMachine machine) {
        // absolute time in seconds
        float tSec = (Minecraft.getInstance().player.tickCount + partial) / 20.0f;

        // seed once per machine so multiple instances don't sync - realistically you never have more than one
        if (!seedInit) {
            pulseSeed = hashPos(machine.getPos());
            seedInit = true;
            nextSpikeT = tSec + 0.2f; // first spike soon-ish
        }

        // real dt (handles multi-pass calls where time didn't advance)
        float dt;
        if (Float.isNaN(prevTSec)) dt = 0f;
        else {
            dt = tSec - prevTSec;
            if (dt < 0f) dt = 0f;
            if (dt > 0.25f) dt = 0.25f; // clamp long stalls
        }
        prevTSec = tSec;

        // Map intensity -> spike rate and strength
        intensity = Mth.clamp(intensity, 0f, 1f);
        float rateHz = Mth.lerp(intensity, 0.3f, 3.0f);
        float gain = Mth.lerp(intensity, 0.25f, 0.9f);
        float decayTau = Mth.lerp(intensity, 0.60f, 0.20f);

        // Drive Poisson spike train
        if (dt > 0f) {
            long[] s = new long[] { pulseSeed };
            while (tSec >= nextSpikeT) {
                float amp = (0.5f + 0.5f * rand01(s)) * gain;
                spikeEnv += amp;
                float inter = expSample(s, rateHz);
                nextSpikeT += inter;
                pulseSeed = s[0];
            }

            // exponential decay of envelope
            float decay = (float) Math.exp(-dt / decayTau);
            spikeEnv *= decay;
        }

        float w = tSec;
        float jitter = 0.04f * (float) Math.sin(7.23 * w + 0.3) + 0.03f * (float) Math.sin(11.1 * w + 1.7) +
                0.02f * (float) Math.sin(4.7 * w * w + 0.5); // slight chaos
        jitter = Mth.clamp(jitter, -0.15f, 0.15f);

        // Combine: base floor + spikes + jitter, then clamp 0..1
        float pulse01 = Mth.clamp(0.12f + spikeEnv + jitter, 0f, 1f);

        // Map to [min,max]
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
        poseStack.scale(3, 3, 3);
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
                5.10f + 0.25f * Mth.sin(totalTick * 0.9f + 0.3f),   // radius breath
                0.26F + 0.06F * Mth.sin(totalTick * 0.291f + 1.7f),  // thickness breath
                12, 40,
                0F, 0, 0F,
                1,  // alpha breath
                upAxis);
        poseStack.scale(scale * 2.5f, scale * 3.2f, scale * 2.7f); // tie scale to osc (ring 1)
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                4.55f + 0.30f * Mth.sin(totalTick * 0.377f + 0.9f), // different rhythm
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
                5.05f + 0.22f * Mth.sin(totalTick * 0.065f + 1.9f), // third rhythm
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
