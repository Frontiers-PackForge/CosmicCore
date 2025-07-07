package com.ghostipedia.cosmiccore.client.renderer.block;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;
import com.ghostipedia.cosmiccore.common.blockentity.CosmicCoilBlockEntity;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class NebulaeCoilRenderer implements BlockEntityRenderer<CosmicCoilBlockEntity> {

    public static final ResourceLocation NEBULAE_LOCATION = CosmicCore.id("textures/entity/nebulae.png");

    public NebulaeCoilRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CosmicCoilBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!state.getValue(GTBlockStateProperties.ACTIVE)) return;

        poseStack.pushPose();
        Matrix4f pose = poseStack.last().pose();
        
        if (GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent()) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(NEBULAE_LOCATION));
            
            Matrix3f normal = poseStack.last().normal();
            //animation with a period of 20 seconds. note that texture coordinates are wrapping, not clamping.
            float progress = (SystemTimeUniforms.TIMER.getFrameTimeCounter() * 0.05f) % 1f;
            
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    0.0F, 1.0F, 1.0F,
                    1.0F, 1.0F, 1.0F,
                    1.0F, 1.0F, 0.0F,
                    0.0F, 1.0F, 0.0F,
                    Direction.UP);
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    0.0F, 0.0F, 1.0F,
                    0.0F, 0.0F, 0.0F,
                    1.0F, 0.0F, 0.0F,
                    1.0F, 0.0F, 1.0F,
                    Direction.DOWN);
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    0.0F, 1.0F, 0.0F,
                    1.0F, 1.0F, 0.0F,
                    1.0F, 0.0F, 0.0F,
                    0.0F, 0.0F, 0.0F,
                    Direction.NORTH);
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    0.0F, 1.0F, 1.0F,
                    0.0F, 1.0F, 0.0F,
                    0.0F, 0.0F, 0.0F,
                    0.0F, 0.0F, 1.0F,
                    Direction.WEST);
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    0.0F, 1.0F, 1.0F,
                    0.0F, 0.0F, 1.0F,
                    1.0F, 0.0F, 1.0F,
                    1.0F, 1.0F, 1.0F,
                    Direction.SOUTH);
            this.renderFaceOculus(blockEntity, pose, normal, consumer, progress,
                    1.0F, 1.0F, 1.0F,
                    1.0F, 0.0F, 1.0F,
                    1.0F, 0.0F, 0.0F,
                    1.0F, 1.0F, 0.0F,
                    Direction.EAST);
        } else {
            VertexConsumer consumer = buffer.getBuffer(CosmicCoreRenderTypes.nebulae());
            
            this.renderFace(blockEntity, pose, consumer,
                    0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F,
                    Direction.SOUTH);
            this.renderFace(blockEntity, pose, consumer,
                    0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                    Direction.NORTH);
            this.renderFace(blockEntity, pose, consumer,
                    1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F,
                    Direction.EAST);
            this.renderFace(blockEntity, pose, consumer,
                    0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F,
                    Direction.WEST);
            this.renderFace(blockEntity, pose, consumer,
                    0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F,
                    Direction.DOWN);
            this.renderFace(blockEntity, pose, consumer,
                    0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F,
                    Direction.UP);
        }
        
        poseStack.popPose();
    }

    private void renderFace(BlockEntity blockEntity, Matrix4f pose, VertexConsumer consumer,
                            float x0, float x1,
                            float y0, float y1,
                            float z0, float z1, float z2, float z3,
                            Direction direction) {
        if (Block.shouldRenderFace(blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(),
                direction, blockEntity.getBlockPos().relative(direction))) {
            consumer.vertex(pose, x0, y0, z0).endVertex();
            consumer.vertex(pose, x1, y0, z1).endVertex();
            consumer.vertex(pose, x1, y1, z2).endVertex();
            consumer.vertex(pose, x0, y1, z3).endVertex();
        }
    }

    private void renderFaceOculus(BlockEntity blockEntity, Matrix4f pose, Matrix3f normal,
                                  VertexConsumer vertexConsumer,
                                  float progress,
                                  float x0, float y0, float z0,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float x3, float y3, float z3,
                                  Direction direction) {
        if (!Block.shouldRenderFace(blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(),
                direction, blockEntity.getBlockPos().relative(direction))) {
            return;
        }
        
        float nx = direction.getStepX();
        float ny = direction.getStepY();
        float nz = direction.getStepZ();
        
        vertexConsumer.vertex(pose, x0, y0, z0).color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(progress, progress)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, nx, ny, nz).endVertex();
        
        vertexConsumer.vertex(pose, x1, y1, z1).color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(progress, 0.2F + progress)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, nx, ny, nz).endVertex();
        
        vertexConsumer.vertex(pose, x2, y2, z2).color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(0.2F + progress, 0.2F + progress)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, nx, ny, nz).endVertex();
        
        vertexConsumer.vertex(pose, x3, y3, z3).color(1.0f, 1.0f, 1.0f, 1.0f)
                .uv(0.2F + progress, progress)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, nx, ny, nz).endVertex();
    }
}
