package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.joml.Quaternionf;

import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HemophagicTransfuserRender extends
                                        DynamicRender<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> {

    public static final HemophagicTransfuserRender INSTANCE = new HemophagicTransfuserRender();
    public static final MapCodec<HemophagicTransfuserRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> TYPE = new DynamicRenderType<>(
            HemophagicTransfuserRender.CODEC);

    private static final BiFunction<Direction, Direction, AABB> renderBoundCache = Util.memoize((front, upwards) -> {
        Direction up = RelativeDirection.UP.getRelative(front, upwards, false);
        Direction back = RelativeDirection.BACK.getRelative(front, upwards, false);
        Direction left = RelativeDirection.LEFT.getRelative(front, upwards, false);

        // offset from the controller to the inner cube (scaled up by 1 in all directions)
        // values are from the multi pattern
        BlockPos.MutableBlockPos minPos = new BlockPos.MutableBlockPos()
                .move(left, 3).move(up, 1).move(back, 2);
        BlockPos.MutableBlockPos maxPos = new BlockPos.MutableBlockPos()
                .move(left, -3).move(up, 7).move(back, 8);

        return AABB.encapsulatingFullBlocks(minPos, maxPos);
    });

    public static final ResourceLocation BLOOD_CUBE_TEXTURE = CosmicCore.id("block/iris/blood_cube");

    private static TextureAtlasSprite bloodCubeSprite = null;
    private static boolean isEventListenerRegistered = false;

    @SuppressWarnings("deprecation")
    private HemophagicTransfuserRender() {
        if (!isEventListenerRegistered) {
            ModelUtils.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
                bloodCubeSprite = event.getAtlas().getSprite(BLOOD_CUBE_TEXTURE);
            });
            isEventListenerRegistered = true;
        }
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine multi) {
        if (multi.isFormed()) {
            AABB bounds = renderBoundCache.apply(multi.getFrontFacing(), multi.getUpwardsFacing());
            return bounds.move(multi.getBlockPos());
        }
        return super.getRenderBoundingBox(multi);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        poseStack.pushPose();

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

        renderBloodCube(poseStack, buffer, totalTick);

        if (machine.isActive()) {
            renderRings(up.getAxis(), totalTick, poseStack, buffer);
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBloodCube(PoseStack poseStack, MultiBufferSource bufferSource, float totalTick) {
        poseStack.pushPose();
        // rotate around center
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(Mth.sin(totalTick / 20),
                        Mth.sin(totalTick / 30),
                        Mth.cos(Mth.HALF_PI + totalTick / 60))
                .rotateXYZ(55f * Mth.DEG_TO_RAD, 30f * Mth.DEG_TO_RAD, 0);
        poseStack.mulPose(rot);

        // draw cube quads
        var consumer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        RenderBufferHelper.renderCube(consumer, poseStack.last(), 0xffffffff,
                LightTexture.FULL_BRIGHT, bloodCubeSprite,
                -1, -1, -1, 1, 1, 1);

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRings(Direction.Axis upAxis, float totalTick, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());

        float xRot = totalTick / 20;
        float zRot = Mth.HALF_PI + totalTick / 60;
        float yRot = totalTick / 30;
        float sinX = Mth.sin(xRot), cosX = Mth.cos(xRot);
        float sinY = Mth.sin(yRot), cosY = Mth.cos(yRot);
        float sinZ = Mth.sin(zRot), cosZ = Mth.cos(zRot);

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(sinX, cosY, sinZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2f, 0.1F, 10, 36,
                0.5F, 0, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.8f, 0.1F, 10, 36,
                0.4F, 0f, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateZ(cosZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.6f, 0.1F, 10, 36,
                0.6F, 0, 0, 1, upAxis);
        poseStack.popPose();
    }
}
