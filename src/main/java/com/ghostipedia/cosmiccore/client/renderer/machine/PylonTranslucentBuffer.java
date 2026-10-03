package com.ghostipedia.cosmiccore.client.renderer.machine;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class PylonTranslucentBuffer implements MultiBufferSource {

    private final List<Quad> pool = new ArrayList<>();
    private final List<Quad> ordered = new ArrayList<>();
    private final Map<RenderType, Capture> captures = new IdentityHashMap<>();
    private final MultiBufferSource.BufferSource output = MultiBufferSource.immediate(new ByteBufferBuilder(65536));

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        if (type.mode() != VertexFormat.Mode.QUADS) return output.getBuffer(type);
        return captures.computeIfAbsent(type, Capture::new);
    }

    void draw(MultiBufferSource parent) {
        if (parent instanceof MultiBufferSource.BufferSource shared) shared.endBatch(Sheets.solidBlockSheet());
        for (Quad quad : ordered) {
            float x = 0, y = 0, z = 0;
            for (Vertex vertex : quad.vertices) {
                x += vertex.x;
                y += vertex.y;
                z += vertex.z;
            }
            quad.distance = x * x + y * y + z * z;
        }
        ordered.sort(Comparator.comparingDouble((Quad quad) -> quad.distance).reversed());
        for (Quad quad : ordered) {
            VertexConsumer consumer = output.getBuffer(quad.type);
            for (Vertex vertex : quad.vertices) vertex.emit(consumer);
        }
        output.endBatch();
        ordered.clear();
        for (Capture capture : captures.values()) capture.index = 0;
    }

    private static final class Quad {

        private final Vertex[] vertices = { new Vertex(), new Vertex(), new Vertex(), new Vertex() };
        private RenderType type;
        private float distance;
    }

    private static final class Vertex {

        private float x, y, z, u, v, nx, ny, nz;
        private int red, green, blue, alpha, overlayU, overlayV, lightU, lightV;

        private void emit(VertexConsumer consumer) {
            consumer.addVertex(x, y, z).setColor(red, green, blue, alpha).setUv(u, v)
                    .setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(nx, ny, nz);
        }
    }

    private final class Capture implements VertexConsumer {

        private final RenderType type;
        private Quad quad;
        private Vertex vertex;
        private int index;

        private Capture(RenderType type) {
            this.type = type;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (index == 0) {
                if (ordered.size() == pool.size()) pool.add(new Quad());
                quad = pool.get(ordered.size());
                quad.type = type;
                ordered.add(quad);
            }
            vertex = quad.vertices[index];
            index = (index + 1) % 4;
            vertex.x = x;
            vertex.y = y;
            vertex.z = z;
            vertex.red = vertex.green = vertex.blue = vertex.alpha = 255;
            vertex.u = vertex.v = vertex.nx = vertex.ny = vertex.nz = 0;
            vertex.overlayU = vertex.overlayV = vertex.lightU = vertex.lightV = 0;
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            vertex.red = red;
            vertex.green = green;
            vertex.blue = blue;
            vertex.alpha = alpha;
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            vertex.u = u;
            vertex.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            vertex.overlayU = u;
            vertex.overlayV = v;
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            vertex.lightU = u;
            vertex.lightV = v;
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            vertex.nx = x;
            vertex.ny = y;
            vertex.nz = z;
            return this;
        }
    }
}
