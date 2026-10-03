package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.client.renderer.CosmicCoreRenderTypes;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;

import com.breakinblocks.neovitae.api.stream.StreamEffect;
import com.breakinblocks.neovitae.client.render.stream.ActiveStream;
import com.breakinblocks.neovitae.client.render.stream.StreamRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

final class PylonBloodStream extends ActiveStream {

    private final double[][] points = new double[65][3];
    private final float[][] colors = new float[65][4];
    private final float[] radii = new float[65];
    private int age;

    PylonBloodStream() {
        super("cosmiccore:pylon", StreamEffect.builder(0, 0, 0).to(0, 0, 0)
                .color(0xA51024).glow(true).tubeSegments(10).build(), 0);
    }

    void render(PoseStack pose, MultiBufferSource buffer, Vec3 start, Vec3 end, double time,
                float strength, boolean spiral, int strand) {
        age = (int) time % 20;
        Vec3 sideways = end.subtract(start).cross(new Vec3(0, 1, 0)).normalize();
        for (int i = 0; i < points.length; i++) {
            double t = i / (double) (points.length - 1);
            Vec3 point = start.lerp(end, t);
            if (spiral) {
                double radius = 0.35 + 0.4 * Math.sin(t * Math.PI);
                double angle = strand * Math.PI + t * Math.PI * 4 - time * 0.025;
                point = point.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                Vec3 offset = point.subtract(end);
                double distance = offset.length();
                double surface = ImbumentPylonRender.sphereRadius(time) + 0.06;
                double blend = Math.max(0, 0.15 - Math.abs(distance - surface)) / 0.15;
                double attached = Math.max(distance, surface) + blend * blend * 0.15 * 0.25;
                point = end.add(offset.scale(attached / distance));
            } else {
                double envelope = Math.sin(Math.PI * t);
                point = point.add(0, envelope * (Math.min(3, start.distanceTo(end) * 0.15) +
                        0.18 * Math.sin(t * 16 - time * 0.13)), 0)
                        .add(sideways.scale(envelope * 0.16 * Math.sin(t * 20 - time * 0.16)));
            }
            points[i][0] = point.x;
            points[i][1] = point.y;
            points[i][2] = point.z;
            float wave = (float) (0.5 + 0.5 * Math.sin(t * 24 - time * 0.24));
            float pulse = spiral ? 0.85f + 0.15f * wave : 0.55f + 0.45f * wave * wave;
            float fade = spiral ? (float) Math.clamp((1 - t) / 0.25, 0, 1) : 1;
            fade = fade * fade * (3 - 2 * fade);
            float brightness = 0.75f + 0.25f * wave;
            colors[i][0] = 0.65f * brightness;
            colors[i][1] = 0.025f * brightness;
            colors[i][2] = 0.07f * brightness;
            colors[i][3] = strength * 0.85f * fade;
            float radius = spiral ? 0.045f * pulse : Math.max(0, 0.36f * pulse - 0.1f);
            radii[i] = radius * strength * fade;
        }
        StreamRenderer.render(this, pose, buffer.getBuffer(CosmicCoreRenderTypes.vitaeSphere()),
                (float) (time - Math.floor(time)));
    }

    @Override
    public double[][] getPositions() {
        return points;
    }

    @Override
    public float[][] getColors() {
        return colors;
    }

    @Override
    public float[] getRadii() {
        return radii;
    }

    @Override
    public int getAge() {
        return age;
    }
}
