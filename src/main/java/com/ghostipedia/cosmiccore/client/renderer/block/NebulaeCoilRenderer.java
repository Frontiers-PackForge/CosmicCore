package com.ghostipedia.cosmiccore.client.renderer.block;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class NebulaeCoilRenderer implements BlockEntityRenderer<CosmicCoilBlockEntity> {

    public static final ResourceLocation NEBULAE_LOCATION = CosmicCore.id("textures/entity/nebulae.png");

    // Small offset to prevent Z-fighting with the block model
    private static final float OFFSET = 0.001F;
    private static final float MIN = OFFSET;
    private static final float MAX = 1.0F - OFFSET;

    public NebulaeCoilRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CosmicCoilBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!state.getValue(GTBlockStateProperties.ACTIVE)) return;

        poseStack.pushPose();
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        // TODO(cosmiccore-42.14): restore the Oculus/Iris shader-animated nebulae path once Iris is on the 1.21
        // classpath.
        VertexConsumer consumer = buffer.getBuffer(CosmicCoreRenderTypes.nebulae());

        this.renderFace(blockEntity, pose, consumer,
                MIN, MAX, MIN, MAX, MAX, MAX, MAX, MAX,
                Direction.SOUTH);
        this.renderFace(blockEntity, pose, consumer,
                MIN, MAX, MAX, MIN, MIN, MIN, MIN, MIN,
                Direction.NORTH);
        this.renderFace(blockEntity, pose, consumer,
                MAX, MAX, MAX, MIN, MIN, MAX, MAX, MIN,
                Direction.EAST);
        this.renderFace(blockEntity, pose, consumer,
                MIN, MIN, MIN, MAX, MIN, MAX, MAX, MIN,
                Direction.WEST);
        this.renderFace(blockEntity, pose, consumer,
                MIN, MAX, MIN, MIN, MIN, MIN, MAX, MAX,
                Direction.DOWN);
        this.renderFace(blockEntity, pose, consumer,
                MIN, MAX, MAX, MAX, MAX, MAX, MIN, MIN,
                Direction.UP);

        poseStack.popPose();
    }

    private void renderFace(BlockEntity blockEntity, Matrix4f pose, VertexConsumer consumer,
                            float x0, float x1,
                            float y0, float y1,
                            float z0, float z1, float z2, float z3,
                            Direction direction) {
        if (Block.shouldRenderFace(blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(),
                direction, blockEntity.getBlockPos().relative(direction))) {
            consumer.addVertex(pose, x0, y0, z0);
            consumer.addVertex(pose, x1, y0, z1);
            consumer.addVertex(pose, x1, y1, z2);
            consumer.addVertex(pose, x0, y1, z3);
        }
    }
}
