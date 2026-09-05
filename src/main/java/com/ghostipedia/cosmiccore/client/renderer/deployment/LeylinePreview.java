package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentAnimation;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentBlueprints;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentPlan;

import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;
import com.gregtechceu.gtceu.client.renderer.PatternPreviewRenderer;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import brachy.modularui.drawable.schema.RenderFilter;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class LeylinePreview {

    private static final Map<Key, LeylinePreview> CACHE = new java.util.LinkedHashMap<>(8, 0.75f, true);
    private static final int CHECKS_PER_TICK = 512;
    private final CompletableFuture<Prepared> preparation;
    private final Direction facing;
    private PatternPreviewRenderer renderer;
    private BlockPos anchor;
    private long checkedAt = Long.MIN_VALUE;
    private int checkIndex;
    private boolean valid;

    private LeylinePreview(Key key) {
        facing = key.facing;
        preparation = CompletableFuture.supplyAsync(() -> {
            var plan = LeylineDeploymentBlueprints.resolve(key.id, key.facing).planOnBase(BlockPos.ZERO);
            var blocks = new Long2ReferenceOpenHashMap<BlockState>(plan.worldPlacements().size());
            plan.worldPlacements().forEach(placement -> blocks.put(placement.pos().asLong(), placement.state()));
            return new Prepared(plan, LeylineDeploymentAnimation.bounds(plan), new MutableSchema(blocks));
        }, Util.backgroundExecutor()).exceptionally(error -> {
            CosmicCore.LOGGER.error("Unable to prepare leyline preview {}", key, error);
            return null;
        });
    }

    static LeylinePreview get(ResourceLocation id, Direction facing) {
        Key key = new Key(id, facing);
        var existing = CACHE.get(key);
        if (existing != null) return existing;
        if (CACHE.size() >= 8) {
            var iterator = CACHE.values().iterator();
            var oldest = iterator.next();
            oldest.preparation.cancel(false);
            if (oldest.renderer != null) oldest.renderer.dispose();
            iterator.remove();
        }
        var created = new LeylinePreview(key);
        CACHE.put(key, created);
        return created;
    }

    void target(BlockPos target) {
        if (!target.equals(anchor)) {
            anchor = target.immutable();
            checkIndex = 0;
            valid = false;
            checkedAt = Long.MIN_VALUE;
        }
    }

    BlockPos anchor() {
        Prepared prepared = preparation.getNow(null);
        return prepared == null || anchor == null ? null : anchor.offset(prepared.plan.controllerPos());
    }

    void draw(RenderLevelStageEvent event) {
        Prepared prepared = preparation.getNow(null);
        if (prepared == null || anchor == null) return;
        if (prepared.plan.blueprint().id()
                .equals(com.ghostipedia.cosmiccore.common.data.CosmicMachines.POWER_TOWER.getId()))
            com.ghostipedia.cosmiccore.client.renderer.transmission.PowerTowerChainClient.draw(event, anchor(), facing);
        var minecraft = Minecraft.getInstance();
        AABB bounds = prepared.bounds.move(anchor);
        if (!event.getFrustum().isVisible(bounds)) return;
        if (renderer == null) {
            renderer = new PatternPreviewRenderer();
            renderer.showPreview(BlockPos.ZERO, prepared.schema, RenderFilter.ALL, Integer.MAX_VALUE);
        }
        var poses = event.getPoseStack();
        renderer.draw(poses, minecraft.renderBuffers().bufferSource(), new RelativeCamera(event.getCamera(), anchor),
                event.getStage(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false),
                event.getModelViewMatrix());
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            validate(prepared);
            var camera = event.getCamera().getPosition();
            poses.pushPose();
            poses.translate(-camera.x, -camera.y, -camera.z);
            LevelRenderer.renderLineBox(poses, minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines()),
                    bounds, valid ? 0.48f : 1, valid ? 0.9f : 0.35f, valid ? 1 : 0.2f, 0.75f);
            poses.popPose();
            minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());
        }
    }

    private void validate(Prepared prepared) {
        var level = Minecraft.getInstance().level;
        if (checkedAt == level.getGameTime()) return;
        checkedAt = level.getGameTime();
        var placements = prepared.plan.worldPlacements();
        int end = Math.min(placements.size(), checkIndex + CHECKS_PER_TICK);
        var pos = new BlockPos.MutableBlockPos();
        for (; checkIndex < end; checkIndex++) {
            pos.setWithOffset(anchor, placements.get(checkIndex).pos());
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) ||
                    !level.isLoaded(pos) || !level.getFluidState(pos).isEmpty() ||
                    level.getBlockEntity(pos) != null || !level.getBlockState(pos).canBeReplaced()) {
                valid = false;
                checkIndex = 0;
                return;
            }
        }
        if (checkIndex == placements.size()) {
            valid = true;
            checkIndex = 0;
        }
    }

    static void clear() {
        CACHE.values().forEach(preview -> {
            preview.preparation.cancel(false);
            if (preview.renderer != null) preview.renderer.dispose();
        });
        CACHE.clear();
    }

    private record Key(ResourceLocation id, Direction facing) {}

    private static final class RelativeCamera extends Camera {

        private final Vec3 position;
        private final boolean initialized;

        private RelativeCamera(Camera camera, BlockPos anchor) {
            position = camera.getPosition().subtract(Vec3.atLowerCornerOf(anchor));
            initialized = camera.isInitialized();
        }

        @Override
        public Vec3 getPosition() {
            return position;
        }

        @Override
        public boolean isInitialized() {
            return initialized;
        }
    }

    private record Prepared(LeylineDeploymentPlan plan, AABB bounds, MutableSchema schema) {}
}
