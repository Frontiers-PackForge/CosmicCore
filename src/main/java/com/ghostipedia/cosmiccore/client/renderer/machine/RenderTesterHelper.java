package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashSet;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RenderTesterHelper extends
                                DynamicRender<WorkableElectricMultiblockMachine, RenderTesterHelper> {

    public static final RenderTesterHelper INSTANCE = new RenderTesterHelper();
    public static final Codec<RenderTesterHelper> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, RenderTesterHelper> TYPE = new DynamicRenderType<>(
            RenderTesterHelper.CODEC);

    private static final BiFunction<Direction, Direction, AABB> renderBoundCache = Util.memoize((front, upwards) -> {
        Direction up = RelativeDirection.UP.getRelative(front, upwards, false);
        Direction back = RelativeDirection.BACK.getRelative(front, upwards, false);
        Direction left = RelativeDirection.LEFT.getRelative(front, upwards, false);

        // offset from the controller to the inner cube (scaled up by 1 in all directions)
        // values are from the multi pattern
        BlockPos.MutableBlockPos minPos = new BlockPos.MutableBlockPos()
                .move(left, 3).move(up, 1).move(back, 2);
        BlockPos.MutableBlockPos maxPos = new BlockPos.MutableBlockPos()
                .move(left, -3).move(up, 7).move(back, 8);

        return new AABB(minPos, maxPos);
    });

    public static final ResourceLocation BLOOD_CUBE_TEXTURE = CosmicCore.id("block/iris/blood_cube");

    private static TextureAtlasSprite bloodCubeSprite = null;
    private static boolean isEventListenerRegistered = false;

    @SuppressWarnings("deprecation")
    private RenderTesterHelper() {
        if (!isEventListenerRegistered) {
            ModelUtils.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
                bloodCubeSprite = event.getAtlas().getSprite(BLOOD_CUBE_TEXTURE);
            });
            isEventListenerRegistered = true;
        }
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, RenderTesterHelper> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine multi) {
        if (multi.isFormed()) {
            AABB bounds = renderBoundCache.apply(multi.getFrontFacing(), multi.getUpwardsFacing());
            return bounds.move(multi.getPos());
        }
        return super.getRenderBoundingBox(multi);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        poseStack.pushPose();

        // move the things:tm: to render at the center of the multiblock
        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        Direction up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        Direction back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();

        // translate to the absolute center of the multiblock
        float x0ffset = 0, y0ffset = 0, z0ffset = 0;

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int upOffset = up.getNormal().get(axis);
            int backOffset = back.getNormal().get(axis);

            float offset = upOffset * (4.0f + (upOffset * 0.5f)) +
                    backOffset * (5.0f + (backOffset * 0.5f));
            switch (axis) {
                case X -> x0ffset = offset;
                case Y -> y0ffset = offset;
                case Z -> z0ffset = offset;
            }
        }
        poseStack.translate(
                x0ffset + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                y0ffset + (leftAxis == Direction.Axis.Y ? 0.5f : 0.0f),
                z0ffset + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateAxis(totalTick * Mth.TWO_PI / 160f, 0, 1, 0));
        float radius = 2.0f;
        float height = 15.0f;
        int sides = 48;
        int segments = 4;
        float r = 0.85f, g = 0.9f, b = 1.0f, a = 1f;

        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        Matrix4f mat = poseStack.last().pose();

        float dA = Mth.TWO_PI / sides;
        float dh = height / segments;
        float halfH = height * 0.5f;

        poseStack.popPose();
        poseStack.translate(0, 10, 0);
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(Mth.sin(totalTick / 20),
                        Mth.sin(totalTick / 30),
                        Mth.cos(Mth.HALF_PI + totalTick / 60))
                .rotateXYZ(55f * Mth.DEG_TO_RAD, 30f * Mth.DEG_TO_RAD, 0);
        poseStack.mulPose(rot);
        renderWireDodecahedron(poseStack, buffer,
                2f,
                0x66CCFF,
                1f);
        consumer = buffer.getBuffer(GTRenderTypes.getLightRing());

        Quaternionf frot = new Quaternionf()
                .rotateXYZ(Mth.sin(totalTick / 30),
                        Mth.sin(totalTick / 60),
                        Mth.cos(Mth.HALF_PI + totalTick / 40))
                .rotateXYZ(55f * Mth.DEG_TO_RAD, 30f * Mth.DEG_TO_RAD, 0);
        poseStack.mulPose(frot);
        renderWireDodecahedronThick(
                poseStack, buffer,
                4.0f,
                0.12f,
                0.4f, 0.9f, 1.0f, 0.85f);
        poseStack.popPose();
    }

    private static void renderWireDodecahedron(PoseStack poseStack, MultiBufferSource buffer,
                                               float scale, int sidesColorRGBA, float alpha) {
        float r = ((sidesColorRGBA >> 16) & 0xFF) / 255f;
        float g = ((sidesColorRGBA >> 8) & 0xFF) / 255f;
        float b = ((sidesColorRGBA) & 0xFF) / 255f;

        final float phi = (1f + (float) Math.sqrt(5.0)) * 0.5f;
        final float inv = 1f / phi;

        // build vertex list! Wahoo!
        final float[][] V = new float[20][3];
        int idx = 0;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sy = -1; sy <= 1; sy += 2)
                for (int sz = -1; sz <= 1; sz += 2)
                    V[idx++] = new float[] { sx, sy, sz };

        for (int s1 = -1; s1 <= 1; s1 += 2)
            for (int s2 = -1; s2 <= 1; s2 += 2) {
                V[idx++] = new float[] { 0f, s1 * inv, s2 * phi };
                V[idx++] = new float[] { s1 * inv, s2 * phi, 0f };
                V[idx++] = new float[] { s1 * phi, 0f, s2 * inv };
            }

        for (int i = 0; i < 20; i++) {
            V[i][0] *= scale;
            V[i][1] *= scale;
            V[i][2] *= scale;
        }

        float edge2 = Float.POSITIVE_INFINITY;
        for (int i = 0; i < 20; i++)
            for (int j = i + 1; j < 20; j++) {
                float dx = V[i][0] - V[j][0], dy = V[i][1] - V[j][1], dz = V[i][2] - V[j][2];
                float d2 = dx * dx + dy * dy + dz * dz;
                if (d2 > 1e-6f && d2 < edge2) edge2 = d2;
            }
        final float eps = edge2 * 1.0015f;

        var mat = poseStack.last().pose();
        var nrm = poseStack.last().normal();
        VertexConsumer vc = buffer.getBuffer(net.minecraft.client.renderer.RenderType.lines());

        for (int i = 0; i < 20; i++)
            for (int j = i + 1; j < 20; j++) {
                float dx = V[i][0] - V[j][0], dy = V[i][1] - V[j][1], dz = V[i][2] - V[j][2];
                float d2 = dx * dx + dy * dy + dz * dz;
                if (d2 <= eps) {
                    float len = (float) Math.sqrt(d2);
                    float nx = dx / len, ny = dy / len, nz = dz / len;

                    vc.vertex(mat, V[i][0], V[i][1], V[i][2])
                            .color(r, g, b, alpha).normal(nrm, nx, ny, nz).endVertex();
                    vc.vertex(mat, V[j][0], V[j][1], V[j][2])
                            .color(r, g, b, alpha).normal(nrm, nx, ny, nz).endVertex();
                }
            }
    }

    private static void renderWireDodecahedronThick(PoseStack poseStack, MultiBufferSource buffer,
                                                    float scale, float thickness,
                                                    float r, float g, float b, float a) {
        final float phi = (1f + (float) Math.sqrt(5.0)) * 0.5f;
        final float inv = 1f / phi;

        final float[][] V = new float[20][3];
        int idx = 0;
        for (int sx = -1; sx <= 1; sx += 2)
            for (int sy = -1; sy <= 1; sy += 2)
                for (int sz = -1; sz <= 1; sz += 2)
                    V[idx++] = new float[] { sx, sy, sz };

        for (int s1 = -1; s1 <= 1; s1 += 2)
            for (int s2 = -1; s2 <= 1; s2 += 2) {
                V[idx++] = new float[] { 0f, s1 * inv, s2 * phi };
                V[idx++] = new float[] { s1 * inv, s2 * phi, 0f };
                V[idx++] = new float[] { s1 * phi, 0f, s2 * inv };
            }

        for (int i = 0; i < 20; i++) {
            V[i][0] *= scale;
            V[i][1] *= scale;
            V[i][2] *= scale;
        }

        HashSet<Long> edges = new java.util.HashSet<>(64);
        for (int i = 0; i < 20; i++) {
            int n1 = -1, n2 = -1, n3 = -1;
            float d1 = Float.POSITIVE_INFINITY, d2 = Float.POSITIVE_INFINITY, d3 = Float.POSITIVE_INFINITY;

            float xi = V[i][0], yi = V[i][1], zi = V[i][2];
            for (int j = 0; j < 20; j++) if (j != i) {
                float dx = xi - V[j][0], dy = yi - V[j][1], dz = zi - V[j][2];
                float d = dx * dx + dy * dy + dz * dz;
                if (d < d1) {
                    d3 = d2;
                    n3 = n2;
                    d2 = d1;
                    n2 = n1;
                    d1 = d;
                    n1 = j;
                } else if (d < d2) {
                    d3 = d2;
                    n3 = n2;
                    d2 = d;
                    n2 = j;
                } else if (d < d3) {
                    d3 = d;
                    n3 = j;
                }
            }
            int[] ns = { n1, n2, n3 };
            for (int j : ns) {
                int low = Math.min(i, j);
                int high = Math.max(i, j);
                long key = ((long) low << 32) | (high & 0xFFFFFFFFL);
                edges.add(key);
            }
        }
        VertexConsumer vertexConsumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        final Matrix4f mat = poseStack.last().pose();

        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        var v3 = cam.getLookVector();
        float vx = (float) v3.x, vy = (float) v3.y, vz = (float) v3.z;
        float vlen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vlen > 0f) {
            vx /= vlen;
            vy /= vlen;
            vz /= vlen;
        }

        final float EPS = 1e-6f;
        for (long key : edges) {
            vertexConsumer = buffer.getBuffer(GTRenderTypes.getLightRing());
            int i0 = (int) (key >>> 32);
            int i1 = (int) (key & 0xFFFFFFFFL);

            float x0 = V[i0][0], y0 = V[i0][1], z0 = V[i0][2];
            float x1 = V[i1][0], y1 = V[i1][1], z1 = V[i1][2];

            float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
            float L2 = dx * dx + dy * dy + dz * dz;
            if (L2 < EPS) continue;
            float L = (float) Math.sqrt(L2);
            float ex = dx / L, ey = dy / L, ez = dz / L;
            float nx = ey * vz - ez * vy;
            float ny = ez * vx - ex * vz;
            float nz = ex * vy - ey * vx;
            float n2 = nx * nx + ny * ny + nz * nz;
            if (n2 < EPS) {
                float upx = 0f, upy = 1f, upz = 0f;
                nx = ey * upz - ez * upy;
                ny = ez * upx - ex * upz;
                nz = ex * upy - ey * upx;
                n2 = nx * nx + ny * ny + nz * nz;
                if (n2 < EPS) {
                    nx = 1f;
                    ny = 0f;
                    nz = 0f;
                    n2 = 1f;
                }
            }
            float s = (0.5f * thickness) / (float) Math.sqrt(n2);
            float ox = nx * s, oy = ny * s, oz = nz * s;
            float ax = x0 - ox, ay = y0 - oy, az = z0 - oz;
            float bx = x0 + ox, by = y0 + oy, bz = z0 + oz;
            float cx = x1 + ox, cy = y1 + oy, cz = z1 + oz;
            float dxq = x1 - ox, dyq = y1 - oy, dzq = z1 - oz;

            // Tri 1: A,B,C
            vertexConsumer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();
            vertexConsumer.vertex(mat, bx, by, bz).color(r, g, b, a).endVertex();
            vertexConsumer.vertex(mat, cx, cy, cz).color(r, g, b, a).endVertex();

            // Tri 2: A,C,D
            vertexConsumer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();
            vertexConsumer.vertex(mat, cx, cy, cz).color(r, g, b, a).endVertex();
            vertexConsumer.vertex(mat, dxq, dyq, dzq).color(r, g, b, a).endVertex();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBloodCube(PoseStack poseStack, MultiBufferSource bufferSource, float totalTick) {
        poseStack.pushPose();
        // rotate around center
        Quaternionf rot = new Quaternionf()
                .rotateXYZ(Mth.sin(totalTick / 20),
                        Mth.sin(totalTick / 30),
                        Mth.cos(Mth.HALF_PI + totalTick / 60))
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
    private void renderRings(Direction.Axis upAxis, float totalTick, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.getLightRing());

        float xRot = totalTick / 20;
        float zRot = Mth.HALF_PI + totalTick / 60;
        float yRot = totalTick / 30;
        float sinX = Mth.sin(xRot), cosX = Mth.cos(xRot);
        float sinY = Mth.sin(yRot), cosY = Mth.cos(yRot);
        float sinZ = Mth.sin(zRot), cosZ = Mth.cos(zRot);

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(sinX, cosY, sinZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2f, 0.1F, 10, 36,
                0.5F, 0, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(cosX, sinY, sinZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.8f, 0.1F, 10, 36,
                0.4F, 0f, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateZ(cosZ));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.6f, 0.1F, 10, 36,
                0.6F, 0, 0, 1, upAxis);
        poseStack.popPose();
    }
}
