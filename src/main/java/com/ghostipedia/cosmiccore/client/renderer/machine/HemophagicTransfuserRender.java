package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
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
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import wayoftime.bloodmagic.BloodMagic;

import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.util.FastColor.ARGB32.*;

public class HemophagicTransfuserRender extends WorkableCasingMachineRenderer implements IControllerRenderer {

    public static final ResourceLocation TEXTURE =BloodMagic.rl("block/blankrune");
    public static final ResourceLocation OVERLAY_MODEL_TEXTURES = GTCEu.id("block/multiblock/fusion_reactor");
    public static final ResourceLocation HEMOPHAGIC_TRANSFUSER_MODEL = CosmicCore.id("block/iris/bloodcube");
    public static final int CENTER_OFFSET = -5;
    public static final float FADEOUT = 60;
    protected float delta = 0;
    protected int lastColor = -1;
    @Persisted
    @DescSynced
    boolean isActive = false;

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
                machineBlockEntity.getMetaMachine() instanceof WorkableElectricMultiblockMachine machine &&
                machine.isFormed()) {
            var level = machine.getLevel();
            var frontFacing = machine.getFrontFacing();
            var upwardsFacing = machine.getUpwardsFacing();
            float tick = level.getGameTime() + partialTicks;
            isActive = false;
            renderCube(machine, poseStack, buffer, frontFacing, upwardsFacing, tick, combinedLight, combinedOverlay);
        }
        if (blockEntity instanceof IMachineBlockEntity machineBlockEntity &&
                machineBlockEntity.getMetaMachine() instanceof WorkableElectricMultiblockMachine machine &&
                machine.isActive()) {
            var level = machine.getLevel();
            var frontFacing = machine.getFrontFacing();
            var upwardsFacing = machine.getUpwardsFacing();
            float tick = level.getGameTime() + partialTicks;
            isActive = true;
            renderCube(machine, poseStack, buffer, frontFacing, upwardsFacing, tick, combinedLight, combinedOverlay);
            renderLightRing(machine, partialTicks, poseStack, buffer, tick);
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
    public void renderCube(WorkableElectricMultiblockMachine machine, PoseStack poseStack, MultiBufferSource bufferSource,
                           Direction frontFacing, Direction upwardsFacing,
                           float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(HEMOPHAGIC_TRANSFUSER_MODEL);
        BlockPos offset = RelativeDirection.offsetPos(BlockPos.ZERO, frontFacing, upwardsFacing, false,
                0, 0, CENTER_OFFSET);
        poseStack.translate(offset.getX() + 0.5D, offset.getY() + 4.5D, offset.getZ() + 0.5D);
        if (!machine.recipeLogic.isWorking()){
            poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1, 0));
        } else if (machine.recipeLogic.isWorking()){
            poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 20, 0, 1, 0));
        }
        poseStack.scale(1.0f, 1.0f, 1.0f);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 0.5f, 0.2f, 0.2f, combinedLight, combinedOverlay);
        }

        poseStack.popPose();
    }
    @OnlyIn(Dist.CLIENT)
    private void renderLightRing(WorkableElectricMultiblockMachine machine, float partialTicks, PoseStack poseStack,
                                 MultiBufferSource buffer, float tick) {
        var color = 12191265;
        var alpha = 1f;
        if (machine.recipeLogic.isWorking()) {
            lastColor = color;
            delta = FADEOUT;
        } else {
            alpha = delta / FADEOUT;
            lastColor = color(Mth.floor(alpha * 255), red(12191265), green(12191265), blue(12191265));
            delta -= Minecraft.getInstance().getDeltaFrameTime();
        }

        var front = machine.getFrontFacing();
        var upwards = machine.getUpwardsFacing();
        var flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        var axis = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getAxis();
        BlockPos offset = RelativeDirection.offsetPos(BlockPos.ZERO, front, upwards, false,
                0, 0, CENTER_OFFSET);
        poseStack.translate(offset.getX() + -0.15D, offset.getY() + 3.5D, offset.getZ() + 2D);
        poseStack.mulPose(new Quaternionf().rotateAxis(45, 0, 0, 1));
        poseStack.mulPose(new Quaternionf().rotateAxis(45, 0, 1, 0));
        poseStack.mulPose(new Quaternionf().rotateAxis(0, 1, 0, 0));
        poseStack.scale(0.25f, 0.25f, 0.25f);
        RenderBufferHelper.renderRing(poseStack, buffer.getBuffer(GTRenderTypes.getLightRing()),
                back.getStepX() * 7 + 0.5F,
                back.getStepY() * 7 + 0.5F,
                back.getStepZ() * 7 + 0.5F,
                7, 0.3F, 10, 36,
                0.5F, 0, 0, alpha, axis);
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