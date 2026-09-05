package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentPlan;

import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LeylineStructureMesh implements AutoCloseable {

    private final List<Batch> batches;

    private LeylineStructureMesh(List<Quad> quads) {
        Map<BatchKey, List<Quad>> grouped = new LinkedHashMap<>();
        for (Quad quad : quads) {
            var key = new BatchKey(quad.layer, quad.section, Math.floorDiv(Mth.floor(quad.minY), 4));
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(quad);
        }
        batches = grouped.entrySet().stream().map(entry -> new Batch(entry.getKey(), entry.getValue())).toList();
    }

    public static LeylineStructureMesh bake(LeylineDeploymentPlan plan, BlockAndTintGetter lighting) {
        var minecraft = Minecraft.getInstance();
        var dispatcher = minecraft.getBlockRenderer();
        var blocks = new Long2ReferenceOpenHashMap<BlockState>(plan.worldPlacements().size());
        plan.worldPlacements().forEach(placement -> blocks.put(placement.pos().asLong(), placement.state()));
        var world = new LitBlueprint(new MutableSchema(blocks).getLevel(), lighting);
        var result = new ArrayList<Quad>();
        PoseStack poses = new PoseStack();
        RandomSource random = RandomSource.create();
        for (var placement : plan.worldPlacements()) {
            BlockState state = placement.state();
            if (state.isAir()) continue;
            var model = dispatcher.getBlockModel(state);
            var blockEntity = world.getBlockEntity(placement.pos());
            ModelData data = model.getModelData(world, placement.pos(), state,
                    blockEntity == null ? ModelData.EMPTY : blockEntity.getModelData());
            BlockPos relative = placement.pos().subtract(plan.controllerPos());
            poses.pushPose();
            poses.translate(relative.getX(), relative.getY(), relative.getZ());
            random.setSeed(state.getSeed(placement.pos()));
            for (RenderType layer : model.getRenderTypes(state, random, data)) {
                var capture = new QuadCapture(result, layer, SectionPos.asLong(placement.pos()));
                dispatcher.getModelRenderer().tesselateBlock(world, model, state, placement.pos(), poses,
                        capture, true, random, state.getSeed(placement.pos()), OverlayTexture.NO_OVERLAY, data, layer);
                capture.finish();
            }
            poses.popPose();
        }
        result.sort(Comparator.comparingInt(quad -> RenderType.chunkBufferLayers().indexOf(quad.layer)));
        return new LeylineStructureMesh(result);
    }

    public void render(RenderLevelStageEvent event, BlockPos anchor, double offset, double clipY,
                       Set<Long> completedSections) {
        var camera = event.getCamera().getPosition();
        var matrix = new Matrix4f(event.getModelViewMatrix()).translate((float) (anchor.getX() - camera.x),
                (float) (anchor.getY() + offset - camera.y), (float) (anchor.getZ() - camera.z));
        var visible = new ArrayList<Batch>();
        for (Batch batch : batches) {
            if (completedSections.contains(batch.key.section) || batch.minY >= clipY ||
                    !atStage(batch.key.layer, event.getStage()))
                continue;
            int x = SectionPos.sectionToBlockCoord(SectionPos.x(batch.key.section));
            int y = SectionPos.sectionToBlockCoord(SectionPos.y(batch.key.section));
            int z = SectionPos.sectionToBlockCoord(SectionPos.z(batch.key.section));
            if (!event.getFrustum().isVisible(new AABB(x, y + offset, z, x + 16, y + offset + 16, z + 16).inflate(2)))
                continue;
            visible.add(batch);
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS ||
                event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            visible.sort(Comparator.comparingDouble((Batch batch) -> batch.distanceSquared(camera, offset)).reversed());
        }
        Vec3 relativeCamera = camera.subtract(anchor.getX(), anchor.getY() + offset, anchor.getZ());
        for (Batch batch : visible) {
            batch.draw(matrix, clipY, relativeCamera);
        }
    }

    private static boolean atStage(RenderType layer, RenderLevelStageEvent.Stage stage) {
        if (RenderLevelStageEvent.Stage.fromRenderType(layer) == stage) return true;
        return stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS &&
                CustomChunkRenderPassRegistry.afterCutoutPasses().stream().anyMatch(pass -> pass.renderType() == layer);
    }

    private static void emit(List<Quad> quads, VertexConsumer consumer, double clipY) {
        for (Quad quad : quads) {
            if (quad.maxY <= clipY) {
                for (Vertex vertex : quad.vertices) vertex.emit(consumer);
            } else if (quad.minY < clipY) {
                List<Vertex> clipped = new ArrayList<>(6);
                Vertex previous = quad.vertices[3];
                for (Vertex current : quad.vertices) {
                    boolean before = previous.y <= clipY;
                    boolean after = current.y <= clipY;
                    if (before != after) {
                        clipped.add(previous.interpolate(current, (float) ((clipY - previous.y) /
                                (current.y - previous.y))));
                    }
                    if (after) clipped.add(current);
                    previous = current;
                }
                for (int index = 1; index + 1 < clipped.size(); index++) {
                    clipped.getFirst().emit(consumer);
                    clipped.get(index).emit(consumer);
                    clipped.get(index + 1).emit(consumer);
                    clipped.get(index + 1).emit(consumer);
                }
            }
        }
    }

    @Override
    public void close() {
        batches.forEach(Batch::close);
    }

    private record BatchKey(RenderType layer, long section, int band) {}

    private static final class Batch implements AutoCloseable {

        private final BatchKey key;
        private final List<Quad> quads;
        private final float minY;
        private final float maxY;
        private final ByteBufferBuilder scratch = new ByteBufferBuilder(32768);
        private MeshData prepared;
        private VertexBuffer complete;
        private VertexBuffer boundary;

        private Batch(BatchKey key, List<Quad> quads) {
            this.key = key;
            this.quads = quads;
            minY = (float) quads.stream().mapToDouble(Quad::minY).min().orElseThrow();
            maxY = (float) quads.stream().mapToDouble(Quad::maxY).max().orElseThrow();
            prepared = build(Double.POSITIVE_INFINITY);
        }

        private MeshData build(double clipY) {
            var builder = new BufferBuilder(scratch, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            emit(quads, builder, clipY);
            return builder.build();
        }

        private double distanceSquared(Vec3 camera, double offset) {
            return camera.distanceToSqr(SectionPos.sectionToBlockCoord(SectionPos.x(key.section)) + 8,
                    SectionPos.sectionToBlockCoord(SectionPos.y(key.section)) + 8 + offset,
                    SectionPos.sectionToBlockCoord(SectionPos.z(key.section)) + 8);
        }

        private void sort(MeshData mesh, Vec3 camera) {
            if (key.layer == RenderType.translucent() || key.layer == RenderType.tripwire()) {
                mesh.sortQuads(scratch, VertexSorting.byDistance((float) camera.x, (float) camera.y, (float) camera.z));
            }
        }

        private void draw(Matrix4f matrix, double clipY, Vec3 camera) {
            boolean full = clipY >= maxY;
            VertexBuffer buffer;
            if (full) {
                if (complete == null) {
                    sort(prepared, camera);
                    complete = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    complete.bind();
                    complete.upload(prepared);
                    prepared = null;
                    scratch.clear();
                }
                buffer = complete;
            } else {
                var clipped = build(clipY);
                if (clipped == null) return;
                sort(clipped, camera);
                if (boundary == null) boundary = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
                boundary.bind();
                boundary.upload(clipped);
                buffer = boundary;
            }
            key.layer.setupRenderState();
            try {
                var shader = RenderSystem.getShader();
                if (shader == null) return;
                if (shader.CHUNK_OFFSET != null) shader.CHUNK_OFFSET.set(0.0f, 0.0f, 0.0f);
                buffer.bind();
                buffer.drawWithShader(matrix, RenderSystem.getProjectionMatrix(), shader);
            } finally {
                VertexBuffer.unbind();
                key.layer.clearRenderState();
            }
        }

        @Override
        public void close() {
            if (prepared != null) prepared.close();
            if (complete != null) complete.close();
            if (boundary != null) boundary.close();
            scratch.close();
        }
    }

    private record Quad(Vertex[] vertices, RenderType layer, long section, float minY, float maxY) {}

    private record LitBlueprint(BlockAndTintGetter blocks, BlockAndTintGetter lighting) implements BlockAndTintGetter {

        @Override
        public float getShade(Direction direction, boolean shade) {
            return lighting.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return lighting.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver resolver) {
            return lighting.getBlockTint(pos, resolver);
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return blocks.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return blocks.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return blocks.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return lighting.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return lighting.getMinBuildHeight();
        }
    }

    private record Vertex(float x, float y, float z, float r, float g, float b, float a,
                          float u, float v, int lightU, int lightV, float nx, float ny, float nz) {

        private void emit(VertexConsumer consumer) {
            consumer.addVertex(x, y, z).setColor(r, g, b, a).setUv(u, v).setUv2(lightU, lightV)
                    .setNormal(nx, ny, nz);
        }

        private Vertex interpolate(Vertex other, float amount) {
            return new Vertex(Mth.lerp(amount, x, other.x), Mth.lerp(amount, y, other.y),
                    Mth.lerp(amount, z, other.z), Mth.lerp(amount, r, other.r), Mth.lerp(amount, g, other.g),
                    Mth.lerp(amount, b, other.b), Mth.lerp(amount, a, other.a), Mth.lerp(amount, u, other.u),
                    Mth.lerp(amount, v, other.v), Math.round(Mth.lerp(amount, lightU, other.lightU)),
                    Math.round(Mth.lerp(amount, lightV, other.lightV)), Mth.lerp(amount, nx, other.nx),
                    Mth.lerp(amount, ny, other.ny), Mth.lerp(amount, nz, other.nz));
        }
    }

    private static final class QuadCapture implements VertexConsumer {

        private final List<Quad> result;
        private final RenderType layer;
        private final long section;
        private final List<Vertex> vertices = new ArrayList<>(4);
        private boolean pending;
        private float x, y, z, r = 1, g = 1, b = 1, a = 1, u, v, nx, ny, nz;
        private int lightU, lightV;

        private QuadCapture(List<Quad> result, RenderType layer, long section) {
            this.result = result;
            this.layer = layer;
            this.section = section;
        }

        private void finish() {
            if (!pending) return;
            vertices.add(new Vertex(x, y, z, r, g, b, a, u, v, lightU, lightV, nx, ny, nz));
            if (vertices.size() == 4) {
                float min = Float.POSITIVE_INFINITY;
                float max = Float.NEGATIVE_INFINITY;
                for (Vertex vertex : vertices) {
                    min = Math.min(min, vertex.y);
                    max = Math.max(max, vertex.y);
                }
                result.add(new Quad(vertices.toArray(Vertex[]::new), layer, section, min, max));
                vertices.clear();
            }
            pending = false;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            finish();
            this.x = x;
            this.y = y;
            this.z = z;
            pending = true;
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            r = red / 255f;
            g = green / 255f;
            b = blue / 255f;
            a = alpha / 255f;
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            lightU = u;
            lightV = v;
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            nx = x;
            ny = y;
            nz = z;
            return this;
        }
    }
}
