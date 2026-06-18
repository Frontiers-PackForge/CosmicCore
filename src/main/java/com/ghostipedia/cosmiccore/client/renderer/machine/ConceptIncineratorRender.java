package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.VoraxReactorMachine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Quaternionf;

import java.util.List;

import static com.ghostipedia.cosmiccore.client.renderer.machine.StarBallastRender.random;

public class ConceptIncineratorRender extends
                                      DynamicRender<WorkableElectricMultiblockMachine, ConceptIncineratorRender> {

    public static final ConceptIncineratorRender INSTANCE = new ConceptIncineratorRender();
    public static final Codec<ConceptIncineratorRender> CODEC = Codec.unit(ConceptIncineratorRender.INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, ConceptIncineratorRender> TYPE = new DynamicRenderType<>(
            ConceptIncineratorRender.CODEC);

    public static final ResourceLocation IRIS_MODEL_CORE = CosmicCore.id("block/iris/iris_sphere");
    public static final ResourceLocation IRIS_MODEL_RING = CosmicCore.id("block/iris/iris_ring");
    public static final ResourceLocation IRIS_MODEL_RING_WHITE = CosmicCore.id("block/iris/iris_ring_white");
    public static final ResourceLocation STAR_CORE = CosmicCore.id("block/iris/star_sphere");
    public static final ResourceLocation STAR_CORE_MIDDLE = CosmicCore.id("block/iris/star_sphere_inner");
    public static final ResourceLocation STAR_CORE_OUTER = CosmicCore.id("block/iris/star_sphere_outer");

    private static BakedModel irisCoreModel = null;
    private static BakedModel irisRingModel = null;
    private static BakedModel irisSmallRingModel = null;
    private static BakedModel irisLowStarModel = null;
    private static BakedModel irisMidStarModel = null;
    private static BakedModel irisOuterStarModel = null;

    private ConceptIncineratorRender() {
        ModelUtils.registerBakeEventListener(true, event -> {
            irisCoreModel = event.getModels().get(IRIS_MODEL_CORE);
            irisRingModel = event.getModels().get(IRIS_MODEL_RING);
            irisSmallRingModel = event.getModels().get(IRIS_MODEL_RING_WHITE);
            irisLowStarModel = event.getModels().get(STAR_CORE);
            irisMidStarModel = event.getModels().get(STAR_CORE_MIDDLE);
            irisOuterStarModel = event.getModels().get(STAR_CORE_OUTER);
        });
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, ConceptIncineratorRender> getType() {
        return TYPE;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!machine.isFormed()) return;

        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        // move the things:tm: to render at the center of the multiblock
        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        Direction up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        Direction back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();

        // translate to the absolute center of the multiblock
        float x0ffset = 0, y0ffset = 0, z0ffset = 0;

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int upOffset = up.getNormal().get(axis);
            int backOffset = back.getNormal().get(axis);

            float offset = upOffset * (4.0f + (upOffset * 0.5f)) +
                    backOffset * (5.0f + (backOffset * 0.5f));
            switch (axis) {
                case X -> x0ffset = offset;
                case Y -> y0ffset = offset;
                case Z -> z0ffset = offset;
            }
        }
        poseStack.translate(
                x0ffset + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                y0ffset + (leftAxis == Direction.Axis.Y ? 0.5f : 0.0f),
                z0ffset + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));
        poseStack.pushPose();
        float percent = 0;
        if (machine instanceof VoraxReactorMachine voraxReactorMachine) {
            percent = clamp((voraxReactorMachine.getContagionStrength() / 50000f) / 4.5f, 0F, 1F);
        } else {
            percent = 0;
        }

        float scale = heartBeatEffect(0.5f, 1.0f, partialTick, percent);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 80, 0, 1, 0));
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());

        renderBallA(poseStack, consumer, packedLight, packedOverlay, percent);

        poseStack.scale(scale * -2, scale * -2, scale * -2);

        consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());
        renderBallC(poseStack, consumer, packedLight, packedOverlay, percent);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    private float bpmFiltered = 50f;
    private float beatPhase01 = 0f;
    private float percentFiltered = 0f;
    private float prevTSec = Float.NaN;

    private static float smoothstep01(float x) {
        x = Mth.clamp(x, 0f, 1f);
        return x * x * (3f - 2f * x);
    }

    float biExp(float x, float center, float rise, float decay) {
        if (x < center) return 0f;
        float u = x - center;
        return (float) (Math.exp(-u / decay) - Math.exp(-u / rise));
    }

    public float heartBeatEffect(float min, float max, float partialTick, float percent) {
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);
        float tSec = totalTick / 20.0f;

        float dt;
        if (Float.isNaN(prevTSec)) {
            dt = 0f;
        } else {
            dt = tSec - prevTSec;
            if (dt < 0f) dt = 0f;
            if (dt > 0.2f) dt = 0.2f;
        }
        prevTSec = tSec;

        {
            float tauP = 0.25f;
            float alphaP = 1f - (float) Math.exp(-(dt <= 0f ? 0f : dt) / tauP);
            percentFiltered += (percent - percentFiltered) * alphaP;
        }

        float eased = smoothstep01(percentFiltered);
        float targetBpm = Mth.lerp(eased, 50f, 180f);

        float tauB = 0.35f;
        float alphaB = 1f - (float) Math.exp(-(dt <= 0f ? 0f : dt) / tauB);
        float bpmSmoothed = bpmFiltered + (targetBpm - bpmFiltered) * alphaB;

        float maxDeltaPerSec = 60f;
        float maxStep = maxDeltaPerSec * dt;
        float delta = Mth.clamp(bpmSmoothed - bpmFiltered, -maxStep, maxStep);
        bpmFiltered += delta;

        float bpm = Mth.clamp(bpmFiltered, 30f, 180f);
        float period = 60f / bpm;

        if (dt > 0f) {
            beatPhase01 += dt / period;
            beatPhase01 -= (int) beatPhase01;
        }

        float tInBeat = beatPhase01 * period;

        float c1 = 0.04f, c2 = 0.24f;

        float riseFrac = 0.02f, decayFrac1 = 0.18f, decayFrac2 = 0.20f;
        float p1 = biExp(tInBeat, c1 * period, riseFrac * period, decayFrac1 * period);
        float p2 = 0.6f * biExp(tInBeat, c2 * period, riseFrac * period, decayFrac2 * period);

        float heartbeat = (p1 + p2) * 1.25f;
        heartbeat = Mth.clamp(heartbeat, 0f, 1f);

        return Mth.lerp(heartbeat, min, max);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance(), 16, getViewDistance());
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBallA(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                            float percent) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.016F + (percent / 4), 0.094F, 0.125F, 1, packedLight, packedOverlay,
                    false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBallB(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.1f, 0.1f, 0.1f, packedLight, packedOverlay);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBallC(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                            float percent) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.2f + percent, 0.2f, 0.7f + -percent, 1, packedLight, packedOverlay,
                    false);
        }
    }

    public void renderRing(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);
        List<BakedQuad> quads = irisRingModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, 1, packedOverlay);
        }

        poseStack.popPose();
    }

    public void renderRingSmall(PoseStack poseStack, VertexConsumer consumer,
                                float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(2f, 2.0f, 2.0f);

        float min = 0.5f;
        float max = 2.0f;
        float amplitude = (max - min) / 2.0f;
        float offset = min + amplitude;
        float scale = (float) Math.sin(totalTick * 0.1f) * amplitude + offset;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 20, 0, 1, 0));
        List<BakedQuad> quads = irisSmallRingModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}
