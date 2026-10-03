package com.ghostipedia.cosmiccore.client.renderer.machine;

import net.minecraft.client.renderer.MultiBufferSource;

import com.breakinblocks.neovitae.api.stream.StreamEffect;
import com.breakinblocks.neovitae.client.render.stream.ActiveStream;
import com.breakinblocks.neovitae.client.render.stream.StreamRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

public final class TransfuserBloodStreams extends ActiveStream {

    private final double[][] points = new double[65][3];
    private final float[][] colors = new float[65][4];
    private final float[] radii = new float[65];
    private int renderAge;

    public TransfuserBloodStreams() {
        super("cosmiccore:transfuser", StreamEffect.builder(0, 0, 0)
                .to(0, 0, 0).color(0xA51024).glow(true).tubeSegments(6).build(), 0);
    }

    public void render(PoseStack pose, MultiBufferSource buffer, double time) {
        renderAge = (int) time % 20;
        for (int strand = 0; strand < 3; strand++) {
            for (int i = 0; i < points.length; i++) {
                double t = i / (double) (points.length - 1);
                double angle = strand * Math.PI * 2 / 3 + t * Math.PI * 3 - time * 0.018;
                double radius = 0.5 + 0.55 * Math.sin(t * Math.PI);
                double capture = Math.max(0, (t - 0.82) / 0.18);
                radius *= 1 - capture;
                points[i][0] = Math.cos(angle) * radius;
                points[i][1] = -3.70 + 4.62 * t;
                points[i][2] = Math.sin(angle) * radius;
                float pulse = (float) (0.8 + 0.2 * Math.sin(t * 28 - time * 0.18));
                colors[i][0] = 0.65f * pulse;
                colors[i][1] = 0.025f * pulse;
                colors[i][2] = 0.07f * pulse;
                colors[i][3] = (float) (0.85 * (1 - capture));
                radii[i] = (float) (0.065 * pulse * (1 - capture));
            }
            StreamRenderer.render(this, pose, buffer.getBuffer(StreamRenderer.STREAM_GLOW_TYPE),
                    (float) (time - Math.floor(time)));
        }
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
        return renderAge;
    }
}
