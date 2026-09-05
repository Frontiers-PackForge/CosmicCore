package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentAnimation;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentBlueprints;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentPlan;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentTarget;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.LeylineDeploymentFinishPacket;
import com.ghostipedia.cosmiccore.common.network.packet.LeylineDeploymentRequestPacket;
import com.ghostipedia.cosmiccore.common.network.packet.LeylineDeploymentStartPacket;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class LeylineDeploymentClient {

    private static final RenderType CIRCLE = RenderType.entityCutoutNoCull(
            CosmicCore.id("textures/effect/leyline/circle.png"));
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource
            .immediate(new ByteBufferBuilder(262144));
    private static final Map<UUID, Presentation> ACTIVE = new HashMap<>();
    private static final Map<UUID, PendingStart> PENDING = new HashMap<>();
    private static LeylinePreview preview;
    private static long nextRequestTick;

    private LeylineDeploymentClient() {}

    public static void start(LeylineDeploymentStartPacket packet) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().location().equals(packet.dimension()) ||
                ACTIVE.containsKey(packet.id()) || PENDING.containsKey(packet.id()))
            return;
        CompletableFuture<com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentBlueprint> blueprint;
        if (packet.blueprint().getNamespace().equals(CosmicCore.MOD_ID) &&
                packet.blueprint().getPath().startsWith("prefab/")) {
            blueprint = LeylinePrefabClient.request(UUID.fromString(packet.blueprint().getPath().substring(6)))
                    .thenApply(prefab -> prefab == null ? null :
                            LeylineDeploymentBlueprints.register(prefab, packet.facing()));
        } else {
            blueprint = CompletableFuture.supplyAsync(
                    () -> LeylineDeploymentBlueprints.resolve(packet.blueprint(), packet.facing()),
                    Util.backgroundExecutor());
        }
        var plan = blueprint.thenApplyAsync(value -> value == null ? null : value.planAt(packet.anchor()),
                Util.backgroundExecutor()).exceptionally(error -> {
                    CosmicCore.LOGGER.error("Unable to resolve leyline presentation {}", packet.blueprint(), error);
                    return null;
                });
        PENDING.put(packet.id(), new PendingStart(packet, plan));
    }

    private static void acceptPreparedStarts() {
        var minecraft = Minecraft.getInstance();
        var iterator = PENDING.values().iterator();
        while (iterator.hasNext()) {
            var pending = iterator.next();
            if (!pending.plan.isDone()) continue;
            iterator.remove();
            var plan = pending.plan.getNow(null);
            if (plan == null || pending.finish != null && !pending.finish.committed()) continue;
            var packet = pending.packet;
            var animation = new LeylineDeploymentAnimation(LeylineDeploymentAnimation.bounds(plan), packet.openingY());
            ACTIVE.put(packet.id(), new Presentation(plan, animation, packet.startTick()));
            if (pending.finish != null) finish(pending.finish);
        }
    }

    public static void finish(LeylineDeploymentFinishPacket packet) {
        var pending = PENDING.get(packet.id());
        if (pending != null) {
            pending.finish = packet;
            return;
        }
        Presentation presentation = ACTIVE.get(packet.id());
        var minecraft = Minecraft.getInstance();
        if (presentation == null || minecraft.level == null) return;
        if (!packet.committed()) {
            ACTIVE.remove(packet.id());
            presentation.close();
            return;
        }
        if (presentation.committed) return;
        presentation.committed = true;
        presentation.age = Math.max(presentation.age, LeylineDeploymentAnimation.IMPACT_TICK);
        presentation.impactAge = presentation.age;
        AABB bounds = presentation.animation.bounds();
        minecraft.levelRenderer.setBlocksDirty((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ,
                (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ);
        presentation.effects.impact();
    }

    public static void sectionUploaded(int x, int y, int z) {
        var minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            minecraft.execute(() -> sectionUploaded(x, y, z));
            return;
        }
        if (ACTIVE.isEmpty() || minecraft.level == null) return;
        long section = SectionPos.asLong(x, y, z);
        for (Presentation presentation : ACTIVE.values()) {
            if (!presentation.committed || !presentation.sections.contains(section)) continue;
            boolean received = presentation.sectionPlacements.get(section).stream()
                    .allMatch(placement -> minecraft.level.getBlockState(placement.pos()).equals(placement.state()));
            if (received) presentation.completedSections.add(section);
        }
    }

    @SubscribeEvent
    public static void use(InputEvent.InteractionKeyMappingTriggered event) {
        var minecraft = Minecraft.getInstance();
        if (!event.isUseItem() || minecraft.player == null || minecraft.level == null || minecraft.screen != null ||
                !LeylineDeploymentBlueprints.isPackage(minecraft.player.getItemInHand(event.getHand())))
            return;
        event.setCanceled(true);
        event.setSwingHand(false);
        if (minecraft.level.getGameTime() < nextRequestTick) return;
        var hit = LeylineDeploymentTarget.trace(minecraft.player, 1);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        nextRequestTick = minecraft.level.getGameTime() + 6;
        CCoreNetwork.sendToServer(new LeylineDeploymentRequestPacket(event.getHand(), hit));
        minecraft.player.swing(event.getHand());
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            acceptPreparedStarts();
            updatePreview(event.getPartialTick().getGameTimeDeltaPartialTick(false));
            if (!minecraft.isPaused() && !minecraft.level.tickRateManager().isFrozen()) {
                double elapsed = event.getPartialTick().getGameTimeDeltaTicks();
                ACTIVE.values().forEach(presentation -> {
                    presentation.age += elapsed;
                    presentation.effects.update(presentation.age, presentation.impactAge);
                });
            }
        }
        if (preview != null && ACTIVE.values().stream()
                .noneMatch(presentation -> presentation.plan.controllerPos().equals(preview.anchor()))) {
            preview.draw(event);
        }
        boolean translucent = event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES;
        ACTIVE.values().removeIf(presentation -> {
            boolean expired = presentation.age > LeylineDeploymentAnimation.CLIENT_TIMEOUT_TICKS ||
                    presentation.committed && presentation.age >
                            presentation.impactAge + LeylineDeploymentEffects.IMPACT_TAIL_TICKS &&
                            presentation.completedSections.containsAll(presentation.sections);
            if (expired) presentation.close();
            return expired;
        });
        PoseStack poses = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        for (Presentation presentation : ACTIVE.values()) {
            double age = Math.max(0, presentation.age);
            AABB bounds = presentation.animation.bounds();
            AABB visualBounds = new AABB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX,
                    presentation.animation.openingY() + 0.1, bounds.maxZ).inflate(presentation.animation.radius());
            if (!event.getFrustum().isVisible(visualBounds)) continue;
            double offset = presentation.committed ? 0 : presentation.animation.verticalOffset(age);
            BlockPos anchor = presentation.plan.controllerPos();
            var mesh = presentation.baking.getNow(null);
            if (mesh != null) mesh.render(event, anchor, offset,
                    presentation.animation.openingY() - offset - anchor.getY(), presentation.completedSections);
            if (translucent) renderCircle(poses, camera, presentation, age);
        }
        if (translucent) BUFFERS.endBatch();
    }

    private static void updatePreview(float partialTick) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null ||
                minecraft.player.isSpectator()) {
            preview = null;
            return;
        }
        InteractionHand hand = LeylineDeploymentBlueprints.isPackage(minecraft.player.getMainHandItem()) ?
                InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var stack = minecraft.player.getItemInHand(hand);
        if (!LeylineDeploymentBlueprints.isPackage(stack)) {
            preview = null;
            return;
        }
        var hit = LeylineDeploymentTarget.trace(minecraft.player, partialTick);
        if (hit.getType() != HitResult.Type.BLOCK) {
            preview = null;
            return;
        }
        BlockPos anchor = LeylineDeploymentTarget.anchor(minecraft.player, hit);
        Direction facing = minecraft.player.getDirection().getOpposite();
        var blueprint = LeylineDeploymentBlueprints.forPackage(stack, facing, minecraft.level);
        if (blueprint == null) {
            preview = null;
            return;
        }
        preview = LeylinePreview.get(blueprint.id(), facing);
        preview.target(anchor);
    }

    private static void renderCircle(PoseStack poses, Vec3 camera, Presentation presentation, double age) {
        float scale = presentation.animation.circleScale(age, presentation.impactAge);
        if (scale <= 0) return;
        Vec3 center = presentation.animation.bounds().getCenter();
        float radius = (float) presentation.animation.radius() * scale;
        poses.pushPose();
        poses.translate(center.x - camera.x, presentation.animation.openingY() - 0.015 - camera.y, center.z - camera.z);
        poses.mulPose(Axis.YP.rotationDegrees((float) (age * 5.5)));
        VertexConsumer consumer = BUFFERS.getBuffer(CIRCLE);
        circleVertex(consumer, poses.last(), -radius, -radius, 0, 0);
        circleVertex(consumer, poses.last(), -radius, radius, 0, 1);
        circleVertex(consumer, poses.last(), radius, radius, 1, 1);
        circleVertex(consumer, poses.last(), radius, -radius, 1, 0);
        poses.popPose();
    }

    private static void circleVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, 0, z).setColor(0.65f, 0.9f, 1.0f, 1.0f).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) clear();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void clear() {
        ACTIVE.values().forEach(Presentation::close);
        ACTIVE.clear();
        PENDING.values().forEach(pending -> pending.plan.cancel(false));
        PENDING.clear();
        preview = null;
        LeylinePreview.clear();
        nextRequestTick = 0;
    }

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Reload {

        @SubscribeEvent
        public static void models(ModelEvent.BakingCompleted event) {
            preview = null;
            LeylinePreview.clear();
            ACTIVE.values().forEach(presentation -> {
                presentation.releaseMesh();
                presentation.bake();
            });
        }
    }

    private static final class Presentation {

        private final LeylineDeploymentPlan plan;
        private final LeylineDeploymentAnimation animation;
        private final LeylineDeploymentEffects effects;
        private double age;
        private final Set<Long> sections = new HashSet<>();
        private final Map<Long, List<LeylineDeploymentPlan.WorldPlacement>> sectionPlacements = new HashMap<>();
        private final Set<Long> completedSections = new HashSet<>();
        private CompletableFuture<LeylineStructureMesh> baking;
        private boolean committed;
        private double impactAge = Double.POSITIVE_INFINITY;

        private Presentation(LeylineDeploymentPlan plan, LeylineDeploymentAnimation animation, long startTick) {
            this.plan = plan;
            this.animation = animation;
            this.age = Minecraft.getInstance().level.getGameTime() - startTick;
            this.effects = new LeylineDeploymentEffects(animation, age);
            plan.worldPlacements().forEach(placement -> sectionPlacements
                    .computeIfAbsent(SectionPos.asLong(placement.pos()), ignored -> new ArrayList<>()).add(placement));
            sections.addAll(sectionPlacements.keySet());
            bake();
        }

        private void bake() {
            var lighting = Minecraft.getInstance().level;
            baking = CompletableFuture
                    .supplyAsync(() -> LeylineStructureMesh.bake(plan, lighting), Util.backgroundExecutor())
                    .exceptionally(error -> {
                        CosmicCore.LOGGER.error("Unable to prepare leyline deployment {}", plan.blueprint().id(),
                                error);
                        return null;
                    });
        }

        private void close() {
            effects.close();
            releaseMesh();
        }

        private void releaseMesh() {
            baking.thenAcceptAsync(mesh -> {
                if (mesh != null) mesh.close();
            }, Minecraft.getInstance());
        }
    }

    private static final class PendingStart {

        private final LeylineDeploymentStartPacket packet;
        private final CompletableFuture<LeylineDeploymentPlan> plan;
        private LeylineDeploymentFinishPacket finish;

        private PendingStart(LeylineDeploymentStartPacket packet, CompletableFuture<LeylineDeploymentPlan> plan) {
            this.packet = packet;
            this.plan = plan;
        }
    }
}
