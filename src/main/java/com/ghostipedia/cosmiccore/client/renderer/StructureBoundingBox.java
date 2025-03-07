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
import org.joml.Matrix3f;
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
            BufferBuilder buffer = tesselator.getBuilder();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
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

            tesselator.end();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
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

            tesselator.end();

            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            RenderSystem.lineWidth(12);

            var direction = getDir(player.getMainHandItem());
            var dirs = DebugBlockPattern.getDir(direction);
            var cSign = dirs[0].axis;
            var sSign = dirs[1].axis;
            var aSign = dirs[2].axis;
            // I Dislike this
            Matrix4f mat4 = poseStack.last().pose();
            Matrix3f mat3 = new Matrix3f(mat4);
            // cSign
            buffer.vertex(mat4, poses[0].getX(), poses[0].getY(), poses[0].getZ()).color(1f, 0f, 0f, 0.75f)
                    .normal(mat3,
                            cSign == Direction.Axis.X ? 1 : 0,
                            cSign == Direction.Axis.Y ? 1 : 0,
                            cSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            buffer.vertex(mat4,
                    cSign == Direction.Axis.X ? poses[0].getX() + 1.5f : poses[0].getX(),
                    cSign == Direction.Axis.Y ? poses[0].getY() + 1.5f : poses[0].getY(),
                    cSign == Direction.Axis.Z ? poses[0].getZ() + 1.5f : poses[0].getZ())
                    .color(1f, 0f, 0f, 0.75f)
                    .normal(mat3,
                            cSign == Direction.Axis.X ? 1 : 0,
                            cSign == Direction.Axis.Y ? 1 : 0,
                            cSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            // sSign
            buffer.vertex(mat4, poses[0].getX(), poses[0].getY(), poses[0].getZ()).color(0f, 1f, 0f, 0.75f)
                    .normal(mat3,
                            sSign == Direction.Axis.X ? 1 : 0,
                            sSign == Direction.Axis.Y ? 1 : 0,
                            sSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            buffer.vertex(mat4,
                    sSign == Direction.Axis.X ? poses[0].getX() + 1.5f : poses[0].getX(),
                    sSign == Direction.Axis.Y ? poses[0].getY() + 1.5f : poses[0].getY(),
                    sSign == Direction.Axis.Z ? poses[0].getZ() + 1.5f : poses[0].getZ())
                    .color(0f, 1f, 0f, 0.75f)
                    .normal(mat3,
                            sSign == Direction.Axis.X ? 1 : 0,
                            sSign == Direction.Axis.Y ? 1 : 0,
                            sSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            // aSign
            buffer.vertex(mat4, poses[0].getX(), poses[0].getY(), poses[0].getZ()).color(0f, 0f, 1f, 0.75f)
                    .normal(mat3,
                            aSign == Direction.Axis.X ? 1 : 0,
                            aSign == Direction.Axis.Y ? 1 : 0,
                            aSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            buffer.vertex(mat4,
                    aSign == Direction.Axis.X ? poses[0].getX() + 1.5f : poses[0].getX(),
                    aSign == Direction.Axis.Y ? poses[0].getY() + 1.5f : poses[0].getY(),
                    aSign == Direction.Axis.Z ? poses[0].getZ() + 1.5f : poses[0].getZ())
                    .color(0f, 0f, 1f, 0.75f)
                    .normal(mat3,
                            aSign == Direction.Axis.X ? 1 : 0,
                            aSign == Direction.Axis.Y ? 1 : 0,
                            aSign == Direction.Axis.Z ? 1 : 0)
                    .endVertex();
            tesselator.end();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }
}
