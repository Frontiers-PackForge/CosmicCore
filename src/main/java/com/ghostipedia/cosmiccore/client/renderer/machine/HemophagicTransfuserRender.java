package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HemophagicTransfuserRender extends
                                        DynamicRender<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> {

    public static final HemophagicTransfuserRender INSTANCE = new HemophagicTransfuserRender();
    public static final Codec<HemophagicTransfuserRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> TYPE = new DynamicRenderType<>(
            HemophagicTransfuserRender.CODEC);

    public static final ResourceLocation BLOOD_CUBE_TEXTURE = CosmicCore.id("block/iris/bloodcube");

    private static TextureAtlasSprite bloodCubeSprite = null;
    private static boolean isEventListenerRegistered = false;

    private HemophagicTransfuserRender() {
        if (!isEventListenerRegistered) {
            ModelUtils.registerAtlasStitchedEventListener(TextureAtlas.LOCATION_BLOCKS, event -> {
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
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float tickValue = (Minecraft.getInstance().level.getGameTime() + partialTick);
        if (machine.isFormed()) {
            renderBloodCube(machine, poseStack, buffer, tickValue);
        }
        if (machine.isActive()) {
            renderRings(machine, tickValue, poseStack, buffer);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBloodCube(MultiblockControllerMachine machine, PoseStack poseStack,
                                MultiBufferSource bufferSource, float totalTick) {
        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();

        Vec3i up = RelativeDirection.UP.getRelative(front, upwards, flipped).getNormal();
        Vec3i back = RelativeDirection.BACK.getRelative(front, upwards, flipped).getNormal();
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();
        // translate to the absolute center of the multiblock
        float xo = 0, yo = 0, zo = 0;

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int upOffset = axis.choose(up.getX(), up.getY(), up.getZ());
            int backOffset = axis.choose(back.getX(), back.getY(), back.getZ());

            float offset = upOffset * (4.0f + (upOffset * 0.5f)) +
                    backOffset * (5.0f + (backOffset * 0.5f));
            switch (axis) {
                case X -> xo = offset;
                case Y -> yo = offset;
                case Z -> zo = offset;
            }
        }
        poseStack.translate(
                xo + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                yo + (leftAxis == Direction.Axis.Y ? 0.5f : 0.0f),
                zo + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));

        // rotate around center
        Quaternionf rot = new Quaternionf()
                .rotateAxis(Mth.sin(totalTick / 20), 1, 0, 0)
                .rotateAxis(Mth.sin(totalTick / 30), 0, 1, 0)
                .rotateAxis(Mth.cos(Mth.HALF_PI + totalTick / 60), 0, 0, 1)
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
    private void renderRings(WorkableElectricMultiblockMachine machine, float totalTick,
                             PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        Direction.Axis axis = RelativeDirection.UP
                .getRelative(machine.getFrontFacing(), machine.getUpwardsFacing(), machine.isFlipped())
                .getAxis();

        float xRot = totalTick / 20;
        float zRot = Mth.HALF_PI + totalTick / 60;
        float yRot = totalTick / 30;
        Quaternionf xAxisRot = new Quaternionf().rotateAxis(Mth.sin(xRot), 1, 0, 0);
        Quaternionf yAxisRot = new Quaternionf().rotateAxis(Mth.sin(yRot), 0, 1, 0);
        Quaternionf zAxisRot = new Quaternionf().rotateAxis(Mth.cos(zRot), 0, 0, 1);

        poseStack.pushPose();
        poseStack.mulPose(xAxisRot);
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.cos(yRot), 0, 1, 0));
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.sin(zRot), 0, 0, 1));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2f, 0.1F, 10, 36,
                0.5F, 0, 0, 1, axis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.cos(xRot), 1, 0, 0));
        poseStack.mulPose(yAxisRot);
        poseStack.mulPose(zAxisRot);
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.8f, 0.1F, 10, 36,
                0.4F, 0f, 0, 1, axis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(zAxisRot);
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.6f, 0.1F, 10, 36,
                0.6F, 0, 0, 1, axis);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
