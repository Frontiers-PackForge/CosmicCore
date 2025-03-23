package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Consumer;

public class HemophagicTransfuserRender extends WorkableCasingMachineRenderer {

    public static final ResourceLocation TEXTURE = CosmicCore
            .id("block/casings/solid/vomahine_certified_chemically_resistant_casing");
    public static final ResourceLocation OVERLAY_MODEL_TEXTURES = GTCEu.id("block/multiblock/fusion_reactor");
    public static final ResourceLocation IRIS_MODEL_CORE = CosmicCore.id("block/iris/bloodcube");
    public static final int CENTER_OFFSET = -1;


    public HemophagicTransfuserRender() {
        super(TEXTURE, OVERLAY_MODEL_TEXTURES);
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
             renderCube(poseStack, buffer, frontFacing ,upwardsFacing, tick, combinedLight, combinedOverlay);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderCube(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing, Direction upwardsFacing,
                           float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(IRIS_MODEL_CORE);
        BlockPos offset = RelativeDirection.offsetPos(BlockPos.ZERO, frontFacing, upwardsFacing, false,
                0, 0, CENTER_OFFSET);
        poseStack.translate(0.5D, -2.5D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1, 0));
        poseStack.scale(10.0f, 10.0f, 10.0f);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1.0f, 1.0f, 1.0f, combinedLight, combinedOverlay);
        }
        poseStack.popPose();
    }


    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(IRIS_MODEL_CORE);
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