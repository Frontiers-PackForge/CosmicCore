package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
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
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.joml.Quaternionf;

import java.util.EnumSet;
import java.util.function.BiFunction;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SufferingChamberRenderer extends
                                      DynamicRender<WorkableElectricMultiblockMachine, SufferingChamberRenderer> {

    // spotless:off
    public static final SufferingChamberRenderer INSTANCE = new SufferingChamberRenderer();
    public static final MapCodec<SufferingChamberRenderer> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, SufferingChamberRenderer> TYPE = new DynamicRenderType<>(SufferingChamberRenderer.CODEC);
    // spotless:on

    public static final ResourceLocation PENTAGRAM = CosmicCore.id("block/iris/pentagram");

    private static TextureAtlasSprite pentagramSprite = null;

    private static final BiFunction<Direction, Direction, AABB> renderBoundCache = Util.memoize((front, upwards) -> {
        Direction up = RelativeDirection.UP.getRelativeFacing(front, upwards, false);
        Direction back = RelativeDirection.BACK.getRelativeFacing(front, upwards, false);
        Direction left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, false);

        BlockPos.MutableBlockPos minPos = new BlockPos.MutableBlockPos()
                .move(left, 4).move(up, 3).move(back, 1);
        BlockPos.MutableBlockPos maxPos = new BlockPos.MutableBlockPos()
                .move(left, -4).move(up, 5).move(back, 7);

        return AABB.encapsulatingFullBlocks(minPos, maxPos);
    });

    private SufferingChamberRenderer() {
        ModelEventHelper.registerAtlasStitchedEventListener(true, TextureAtlas.LOCATION_BLOCKS, event -> {
            pentagramSprite = event.getAtlas().getSprite(PENTAGRAM);
        });
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, SufferingChamberRenderer> getType() {
        return TYPE;
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
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }
        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();

        Vec3i up = RelativeDirection.UP.getRelativeFacing(front, upwards, flipped).getNormal();
        Vec3i back = RelativeDirection.BACK.getRelativeFacing(front, upwards, flipped).getNormal();
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelativeFacing(front, upwards, flipped).getAxis();

        float x0ffset = 0, y0ffset = 0, zOffset = 0;

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int upOffset = up.get(axis);
            int backOffset = back.get(axis);

            float offset = upOffset * (4.0f + (upOffset * 0.5f)) +
                    backOffset * (4.0f + (backOffset * 0.5f));
            switch (axis) {
                case X -> x0ffset = offset;
                case Y -> y0ffset = offset;
                case Z -> zOffset = offset;
            }
        }

        poseStack.translate(
                x0ffset + (leftAxis == Direction.Axis.X ? 0.5f : 0.0f),
                y0ffset + (leftAxis == Direction.Axis.Y ? 0.5f : 0.0f),
                zOffset + (leftAxis == Direction.Axis.Z ? 0.5f : 0.0f));

        // do the rotaty thingy yee
        Quaternionf rot = new Quaternionf()
                .rotateY(totalTick / 30);
        poseStack.mulPose(rot);

        VertexConsumer consumer = bufferSource.getBuffer(Sheets.cutoutBlockSheet());
        RenderBufferHelper.renderTexturedCube(
                consumer,
                poseStack.last(),
                EnumSet.of(Direction.UP, Direction.DOWN),
                0xFF88FFFF,
                LightTexture.FULL_BRIGHT,
                pentagramSprite,
                -3.5f, 0, -3.5f,
                3.5f, 0, 3.5f);

        poseStack.popPose();
    }
}
