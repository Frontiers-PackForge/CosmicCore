package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

// This multi should never be rotated on its side
// so it uses a static location for the star as the controller should never be rotated on its side
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarBallastRender extends DynamicRender<IrisMultiblockMachine, StarBallastRender> {

    public static final StarBallastRender INSTANCE = new StarBallastRender();
    public static final Codec<StarBallastRender> CODEC = Codec.unit(StarBallastRender.INSTANCE);
    public static final DynamicRenderType<IrisMultiblockMachine, StarBallastRender> TYPE = new DynamicRenderType<>(
            StarBallastRender.CODEC);

    public static final ResourceLocation STAR_MODEL_CORE = CosmicCore.id("block/iris/star_sphere");
    public static final ResourceLocation STAR_MODEL_OUTER = CosmicCore.id("block/iris/star_sphere_outer");
    public static final ResourceLocation STAR_MODEL_INNER = CosmicCore.id("block/iris/star_sphere_inner");
    public static final ResourceLocation STAR_MODEL_BEAM = CosmicCore.id("block/iris/star_beam");

    private static final RandomSource random = RandomSource.create(0L);
    private static BakedModel starCoreModel = null;
    private static BakedModel outerStarSphereModel = null;
    private static BakedModel innerStarSphereModel = null;
    private static BakedModel starBeamModel = null;

    private StarBallastRender() {
        ModelUtils.registerBakeEventListener(event -> {
            starCoreModel = event.getModels().get(STAR_MODEL_CORE);
            outerStarSphereModel = event.getModels().get(STAR_MODEL_OUTER);
            innerStarSphereModel = event.getModels().get(STAR_MODEL_INNER);
            starBeamModel = event.getModels().get(STAR_MODEL_BEAM);
        });
    }

    @Override
    public DynamicRenderType<IrisMultiblockMachine, StarBallastRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(IrisMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(IrisMultiblockMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance(), 16, getViewDistance());
    }

    @Override
    public void render(IrisMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) return;

        float totalTick = (Minecraft.getInstance().level.getGameTime() + partialTick);
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());

        poseStack.pushPose();
        poseStack.translate(0.5f, -27.5f, 0.5f);

        renderStar(poseStack, consumer, totalTick, packedLight, packedOverlay);
        renderStarInsides(poseStack, consumer, totalTick, packedLight, packedOverlay);
        renderStarShell(poseStack, consumer, totalTick, packedLight, packedOverlay);
        renderStarBeam(poseStack, consumer, totalTick, packedLight, packedOverlay);

        poseStack.popPose();
    }

    public void renderStarBeam(PoseStack poseStack, VertexConsumer consumer,
                               float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.55f, 0.0f, 1f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 1);
        poseStack.mulPose(rot);
        // ??? what is this scaling, magic numbers galore
        poseStack.scale(75.6f, 3f, 5f);

        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = starBeamModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, 0.65f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    public void renderStar(PoseStack poseStack, VertexConsumer consumer,
                           float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.25f, 0.0f, 0f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 1f);
        poseStack.mulPose(rot);
        poseStack.scale(9.6f, 9.6f, 9.6f);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = starCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, 0.65f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    public void renderStarShell(PoseStack poseStack, VertexConsumer consumer,
                                float totalTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        Quaternionf rot = new Quaternionf()
                .rotateXYZ(0.65f, 0.0f, 0.35f)
                .rotateAxis(totalTick * Mth.TWO_PI / 80, 0f, 1f, 0f);
        poseStack.mulPose(rot);
        poseStack.scale(10.0f, 10.0f, 10.0f);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = outerStarSphereModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, 0.5f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }

    public void renderStarInsides(PoseStack poseStack, VertexConsumer consumer,
                                  float tick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1f, 0));
        poseStack.scale(9.85f, 9.85f, 9.85f);
        PoseStack.Pose pose = poseStack.last();

        List<BakedQuad> quads = innerStarSphereModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, 0.7f, packedLight, packedOverlay, false);
        }
        poseStack.popPose();
    }
}
