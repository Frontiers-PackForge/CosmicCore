package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import wayoftime.bloodmagic.BloodMagic;

import java.util.List;
import java.util.function.Consumer;

public class HemophagicTransfuserRender extends WorkableCasingMachineRenderer implements IControllerRenderer {

    public static final ResourceLocation TEXTURE = BloodMagic.rl("block/blankrune");
    public static final ResourceLocation OVERLAY_MODEL_TEXTURES = GTCEu.id("block/multiblock/fusion_reactor");
    public static final ResourceLocation HEMOPHAGIC_TRANSFUSER_MODEL = CosmicCore.id("block/iris/bloodcube");
    public static final int CENTER_OFFSET = -5;
    public static final float FADEOUT = 60;
    protected float delta = 0;
    protected int lastColor = -1;
    boolean isActive = false;
    private float tickvalue = 0;
    public final ResourceLocation multipartSprite;

    public HemophagicTransfuserRender(ResourceLocation texture, ResourceLocation multipartSprite,
                                      ResourceLocation workableModel) {
        super(TEXTURE, OVERLAY_MODEL_TEXTURES);
        this.multipartSprite = multipartSprite;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof WorkableElectricMultiblockMachine machine) {
            var level = machine.getLevel();
            assert level != null;
            tickvalue += partialTicks / 30;
            isActive = machine.isActive();
            if (machine.isFormed()) {
                renderCube(machine, poseStack, buffer, tickvalue, combinedLight,
                        combinedOverlay);
            }
            if (isActive) {
                renderLightRing(machine, tickvalue, poseStack, buffer, tickvalue);
            }
        }
    }

    @Override
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
                                @Nullable Direction side, RandomSource rand, Direction modelFacing,
                                ModelState modelState) {
        if (modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(multipartSprite),
                    modelState));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCube(WorkableElectricMultiblockMachine machine, PoseStack poseStack,
                           MultiBufferSource bufferSource, float tick, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var up = RelativeDirection.UP.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        var back = RelativeDirection.BACK.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        var left = RelativeDirection.LEFT.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        // translate to the absolute center of multiblock
        poseStack.translate(
                up.getStepX() * (4f + (up.getStepX() > 0 ? .5f : -.5f)) +
                        back.getStepX() * (5f + (back.getStepX() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.X ? .5f : 0),
                up.getStepY() * (4f + (up.getStepY() > 0 ? .5f : -.5f)) +
                        back.getStepY() * (5f + (back.getStepY() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.Y ? .5f : 0),
                up.getStepZ() * (4f + (up.getStepZ() > 0 ? .5f : -.5f)) +
                        back.getStepZ() * (5f + (back.getStepZ() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.Z ? .5f : 0));
        // rotate around center
        Quaternionf xAxisRot = new Quaternionf().rotateAxis(Mth.sin(tick / 20), 1, 0, 0);
        Quaternionf yAxisRot = new Quaternionf().rotateAxis(Mth.sin(tick / 30), 0, 1, 0);
        Quaternionf zAxisRot = new Quaternionf().rotateAxis(Mth.cos(Mth.HALF_PI + tick / 60), 0, 0, 1);
        poseStack.mulPose(xAxisRot);
        poseStack.mulPose(yAxisRot);
        poseStack.mulPose(zAxisRot);
        // scale the stack
        poseStack.scale(2, 2, 2);
        // translate back to corner of center block position (blocks are drawn from the 0,0,0 corner)
        poseStack.translate(up.getStepX() * (up.getStepX() > 0 ? -.5f : .5f) +
                back.getStepX() * (back.getStepX() > 0 ? -.5f : .5f) -
                (left.getAxis() == Direction.Axis.X ? .5f : 0),
                up.getStepY() * (up.getStepY() > 0 ? -.5f : .5f) +
                        back.getStepY() * (back.getStepY() > 0 ? -.5f : .5f) -
                        (left.getAxis() == Direction.Axis.Y ? .5f : 0),
                up.getStepZ() * (up.getStepZ() > 0 ? -.5f : .5f) +
                        back.getStepZ() * (back.getStepZ() > 0 ? -.5f : .5f) -
                        (left.getAxis() == Direction.Axis.Z ? .5f : 0));
        // draw block model quads
        var bakedModel = blockRenderer.getBlockModel(CosmicBlocks.BLOOD_CUBE.getDefaultState());
        var consumer = bufferSource.getBuffer(RenderType.solid());
        for (var face : GTUtil.DIRECTIONS) {
            bakedModel.getQuads(CosmicBlocks.BLOOD_CUBE.getDefaultState(), face, GTValues.RNG)
                    .forEach(quad -> consumer.putBulkData(poseStack.last(), quad, 1, 1, 1,
                            LightTexture.FULL_BRIGHT, combinedOverlay));
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(WorkableElectricMultiblockMachine machine, float partialTicks, PoseStack poseStack,
                                 MultiBufferSource buffer, float tick) {
        var color = 12191265;
        var alpha = 1f;
        // if (machine.recipeLogic.isWorking()) {
        // lastColor = color;
        // delta = FADEOUT;
        // } else {
        // alpha = delta / FADEOUT;
        // lastColor = color(Mth.floor(alpha * 255), red(12191265), green(12191265), blue(12191265));
        // delta -= Minecraft.getInstance().getDeltaFrameTime();
        // }

        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        var up = RelativeDirection.UP.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        var back = RelativeDirection.BACK.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        var left = RelativeDirection.LEFT.getRelativeFacing(machine.getFrontFacing(), machine.getUpwardsFacing(),
                machine.isFlipped());
        poseStack.pushPose();
        poseStack.translate(
                up.getStepX() * (4f + (up.getStepX() > 0 ? .5f : -.5f)) +
                        back.getStepX() * (5f + (back.getStepX() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.X ? .5f : 0),
                up.getStepY() * (4f + (up.getStepY() > 0 ? .5f : -.5f)) +
                        back.getStepY() * (5f + (back.getStepY() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.Y ? .5f : 0),
                up.getStepZ() * (4f + (up.getStepZ() > 0 ? .5f : -.5f)) +
                        back.getStepZ() * (5f + (back.getStepZ() > 0 ? .5f : -.5f)) +
                        (left.getAxis() == Direction.Axis.Z ? .5f : 0));
        poseStack.pushPose();
        float partialDiv20 = partialTicks / 20;
        float halfPiPartialDiv60 = Mth.HALF_PI + partialTicks / 60;
        float partialDiv30 = partialTicks / 30;
        var sinPartialDiv20Quaternion = new Quaternionf().rotateAxis(Mth.sin(partialDiv20), 1, 0, 0);
        var sinPartialDiv30Quaternion = new Quaternionf().rotateAxis(Mth.sin(partialDiv30), 0, 1, 0);
        var cosHalfPiPartialDiv60Quaternion = new Quaternionf().rotateAxis(Mth.cos(halfPiPartialDiv60), 0, 0, 1);
        poseStack.mulPose(sinPartialDiv20Quaternion);
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.cos(partialDiv30), 0, 1, 0));
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.sin(halfPiPartialDiv60), 0, 0, 1));
        RenderBufferHelper.renderRing(poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()), 0, 0, 0,
                2f, 0.1F, 10, 36, 0.5F, 0, 0, alpha, axis);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(Mth.cos(partialDiv20), 1, 0, 0));
        poseStack.mulPose(sinPartialDiv30Quaternion);
        poseStack.mulPose(cosHalfPiPartialDiv60Quaternion);
        RenderBufferHelper.renderRing(poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()), 0, 0, 0,
                1.8f, 0.1F, 10, 36, 0.4F, 0f, 0, alpha, axis);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(cosHalfPiPartialDiv60Quaternion);
        RenderBufferHelper.renderRing(poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()), 0, 0, 0,
                1.6f, 0.1F, 10, 36, 0.6F, 0, 0, alpha, axis);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(HEMOPHAGIC_TRANSFUSER_MODEL);
    }

    @OnlyIn(Dist.CLIENT)
    public float reBakeCustomQuadsOffset() {
        return 0f;
    }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
