package com.ghostipedia.cosmiccore.api.utility.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class LaserUtil {
    // todo: replace rotation angle/axis methods with gtm api when the single block rotation pr gets merged

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @return the angle of rotation to make {@code from} point in the direction of {@code to}
     */
    @Contract(pure = true)
    public static float getRotationAngle(final Vector3f from, final Vector3f to) {
        return (float) Math.acos(from.dot(to));
    }

    /**
     * This method isn't pure, {@code from} will be modified!
     *
     * @param from the original vector
     * @param to   the wanted vector
     * @return the axis of rotation to make {@code from} point in the direction of {@code to}
     */
    public static Vector3f getRotationAxis(Vector3f from, final Vector3f to) {
        return getRotationAxis(from, to, from);
    }

    /**
     * @param from the original vector
     * @param to   the wanted vector
     * @param dest the vector to save the result to
     * @return {@code dest}
     */
    public static Vector3f getRotationAxis(Vector3f from, Vector3f to, Vector3f dest) {
        return from.cross(to, dest).normalize();
    }

    // laser related things

    public static void renderLaser(Vector3f ray, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float red, float green, float blue, float alpha, float partialTick, long gameTime) {
        renderLaser(ray, poseStack, bufferSource, red, green, blue, alpha, partialTick, gameTime,
                BeaconRenderer.BEAM_LOCATION);
    }

    public static void renderLaser(Vector3f ray, PoseStack poseStack, MultiBufferSource bufferSource,
                                   float red, float green, float blue, float alpha, float partialTick, long gameTime,
                                   ResourceLocation texture) {
        poseStack.pushPose();
        // calculate rotation to point in ray direction
        var beamDirection = new Vector3f(0, 1, 0);
        var wantedNorm = ray.normalize(new Vector3f());
        var angle = getRotationAngle(beamDirection, wantedNorm);
        var axis = getRotationAxis(beamDirection, wantedNorm);
        poseStack.mulPose(Axis.of(axis).rotation(angle));
        // calculate beam length and self rotation
        var beamLength = ray.length();
        var step = Math.floorMod(gameTime, 40) + partialTick;
        var vRelated = Mth.frac(step * 0.2F - (float) Mth.floor(step * 0.1F));
        var maxV = -1.0F + vRelated;
        var minV = beamLength * (0.5F / .1f) + maxV;
        poseStack.mulPose(Axis.YP.rotationDegrees(step * 2.25F - 45.0F));
        renderPart(poseStack, bufferSource.getBuffer(RenderType.beaconBeam(texture, false)),
                red, green, blue, alpha, 0, beamLength,
                0, .1f, .1f, 0, -.1f, 0, 0, -.1f, 0, 1, minV, maxV);

        poseStack.popPose();
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue,
                                  float alpha, float minY, float maxY, float x0, float z0, float x1, float z1, float x2,
                                  float z2, float x3, float z3, float minU, float maxU, float minV, float maxV) {
        var pose = poseStack.last();
        var mat = pose.pose();
        var normalMat = pose.normal();
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3,
                minU, maxU, minV, maxV);
        renderQuad(mat, normalMat, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0,
                minU, maxU, minV, maxV);
    }

    public static void renderQuad(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float red, float green,
                                  float blue, float alpha, float minY, float maxY, float minX, float minZ,
                                  float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxY, minX, minZ, maxU, minV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minY, minX, minZ, maxU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxY, maxX, maxZ, minU, minV);
    }

    public static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float red, float green,
                                 float blue, float alpha, float y, float x, float z, float u, float v) {
        consumer.vertex(pose, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
