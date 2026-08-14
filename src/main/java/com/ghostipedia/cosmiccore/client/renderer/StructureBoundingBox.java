package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.api.data.DebugBlockPattern;
import com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior;

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

import static com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior.getOrientation;

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

            renderCubeFace(
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

            drawCubeFrame(
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

            var orientation = getOrientation(player.getMainHandItem());
            var directions = orientation.world();
            Direction sliceDirection = directions.slice();
            Direction stringDirection = directions.string();
            Direction characterDirection = directions.character();
            double originX = axisOrigin(poses, Direction.Axis.X, sliceDirection, stringDirection, characterDirection);
            double originY = axisOrigin(poses, Direction.Axis.Y, sliceDirection, stringDirection, characterDirection);
            double originZ = axisOrigin(poses, Direction.Axis.Z, sliceDirection, stringDirection, characterDirection);
            PoseStack.Pose last = poseStack.last();
            Matrix4f mat4 = last.pose();
            addAxis(buffer, last, mat4, originX, originY, originZ, sliceDirection, 1f, 0.2f, 0.2f);
            addAxis(buffer, last, mat4, originX, originY, originZ, stringDirection, 0.2f, 1f, 0.2f);
            addAxis(buffer, last, mat4, originX, originY, originZ, characterDirection, 0.2f, 0.4f, 1f);
            addFrontArrow(
                    buffer,
                    last,
                    mat4,
                    poses,
                    DebugBlockPattern.exportOrientationFor(orientation).front());
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    private static void renderCubeFace(
                                       PoseStack poseStack,
                                       BufferBuilder buffer,
                                       float minX,
                                       float minY,
                                       float minZ,
                                       float maxX,
                                       float maxY,
                                       float maxZ,
                                       float red,
                                       float green,
                                       float blue,
                                       float alpha,
                                       boolean shade) {
        Matrix4f matrix = poseStack.last().pose();
        float sideRed = shade ? red * 0.6f : red;
        float sideGreen = shade ? green * 0.6f : green;
        float sideBlue = shade ? blue * 0.6f : blue;
        addFaceVertex(buffer, matrix, minX, minY, minZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, minX, minY, maxZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, maxZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, minZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, minZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, minZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, maxZ, sideRed, sideGreen, sideBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, maxZ, sideRed, sideGreen, sideBlue, alpha);

        float downRed = shade ? red * 0.5f : red;
        float downGreen = shade ? green * 0.5f : green;
        float downBlue = shade ? blue * 0.5f : blue;
        addFaceVertex(buffer, matrix, minX, minY, minZ, downRed, downGreen, downBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, minZ, downRed, downGreen, downBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, maxZ, downRed, downGreen, downBlue, alpha);
        addFaceVertex(buffer, matrix, minX, minY, maxZ, downRed, downGreen, downBlue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, minZ, red, green, blue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, maxZ, red, green, blue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, maxZ, red, green, blue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, minZ, red, green, blue, alpha);

        float frontRed = shade ? red * 0.8f : red;
        float frontGreen = shade ? green * 0.8f : green;
        float frontBlue = shade ? blue * 0.8f : blue;
        addFaceVertex(buffer, matrix, minX, minY, minZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, minZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, minZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, minZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, minX, minY, maxZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, minY, maxZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, maxX, maxY, maxZ, frontRed, frontGreen, frontBlue, alpha);
        addFaceVertex(buffer, matrix, minX, maxY, maxZ, frontRed, frontGreen, frontBlue, alpha);
    }

    private static void addFaceVertex(
                                      BufferBuilder buffer,
                                      Matrix4f matrix,
                                      float x,
                                      float y,
                                      float z,
                                      float red,
                                      float green,
                                      float blue,
                                      float alpha) {
        buffer.addVertex(matrix, x, y, z).setColor(red, green, blue, alpha);
    }

    private static void drawCubeFrame(
                                      PoseStack poseStack,
                                      BufferBuilder buffer,
                                      float minX,
                                      float minY,
                                      float minZ,
                                      float maxX,
                                      float maxY,
                                      float maxZ,
                                      float red,
                                      float green,
                                      float blue,
                                      float alpha) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        addFrameLine(buffer, pose, matrix, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha, 1, 0, 0);
        addFrameLine(buffer, pose, matrix, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha, 1, 0, 0);
        addFrameLine(buffer, pose, matrix, minX, minY, maxZ, maxX, minY, maxZ, red, green, blue, alpha, 1, 0, 0);
        addFrameLine(buffer, pose, matrix, minX, maxY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha, 1, 0, 0);
        addFrameLine(buffer, pose, matrix, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha, 0, 1, 0);
        addFrameLine(buffer, pose, matrix, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha, 0, 1, 0);
        addFrameLine(buffer, pose, matrix, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha, 0, 1, 0);
        addFrameLine(buffer, pose, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha, 0, 1, 0);
        addFrameLine(buffer, pose, matrix, minX, minY, minZ, minX, minY, maxZ, red, green, blue, alpha, 0, 0, 1);
        addFrameLine(buffer, pose, matrix, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha, 0, 0, 1);
        addFrameLine(buffer, pose, matrix, minX, maxY, minZ, minX, maxY, maxZ, red, green, blue, alpha, 0, 0, 1);
        addFrameLine(buffer, pose, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha, 0, 0, 1);
    }

    private static void addFrameLine(
                                     BufferBuilder buffer,
                                     PoseStack.Pose pose,
                                     Matrix4f matrix,
                                     float startX,
                                     float startY,
                                     float startZ,
                                     float endX,
                                     float endY,
                                     float endZ,
                                     float red,
                                     float green,
                                     float blue,
                                     float alpha,
                                     float normalX,
                                     float normalY,
                                     float normalZ) {
        buffer.addVertex(matrix, startX, startY, startZ)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, normalX, normalY, normalZ);
        buffer.addVertex(matrix, endX, endY, endZ)
                .setColor(red, green, blue, alpha)
                .setNormal(pose, normalX, normalY, normalZ);
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

    private static void addFrontArrow(
                                      BufferBuilder buffer,
                                      PoseStack.Pose pose,
                                      Matrix4f matrix,
                                      BlockPos[] positions,
                                      Direction direction) {
        double minX = positions[0].getX();
        double minY = positions[0].getY();
        double minZ = positions[0].getZ();
        double maxX = positions[1].getX() + 1;
        double maxY = positions[1].getY() + 1;
        double maxZ = positions[1].getZ() + 1;
        double centerX = (minX + maxX) * 0.5;
        double centerY = (minY + maxY) * 0.5;
        double centerZ = (minZ + maxZ) * 0.5;
        double faceX = direction.getStepX() > 0 ? maxX : direction.getStepX() < 0 ? minX : centerX;
        double faceY = direction.getStepY() > 0 ? maxY : direction.getStepY() < 0 ? minY : centerY;
        double faceZ = direction.getStepZ() > 0 ? maxZ : direction.getStepZ() < 0 ? minZ : centerZ;
        double tipX = faceX + direction.getStepX() * 2.25;
        double tipY = faceY + direction.getStepY() * 2.25;
        double tipZ = faceZ + direction.getStepZ() * 2.25;
        double neckX = tipX - direction.getStepX() * 0.65;
        double neckY = tipY - direction.getStepY() * 0.65;
        double neckZ = tipZ - direction.getStepZ() * 0.65;
        double[] firstPerpendicular = direction.getAxis() == Direction.Axis.Y ?
                new double[] { 1, 0, 0 } : new double[] { 0, 1, 0 };
        double[] secondPerpendicular = direction.getAxis() == Direction.Axis.Z ?
                new double[] { 1, 0, 0 } : new double[] { 0, 0, 1 };

        addLine(buffer, pose, matrix, faceX, faceY, faceZ, tipX, tipY, tipZ, direction);
        addArrowHeadLine(buffer, pose, matrix, tipX, tipY, tipZ, neckX, neckY, neckZ,
                firstPerpendicular, 0.42, direction);
        addArrowHeadLine(buffer, pose, matrix, tipX, tipY, tipZ, neckX, neckY, neckZ,
                firstPerpendicular, -0.42, direction);
        addArrowHeadLine(buffer, pose, matrix, tipX, tipY, tipZ, neckX, neckY, neckZ,
                secondPerpendicular, 0.42, direction);
        addArrowHeadLine(buffer, pose, matrix, tipX, tipY, tipZ, neckX, neckY, neckZ,
                secondPerpendicular, -0.42, direction);
    }

    private static void addArrowHeadLine(
                                         BufferBuilder buffer,
                                         PoseStack.Pose pose,
                                         Matrix4f matrix,
                                         double tipX,
                                         double tipY,
                                         double tipZ,
                                         double neckX,
                                         double neckY,
                                         double neckZ,
                                         double[] perpendicular,
                                         double scale,
                                         Direction direction) {
        addLine(
                buffer,
                pose,
                matrix,
                tipX,
                tipY,
                tipZ,
                neckX + perpendicular[0] * scale,
                neckY + perpendicular[1] * scale,
                neckZ + perpendicular[2] * scale,
                direction);
    }

    private static void addLine(
                                BufferBuilder buffer,
                                PoseStack.Pose pose,
                                Matrix4f matrix,
                                double startX,
                                double startY,
                                double startZ,
                                double endX,
                                double endY,
                                double endZ,
                                Direction direction) {
        buffer.addVertex(matrix, (float) startX, (float) startY, (float) startZ)
                .setColor(1f, 1f, 1f, 1f)
                .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
        buffer.addVertex(matrix, (float) endX, (float) endY, (float) endZ)
                .setColor(1f, 1f, 1f, 1f)
                .setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }
}
