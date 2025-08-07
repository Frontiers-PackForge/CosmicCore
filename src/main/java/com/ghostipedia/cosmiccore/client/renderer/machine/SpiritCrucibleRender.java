package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
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

import java.util.EnumSet;
import java.util.List;

import static com.ghostipedia.cosmiccore.client.renderer.machine.StarBallastRender.random;

public class SpiritCrucibleRender extends DynamicRender<WorkableElectricMultiblockMachine, SpiritCrucibleRender> {

    public static final SpiritCrucibleRender INSTANCE = new SpiritCrucibleRender();
    public static final Codec<SpiritCrucibleRender> CODEC = Codec.unit(SpiritCrucibleRender.INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, SpiritCrucibleRender> TYPE = new DynamicRenderType<>(
            SpiritCrucibleRender.CODEC);

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, SpiritCrucibleRender> getType() {
        return TYPE;
    }

    public static final ResourceLocation IRIS_MODEL_CORE = CosmicCore.id("block/iris/iris_sphere");
    private static BakedModel irisCoreModel = null;

    public static final ResourceLocation VOID_SWIRL = CosmicCore.id("block/iris/void_swirl");
    private static TextureAtlasSprite swirlSprite = null;

    public static final ResourceLocation VOID_BLANK = CosmicCore.id("block/iris/the_hole");
    private static TextureAtlasSprite blankVoidSprite = null;

    private SpiritCrucibleRender() {
        ModelUtils.registerBakeEventListener(true, event -> {
            irisCoreModel = event.getModels().get(IRIS_MODEL_CORE);

        });
        ModelUtils.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
            swirlSprite = event.getAtlas().getSprite(VOID_SWIRL);
            blankVoidSprite = event.getAtlas().getSprite(VOID_BLANK);
        });
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);
        VertexConsumer consumer = buffer.getBuffer(Sheets.translucentCullBlockSheet());
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

            float offset = upOffset * (8.0f + (upOffset * 0.5f)) +
                    backOffset * (4.0f + (backOffset * 0.5f));
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

        renderRings(up.getAxis(), totalTick, poseStack, buffer);
        renderSwirl(machine, partialTick, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, -6, 0);
        Quaternionf rot = new Quaternionf()
                .rotateY(totalTick / -2);
        poseStack.mulPose(rot);
        poseStack.popPose();

        float min = 0.5f;
        float max = 1.0f;
        float amplitude = (max - min) / 2.0f;
        float offset = min + amplitude;
        float scale = (float) Math.sin(totalTick * 0.1f) * amplitude + offset;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 80, 0, 1, 0));
        poseStack.pushPose();
        renderBallA(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();

        max = 0.7f;
        amplitude = (max - min) / 2.0f;
        offset = min + amplitude;
        scale = (float) Math.sin(totalTick * 0.1f) * amplitude + offset;
        poseStack.scale(scale * -1.5F, scale * -1.5F, scale * -1.5F);
        poseStack.pushPose();
        consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());
        renderBallC(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBallA(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.2F, 0f, 0.2F, LightTexture.FULL_BRIGHT, packedOverlay);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBallC(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        List<BakedQuad> quads = irisCoreModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.4F, 0f, 0.4F, LightTexture.FULL_BRIGHT, packedOverlay);
        }
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
                2.75f, 0.1F, 10, 36,
                0.5F, 0, 0.5F, 1, upAxis);
        poseStack.scale(3, 3, 3);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2.5f, 0.2F, 10, 36,
                0.4F, 0f, 0.4F, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateZ(cosZ));
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2f, 0.2F, 10, 36,
                0.25F, 0, 0.25F, 1, upAxis);
        poseStack.popPose();
    }

    public void renderSwirl(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        poseStack.pushPose();
        // do the rotaty thingy yee
        Quaternionf rot = new Quaternionf()
                .rotateY(totalTick / -5);
        poseStack.mulPose(rot);
        poseStack.translate(0, -6.3, 0);
        VertexConsumer consumer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        RenderBufferHelper.renderCube(
                consumer,
                poseStack.last(),
                EnumSet.of(Direction.UP, Direction.DOWN),
                0xFFFFFFFF,
                200,
                swirlSprite,
                -3.5f, 0, -3.5f,
                3.5f, 0, 3.5f);

        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance(), 16, getViewDistance());
    }
}
