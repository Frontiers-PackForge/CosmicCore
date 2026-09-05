package com.ghostipedia.cosmiccore.client.renderer.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

final class PowerTowerWireMesh implements AutoCloseable {

    private static final RenderType LAYER = RenderType
            .entitySolid(CosmicCore.id("textures/effect/power_tower_wire.png"));
    private final VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private final Vec3 origin;

    PowerTowerWireMesh(PowerTowerWireGeometry geometry) {
        origin = geometry.segments().getFirst().start();
        try (var memory = new ByteBufferBuilder(262144)) {
            var builder = new BufferBuilder(memory, LAYER.mode(), LAYER.format());
            List<Vec3> path = new ArrayList<>();
            int index = 0;
            for (var segment : geometry.segments()) {
                if (!path.isEmpty() && !segment.start().equals(geometry.segments().get(index - 1).end())) {
                    emitPath(builder, path);
                    path.clear();
                }
                if (path.isEmpty()) path.add(segment.start());
                path.add(segment.end());
                index++;
            }
            if (!path.isEmpty()) emitPath(builder, path);
            buffer.bind();
            buffer.upload(builder.buildOrThrow());
            VertexBuffer.unbind();
        }
    }

    private void emitPath(BufferBuilder builder, List<Vec3> path) {
        List<Vec3> points = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i += 4) points.add(path.get(i));
        points.add(path.getLast());
        Vec3[][] rings = new Vec3[points.size()][4];
        for (int i = 0; i < points.size(); i++) {
            Vec3 tangent = points.get(Math.min(i + 1, points.size() - 1))
                    .subtract(points.get(Math.max(0, i - 1))).normalize();
            Vec3 side = tangent.cross(new Vec3(0, 1, 0));
            if (side.lengthSqr() < 1.0E-8) side = tangent.cross(new Vec3(1, 0, 0));
            side = side.normalize().scale(PowerTowerWireGeometry.RADIUS / Math.sqrt(2));
            Vec3 up = side.normalize().cross(tangent).scale(PowerTowerWireGeometry.RADIUS / Math.sqrt(2));
            rings[i][0] = points.get(i).add(side).add(up);
            rings[i][1] = points.get(i).subtract(side).add(up);
            rings[i][2] = points.get(i).subtract(side).subtract(up);
            rings[i][3] = points.get(i).add(side).subtract(up);
        }
        for (int i = 0; i < points.size() - 1; i++) {
            int light = LevelRenderer.getLightColor(Minecraft.getInstance().level, BlockPos.containing(points.get(i)));
            for (int side = 0; side < 4; side++) {
                int next = (side + 1) % 4;
                Vec3 normal = rings[i][side].add(rings[i][next]).scale(0.5).subtract(points.get(i)).normalize();
                vertex(builder, rings[i][side], normal, 0, 0, light);
                vertex(builder, rings[i + 1][side], normal, 0, 1, light);
                vertex(builder, rings[i + 1][next], normal, 1, 1, light);
                vertex(builder, rings[i][next], normal, 1, 0, light);
            }
        }
    }

    private void vertex(BufferBuilder builder, Vec3 point, Vec3 normal, float u, float v, int light) {
        Vec3 local = point.subtract(origin);
        builder.addVertex((float) local.x, (float) local.y, (float) local.z).setColor(90, 90, 90, 255)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    void render(RenderLevelStageEvent event) {
        Vec3 camera = event.getCamera().getPosition();
        var matrix = new Matrix4f(event.getModelViewMatrix()).translate((float) (origin.x - camera.x),
                (float) (origin.y - camera.y), (float) (origin.z - camera.z));
        LAYER.setupRenderState();
        try {
            var shader = RenderSystem.getShader();
            if (shader == null) return;
            if (shader.CHUNK_OFFSET != null) shader.CHUNK_OFFSET.set(0.0f, 0.0f, 0.0f);
            buffer.bind();
            buffer.drawWithShader(matrix, event.getProjectionMatrix(), shader);
        } finally {
            VertexBuffer.unbind();
            LAYER.clearRenderState();
        }
    }

    @Override
    public void close() {
        buffer.close();
    }
}
