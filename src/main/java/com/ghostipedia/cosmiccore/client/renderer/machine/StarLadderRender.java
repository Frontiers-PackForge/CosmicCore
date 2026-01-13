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
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.HashSet;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StarLadderRender extends
                              DynamicRender<WorkableElectricMultiblockMachine, StarLadderRender> {

    public static final StarLadderRender INSTANCE = new StarLadderRender();
    public static final Codec<StarLadderRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, StarLadderRender> TYPE = new DynamicRenderType<>(
            StarLadderRender.CODEC);

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
    private StarLadderRender() {
        if (!isEventListenerRegistered) {
            ModelUtils.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
                bloodCubeSprite = event.getAtlas().getSprite(BLOOD_CUBE_TEXTURE);
            });
            isEventListenerRegistered = true;
        }
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, StarLadderRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine multi) {
        return new AABB(multi.getPos()).inflate(getViewDistance(), 256, getViewDistance());
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

        // Position the pillar 11 blocks in front of the controller
        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        Direction frontDir = RelativeDirection.FRONT.getRelative(front, upwards, flipped);
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();

        float x0ffset = 0, y0ffset = 0.5f, z0ffset = 0;

        // Calculate offset 11 blocks in front of controller
        Vec3i frontNormal = frontDir.getNormal();
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int frontOffset = frontNormal.get(axis);
            float offset = frontOffset * 10.5f;
            switch (axis) {
                case X -> x0ffset = offset;
                case Y -> y0ffset = 0.5f;
                case Z -> z0ffset = offset;
            }
        }

        poseStack.translate(
                x0ffset + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                y0ffset,
                z0ffset + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));

        // Render the massive woven cable pillar
        renderWovenCablePillar(poseStack, buffer, totalTick, packedLight, packedOverlay);

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
            int[] ns = getInts(V, i);
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

    private static int @NotNull [] getInts(float[][] V, int i) {
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
        return ns;
    }

    public static void renderSolidSphere(PoseStack poseStack, MultiBufferSource buffer,
                                         float cx, float cy, float cz,
                                         float radius, int slices, int stacks,
                                         float r, float g, float b, float a) {
        Matrix4f mat = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(GTRenderTypes.getLightRing());

        float dPhi = (float) (Mth.TWO_PI / Math.max(3, slices));
        float dTheta = (float) (Math.PI / Math.max(2, stacks));

        for (int i = 0; i < stacks; i++) {
            float th0 = i * dTheta;
            float th1 = (i + 1) * dTheta;
            float sin0 = Mth.sin(th0), cos0 = Mth.cos(th0);
            float sin1 = Mth.sin(th1), cos1 = Mth.cos(th1);

            // one triangle strip per latitude band; <= closes seam
            for (int j = 0; j <= slices; j++) {
                float ph = j * dPhi;
                float cosp = Mth.cos(ph), sinp = Mth.sin(ph);

                // band top (th0)
                float x0 = cx + radius * sin0 * cosp;
                float y0 = cy + radius * cos0;
                float z0 = cz + radius * sin0 * sinp;

                // band bottom (th1)
                float x1 = cx + radius * sin1 * cosp;
                float y1 = cy + radius * cos1;
                float z1 = cz + radius * sin1 * sinp;

                // order chosen for typical backface cull; swap if it looks inside-out
                vc.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
                vc.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
            }
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
    private void renderWovenCablePillar(PoseStack poseStack, MultiBufferSource buffer,
                                        float totalTick, int packedLight, int packedOverlay) {
        int numStrands = 4;
        float pillarHeight = 2048f;
        float coreRadius = 3.0f;
        float strandRadius = 0.85f;
        float helixRadius = 3.8f;
        float windingSpeed = 0.03f;

        float animTime = -totalTick * 0.10f;

        // Render central core column first (back to front for translucency)
        VertexConsumer coreConsumer = buffer.getBuffer(GTRenderTypes.getLightRing());
        renderCoreColumn(poseStack, coreConsumer, pillarHeight, coreRadius, totalTick, packedLight, packedOverlay);

        // Render counter-rotating helix layers for structural stability look
        // First layer - clockwise spiral
        for (int strand = 0; strand < numStrands; strand++) {
            VertexConsumer strandConsumer = buffer.getBuffer(GTRenderTypes.getLightRing());
            float strandAngleOffset = (strand / (float) numStrands) * Mth.TWO_PI;
            renderBraidedStrand(poseStack, strandConsumer, pillarHeight, helixRadius, strandRadius,
                    strandAngleOffset, animTime, windingSpeed, packedLight, packedOverlay, true);
        }

        // Second layer - counter-clockwise spiral (creates woven/braided effect)
        for (int strand = 0; strand < numStrands; strand++) {
            VertexConsumer strandConsumer = buffer.getBuffer(GTRenderTypes.getLightRing());
            float strandAngleOffset = (strand / (float) numStrands) * Mth.TWO_PI + (Mth.PI / numStrands);
            renderBraidedStrand(poseStack, strandConsumer, pillarHeight, helixRadius * 0.95f, strandRadius * 0.8f,
                    strandAngleOffset, animTime, -windingSpeed, packedLight, packedOverlay, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderBraidedStrand(PoseStack poseStack, VertexConsumer consumer, float height,
                                     float helixRadius, float strandRadius, float angleOffset,
                                     float animTime, float windingSpeed, int packedLight, int packedOverlay,
                                     boolean isClockwise) {
        int segments = 256;
        float segmentHeight = height / segments;

        Matrix4f mat = poseStack.last().pose();

        // Different colors for each layer to show the braiding
        float r = isClockwise ? 0.1f : 0.15f;
        float g = isClockwise ? 0.1f : 0.15f;
        float b = isClockwise ? 0.1f : 0.15f;

        for (int i = 0; i < segments; i++) {
            float y1 = i * segmentHeight;
            float y2 = (i + 1) * segmentHeight;

            float angle1 = (y1 * windingSpeed + angleOffset + animTime) % Mth.TWO_PI;
            float angle2 = (y2 * windingSpeed + angleOffset + animTime) % Mth.TWO_PI;

            float x1 = helixRadius * Mth.cos(angle1);
            float z1 = helixRadius * Mth.sin(angle1);
            float x2 = helixRadius * Mth.cos(angle2);
            float z2 = helixRadius * Mth.sin(angle2);

            // Draw cylindrical tube segment
            drawTubeSegment(mat, consumer, x1, y1, z1, x2, y2, z2, strandRadius, r, g, b, 1f);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void renderCoreColumn(PoseStack poseStack, VertexConsumer consumer, float height,
                                  float radius, float totalTick, int packedLight, int packedOverlay) {
        int segments = 256;
        float segmentHeight = height / segments;

        Matrix4f mat = poseStack.last().pose();

        for (int i = 0; i < segments; i++) {
            float y1 = i * segmentHeight;
            float y2 = (i + 1) * segmentHeight;

            float glow = 0.7f + 0.3f * Mth.sin(totalTick * 0.05f + y1 * 0.1f);

            // Draw central glowing column
            drawTubeSegment(mat, consumer, 0, y1, 0, 0, y2, 0, radius,
                    0.3f * glow, 0.45f * glow, 0.6f * glow, 1f);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawTubeSegment(Matrix4f mat, VertexConsumer consumer,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float radius, float r, float g, float b, float a) {
        int sides = 4;
        float angleStep = Mth.TWO_PI / sides;

        for (int i = 0; i < sides; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;

            float cos1 = Mth.cos(angle1);
            float sin1 = Mth.sin(angle1);
            float cos2 = Mth.cos(angle2);
            float sin2 = Mth.sin(angle2);

            // First triangle of quad
            consumer.vertex(mat, x1 + radius * cos1, y1, z1 + radius * sin1).color(r, g, b, a).endVertex();
            consumer.vertex(mat, x1 + radius * cos2, y1, z1 + radius * sin2).color(r, g, b, a).endVertex();
            consumer.vertex(mat, x2 + radius * cos2, y2, z2 + radius * sin2).color(r, g, b, a).endVertex();

            // Second triangle of quad
            consumer.vertex(mat, x1 + radius * cos1, y1, z1 + radius * sin1).color(r, g, b, a).endVertex();
            consumer.vertex(mat, x2 + radius * cos2, y2, z2 + radius * sin2).color(r, g, b, a).endVertex();
            consumer.vertex(mat, x2 + radius * cos1, y2, z2 + radius * sin1).color(r, g, b, a).endVertex();
        }
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
