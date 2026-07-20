package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern;
import com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior;

import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import static com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior.getDir;

public class StructureBoundingBox {

    public static void renderStructureSelect(PoseStack poseStack, Camera camera) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) return;

        ItemStack held = player.getMainHandItem();
        if (StructureWriteBehavior.isItemStructureWriter(held)) {
            BlockPos[] poses = StructureWriteBehavior.getPos(held);
            if (poses == null) return;
            Vec3 pos = camera.getPosition();

            poseStack.pushPose();
            poseStack.translate(-pos.x, -pos.y, -pos.z);

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer;

            buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            RenderBufferUtils.renderCubeFace(
                    poseStack,
                    buffer,
                    poses[0].getX(),
                    poses[0].getY(),
                    poses[0].getZ(),
                    poses[1].getX() + 1,
                    poses[1].getY() + 1,
                    poses[1].getZ() + 1,
                    0.85f,
                    0.85f,
                    1f,
                    0.25f,
                    true);

            BufferUploader.drawWithShader(buffer.buildOrThrow());

            buffer = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(3);

            RenderBufferUtils.drawCubeFrame(
                    poseStack,
                    buffer,
                    poses[0].getX(),
                    poses[0].getY(),
                    poses[0].getZ(),
                    poses[1].getX() + 1,
                    poses[1].getY() + 1,
                    poses[1].getZ() + 1,
                    1f,
                    1f,
                    1f,
                    0.5f);

            BufferUploader.drawWithShader(buffer.buildOrThrow());

            buffer = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(12);

            var direction = getDir(player.getMainHandItem());
            var directions = DebugBlockPattern.directionsFor(direction);
            Direction sliceDirection = directions.slice().getDefaultFacing();
            Direction stringDirection = directions.string().getDefaultFacing();
            Direction characterDirection = directions.character().getDefaultFacing();
            double originX = axisOrigin(poses, Direction.Axis.X, sliceDirection, stringDirection, characterDirection);
            double originY = axisOrigin(poses, Direction.Axis.Y, sliceDirection, stringDirection, characterDirection);
            double originZ = axisOrigin(poses, Direction.Axis.Z, sliceDirection, stringDirection, characterDirection);
            PoseStack.Pose last = poseStack.last();
            Matrix4f mat4 = last.pose();
            addAxis(buffer, last, mat4, originX, originY, originZ, sliceDirection, 1f, 0.2f, 0.2f);
            addAxis(buffer, last, mat4, originX, originY, originZ, stringDirection, 0.2f, 1f, 0.2f);
            addAxis(buffer, last, mat4, originX, originY, originZ, characterDirection, 0.2f, 0.4f, 1f);
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    private static double axisOrigin(
                                     BlockPos[] positions,
                                     Direction.Axis axis,
                                     Direction first,
                                     Direction second,
                                     Direction third) {
        Direction direction = first.getAxis() == axis ? first : second.getAxis() == axis ? second : third;
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            return switch (axis) {
                case X -> positions[0].getX();
                case Y -> positions[0].getY();
                case Z -> positions[0].getZ();
            };
        }
        return switch (axis) {
            case X -> positions[1].getX() + 1;
            case Y -> positions[1].getY() + 1;
            case Z -> positions[1].getZ() + 1;
        };
    }

    private static void addAxis(
                                BufferBuilder buffer,
                                PoseStack.Pose pose,
                                Matrix4f matrix,
                                double x,
                                double y,
                                double z,
                                Direction direction,
                                float red,
                                float green,
                                float blue) {
        int stepX = direction.getStepX();
        int stepY = direction.getStepY();
        int stepZ = direction.getStepZ();
        buffer.addVertex(matrix, (float) x, (float) y, (float) z)
                .setColor(red, green, blue, 0.85f)
                .setNormal(pose, stepX, stepY, stepZ);
        buffer.addVertex(
                matrix,
                (float) x + stepX * 1.5f,
                (float) y + stepY * 1.5f,
                (float) z + stepZ * 1.5f)
                .setColor(red, green, blue, 0.85f)
                .setNormal(pose, stepX, stepY, stepZ);
    }
}
