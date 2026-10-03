package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelEventHelper;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HemophagicTransfuserRender extends
                                        DynamicRender<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> {

    public static final HemophagicTransfuserRender INSTANCE = new HemophagicTransfuserRender();
    public static final MapCodec<HemophagicTransfuserRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> TYPE = new DynamicRenderType<>(
            HemophagicTransfuserRender.CODEC);

    private static final BiFunction<Direction, Direction, AABB> renderBoundCache = Util.memoize((front, upwards) -> {
        Direction up = RelativeDirection.UP.getRelativeFacing(front, upwards, false);
        Direction back = RelativeDirection.BACK.getRelativeFacing(front, upwards, false);
        Direction left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, false);

        BlockPos.MutableBlockPos minPos = new BlockPos.MutableBlockPos()
                .move(left, 3).move(up, -2).move(back, 0);
        BlockPos.MutableBlockPos maxPos = new BlockPos.MutableBlockPos()
                .move(left, -3).move(up, 6).move(back, 6);

        return AABB.encapsulatingFullBlocks(minPos, maxPos);
    });

    public static final ResourceLocation BLOOD_CUBE_TEXTURE = CosmicCore.id("block/iris/blood_cube");

    private static TextureAtlasSprite bloodCubeSprite = null;
    private final TransfuserBloodStreams bloodStreams = new TransfuserBloodStreams();
    private final Map<WorkableElectricMultiblockMachine, Integer> particleTicks = new WeakHashMap<>();
    private static boolean isEventListenerRegistered = false;

    @SuppressWarnings("deprecation")
    private HemophagicTransfuserRender() {
        if (!isEventListenerRegistered) {
            ModelEventHelper.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
                bloodCubeSprite = event.getAtlas().getSprite(BLOOD_CUBE_TEXTURE);
            });
            isEventListenerRegistered = true;
        }
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, HemophagicTransfuserRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine multi) {
        if (multi.isFormed()) {
            AABB bounds = renderBoundCache.apply(multi.getFrontFacing(), multi.getUpwardsFacing());
            return bounds.move(multi.getBlockPos());
        }
        return super.getRenderBoundingBox(multi);
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
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        double totalTick = (double) player.tickCount + partialTick;

        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();
        Direction up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped);
        Direction back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped);
        poseStack.translate(
                0.5 + 3 * up.getStepX() + 3 * back.getStepX(),
                0.5 + 3 * up.getStepY() + 3 * back.getStepY(),
                0.5 + 3 * up.getStepZ() + 3 * back.getStepZ());

        renderBloodPool(poseStack, buffer);
        bloodStreams.render(poseStack, buffer, totalTick);
        poseStack.translate(0.5 * up.getStepX(), 0.5 * up.getStepY(), 0.5 * up.getStepZ());
        renderBloodCube(poseStack, buffer, totalTick);

        renderRings(up.getAxis(), totalTick, poseStack, buffer);
        if (!Integer.valueOf(player.tickCount).equals(particleTicks.put(machine, player.tickCount))) {
            var random = machine.getLevel().random;
            for (int i = 0; i < 2; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double height = random.nextDouble() * 2 - 1;
                double radius = Math.sqrt(1 - height * height) * 2.2;
                machine.getLevel().addParticle(new DustParticleOptions(new Vector3f(0.65f, 0.025f, 0.07f), 0.7f),
                        machine.getBlockPos().getX() + 0.5 + 3.5 * up.getStepX() + 3 * back.getStepX() +
                                Math.cos(angle) * radius,
                        machine.getBlockPos().getY() + 0.5 + 3.5 * up.getStepY() + 3 * back.getStepY() + height * 2.2,
                        machine.getBlockPos().getZ() + 0.5 + 3.5 * up.getStepZ() + 3 * back.getStepZ() +
                                Math.sin(angle) * radius,
                        0, 0.015, 0);
            }
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderBloodPool(PoseStack poseStack, MultiBufferSource buffer) {
        VitaeFluidRender.renderPool(poseStack, buffer, new Vec3(0, -3.58, 0), 2);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBloodCube(PoseStack poseStack, MultiBufferSource bufferSource, double totalTick) {
        poseStack.pushPose();
        // rotate around center
        Quaternionf rot = new Quaternionf()
                .rotateXYZ((float) (totalTick / 80 % (Math.PI * 2)),
                        (float) (totalTick / 60 % (Math.PI * 2)),
                        (float) (totalTick / 120 % (Math.PI * 2)))
                .rotateXYZ(55f * Mth.DEG_TO_RAD, 30f * Mth.DEG_TO_RAD, 0);
        poseStack.mulPose(rot);

        // draw cube quads
        var consumer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
        RenderBufferHelper.renderTexturedCube(consumer, poseStack.last(), EnumSet.allOf(Direction.class), 0xffffffff,
                LightTexture.FULL_BRIGHT, bloodCubeSprite,
                -1, -1, -1, 1, 1, 1);

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderRings(Direction.Axis upAxis, double totalTick, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(GTRenderTypes.lightRing());

        float xRot = (float) (totalTick / 80 % (Math.PI * 2));
        float zRot = (float) (totalTick / 60 % (Math.PI * 2));
        float yRot = (float) (totalTick / 100 % (Math.PI * 2));

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(xRot, yRot, zRot));
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                2f, 0.1F, 10, 36,
                0.5F, 0, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(-xRot, yRot + Mth.HALF_PI, -zRot));
        consumer = buffer.getBuffer(GTRenderTypes.lightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.8f, 0.1F, 10, 36,
                0.4F, 0f, 0, 1, upAxis);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateXYZ(Mth.HALF_PI, yRot, zRot));
        consumer = buffer.getBuffer(GTRenderTypes.lightRing());
        RenderBufferHelper.renderRing(poseStack, consumer,
                0, 0, 0,
                1.6f, 0.1F, 10, 36,
                0.6F, 0, 0, 1, upAxis);
        poseStack.popPose();
    }
}
