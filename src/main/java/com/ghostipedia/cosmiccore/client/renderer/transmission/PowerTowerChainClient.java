package com.ghostipedia.cosmiccore.client.renderer.transmission;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.deployment.LeylineDeploymentBlueprints;
import com.ghostipedia.cosmiccore.common.item.PowerTowerCoilItem;
import com.ghostipedia.cosmiccore.common.network.packet.PowerTowerChainPacket;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerAttachments;
import com.ghostipedia.cosmiccore.common.transmission.geometry.PowerTowerWireGeometry;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerNode;
import com.ghostipedia.cosmiccore.common.transmission.link.PowerTowerLinkService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Locale;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class PowerTowerChainClient {

    private static PowerTowerNode source;
    private static BlockPos target;
    private static Direction facing;
    private static PowerTowerWireGeometry geometry;
    private static double distance;
    private static double angle;
    private static boolean valid;
    private static long drawnAt = Long.MIN_VALUE;

    public static void update(PowerTowerChainPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(packet.dimension())) return;
        source = packet.node();
        target = null;
        geometry = null;
    }

    private static ItemStack coil() {
        var player = Minecraft.getInstance().player;
        if (player == null) return ItemStack.EMPTY;
        if (LeylineDeploymentBlueprints.isPackage(player.getMainHandItem())) return player.getOffhandItem();
        if (LeylineDeploymentBlueprints.isPackage(player.getOffhandItem())) return player.getMainHandItem();
        return ItemStack.EMPTY;
    }

    public static void draw(RenderLevelStageEvent event, BlockPos controller, Direction direction) {
        if (source == null || controller == null ||
                event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
            return;
        var minecraft = Minecraft.getInstance();
        drawnAt = minecraft.level.getGameTime();
        if (!controller.equals(target) || facing != direction) {
            target = controller.immutable();
            facing = direction;
            var destination = PowerTowerAttachments.preview(controller, direction, null);
            angle = Math.max(PowerTowerLinkService.departureAngle(source, destination),
                    PowerTowerLinkService.departureAngle(destination, source));
            geometry = null;
            try {
                distance = PowerTowerLinkService.distance(source, destination);
                geometry = PowerTowerWireGeometry.endpoints(source, destination).geometry();
            } catch (IllegalArgumentException ignored) {
                distance = 0;
            }
            valid = geometry != null && distance <= PowerTowerLinkService.DEFAULT_MAX_ATTACHMENT_DISTANCE &&
                    angle <= PowerTowerLinkService.MAX_DEPARTURE_ANGLE + 1.0E-6;
        }
        if (geometry == null || !(coil().getItem() instanceof PowerTowerCoilItem)) return;
        var buffers = minecraft.renderBuffers().bufferSource();
        var consumer = buffers.getBuffer(RenderType.lines());
        var camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        var pose = poses.last();
        for (var segment : geometry.segments()) {
            var normal = segment.end().subtract(segment.start()).normalize();
            for (var point : new net.minecraft.world.phys.Vec3[] { segment.start(), segment.end() }) {
                consumer.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                        .setColor(valid ? 0.3f : 1.0f, valid ? 0.95f : 0.25f, valid ? 1.0f : 0.2f, 0.85f)
                        .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
            }
        }
        poses.popPose();
        buffers.endBatch(RenderType.lines());
    }

    @SubscribeEvent
    public static void hud(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (source == null || minecraft.level == null || minecraft.player == null ||
                drawnAt != minecraft.level.getGameTime() || minecraft.options.hideGui ||
                !(LeylineDeploymentBlueprints.isPackage(minecraft.player.getMainHandItem()) ||
                        LeylineDeploymentBlueprints.isPackage(minecraft.player.getOffhandItem())))
            return;
        var graphics = event.getGuiGraphics();
        var text = Component.translatable("cosmiccore.power_tower.chain.preview",
                String.format(Locale.ROOT, "%.1f", distance),
                (int) PowerTowerLinkService.DEFAULT_MAX_ATTACHMENT_DISTANCE, String.format(Locale.ROOT, "%.0f", angle),
                (int) PowerTowerLinkService.MAX_DEPARTURE_ANGLE);
        graphics.drawCenteredString(minecraft.font, text, graphics.guiWidth() / 2, graphics.guiHeight() / 2 + 24,
                valid ? 0xFF80E8FF : 0xFFFF6655);
        if (!(coil().getItem() instanceof PowerTowerCoilItem))
            graphics.drawCenteredString(minecraft.font, Component.translatable("cosmiccore.power_tower.chain.no_coil"),
                    graphics.guiWidth() / 2, graphics.guiHeight() / 2 + 36, 0xFFFFFF99);
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
        source = null;
        target = null;
        geometry = null;
        drawnAt = Long.MIN_VALUE;
    }
}
