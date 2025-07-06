package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;


import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Consumer;


public class IrisMachineRenderer extends DynamicRender<IrisMultiblockMachine, IrisMachineRenderer> {

    public static final ResourceLocation TEXTURE = CosmicCore
            .id("block/casings/solid/vomahine_certified_chemically_resistant_casing");
    public static final ResourceLocation OVERLAY_MODEL_TEXTURES = GTCEu.id("block/multiblock/fusion_reactor");
    public static final ResourceLocation STAR_MODEL_CORE = CosmicCore.id("block/iris/star_sphere");
    public static final ResourceLocation STAR_MODEL_OUTER = CosmicCore.id("block/iris/star_sphere_outer");
    public static final ResourceLocation STAR_MODEL_INNER = CosmicCore.id("block/iris/star_sphere_inner");
    public static final ResourceLocation IRIS_MODEL_CORE = CosmicCore.id("block/iris/iris_sphere");
    public static final ResourceLocation IRIS_MODEL_RING = CosmicCore.id("block/iris/iris_ring");
    public static final ResourceLocation IRIS_MODEL_RING_WHITE = CosmicCore.id("block/iris/iris_ring_white");
    private float tickvalue;

//    public IrisMachineRenderer() {
//        super(TEXTURE, OVERLAY_MODEL_TEXTURES);
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean hasTESR(BlockEntity blockEntity) {
//        return true;
//    }

    @Override
    public void render(IrisMultiblockMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(IrisMultiblockMachine machine) {
        return super.shouldRenderOffScreen(machine);
    }

    @Override
    public boolean shouldRender(IrisMultiblockMachine machine, Vec3 cameraPos) {
        return super.shouldRender(machine, cameraPos);
    }

    @Override
    public AABB getRenderBoundingBox(IrisMultiblockMachine machine) {
        return super.getRenderBoundingBox(machine);
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return super.getBlockEntityType();
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
            assert level != null;
            tickvalue += partialTicks / 30;
            // renderStar(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
            // renderStarInsides(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
            // renderStarShell(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
            renderIris(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
            renderRing(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
            renderRingSmall(poseStack, buffer, frontFacing, tickvalue, combinedLight, combinedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull BlockEntity blockEntity) {
        return super.shouldRenderOffScreen(blockEntity);
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance();
    }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return super.getRenderBoundingBox(blockEntity);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderIris(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                           float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(IRIS_MODEL_CORE);
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

    public void renderRing(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                           float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(IRIS_MODEL_RING);
        poseStack.translate(0.5D, -2.5D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1, 0));
        poseStack.scale(20.0f, 20.0f, 20.0f);
        PoseStack.Pose pose = poseStack.last();
        RenderSystem.disableCull();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, combinedLight, combinedOverlay);
        }
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    public void renderRingSmall(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                                float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(IRIS_MODEL_RING_WHITE);
        poseStack.translate(0.5D, -2.0D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 20, 0, 1, 0));
        poseStack.scale(13.0f, 13.0f, 13.0f);
        PoseStack.Pose pose = poseStack.last();
        RenderSystem.disableCull();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, combinedLight, combinedOverlay);
        }
        RenderSystem.enableCull();
        poseStack.popPose();
    }

    /// STAR

    public void renderStar(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                           float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(STAR_MODEL_CORE);
        poseStack.translate(0.5D, -2.5D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateXYZ(0.25f, 0.0f, 0f));
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0f, 1f, 0));
        poseStack.scale(4.6f, 4.6f, 4.6f);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, combinedLight, combinedOverlay);
            consumer.putBulkData(pose, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 1f, 1f, 1f, 0.65f,
                    new int[]{combinedLight, combinedLight, combinedLight, combinedLight}, combinedOverlay, false);

        }
        poseStack.popPose();
    }

    public void renderStarShell(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                                float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(STAR_MODEL_OUTER);
        poseStack.translate(0.5D, -2.5D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateXYZ(0.65f, 0.0f, 0.35f));
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0f, 1, 0f));
        poseStack.scale(5.0f, 5.0f, 5.0f);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, combinedLight, combinedOverlay);
            consumer.putBulkData(pose, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 1f, 1f, 1f, 0.5f,
                    new int[]{combinedLight, combinedLight, combinedLight, combinedLight}, combinedOverlay, false);
        }
        poseStack.popPose();
    }

    public void renderStarInsides(PoseStack poseStack, MultiBufferSource bufferSource, Direction frontFacing,
                                  float tick, int combinedLight, int combinedOverlay) {
        var modelManager = Minecraft.getInstance().getModelManager();
        poseStack.pushPose();
        BakedModel bakedmodel = modelManager.getModel(STAR_MODEL_INNER);
        poseStack.translate(0.5D, -2.5D, 46.5D);
        poseStack.mulPose(new Quaternionf().rotateAxis(tick * Mth.TWO_PI / 80, 0, 1f, 0));
        poseStack.scale(4.85f, 4.85f, 4.85f);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
        List<BakedQuad> quads = bakedmodel.getQuads(null, null, GTValues.RNG);
        for (BakedQuad quad : quads) {
            consumer.putBulkData(pose, quad, 1f, 1f, 1f, combinedLight, combinedOverlay);
            consumer.putBulkData(pose, quad, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 1f, 1f, 1f, 0.7f,
                    new int[]{combinedLight, combinedLight, combinedLight, combinedLight}, combinedOverlay, false);
        }
        poseStack.popPose();
    }

    @Override
    public DynamicRenderType<IrisMultiblockMachine, IrisMachineRenderer> getType() {
        return ;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return super.getQuads(state, side, rand);
    }

    @Override
    public ItemTransforms getTransforms() {
        return super.getTransforms();
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return super.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return super.useAmbientOcclusion(state, renderType);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return super.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return super.getModelData(level, pos, state, modelData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return super.getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return super.getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return super.getRenderTypes(itemStack, fabulous);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return super.getRenderPasses(itemStack, fabulous);
    }
}

//    @Override
//    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
//        super.onAdditionalModel(registry);
//        registry.accept(IRIS_MODEL_CORE);
//        registry.accept(IRIS_MODEL_RING);
//        registry.accept(IRIS_MODEL_RING_WHITE);
//        registry.accept(STAR_MODEL_CORE);
//        registry.accept(STAR_MODEL_INNER);
//        registry.accept(STAR_MODEL_OUTER);
//    }
//
//    @Override
//    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
//        return super.getModelData(level, pos, state, modelData);
//    }
