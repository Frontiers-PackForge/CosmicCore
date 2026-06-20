package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BioVatRender extends DynamicRender<WorkableElectricMultiblockMachine, BioVatRender> {

    // spotless:off
    public static final BioVatRender INSTANCE = new BioVatRender();
    public static final MapCodec<BioVatRender> CODEC = MapCodec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, BioVatRender> TYPE = new DynamicRenderType<>(BioVatRender.CODEC);
    //spotless:on

    private final List<RelativeDirection> RENDER_FACES = List.of(new RelativeDirection[] {
            RelativeDirection.FRONT,
            RelativeDirection.BACK,
            RelativeDirection.LEFT,
            RelativeDirection.RIGHT,
            RelativeDirection.UP
    });

    private @Nullable Fluid cachedFluid;
    private @Nullable ResourceLocation cachedRecipe;

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, BioVatRender> getType() {
        return TYPE;
    }

    @Override
    public @NotNull List<BakedQuad> getRenderQuads(@Nullable WorkableElectricMultiblockMachine machine,
                                                   @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                                   @Nullable BlockState blockState, @Nullable Direction side,
                                                   RandomSource rand, @NotNull ModelData modelData,
                                                   @Nullable RenderType renderType) {
        return super.getRenderQuads(machine, level, pos, blockState, side, rand, modelData, renderType);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return true;
    }

    @Override
    public boolean shouldRender(WorkableElectricMultiblockMachine machine, Vec3 cameraPos) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        return super.getRenderBoundingBox(machine);
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!machine.isFormed()) {
            return;
        }

        // caches the fluid for use (HEAVILY INSPIRED FROM FLUID RENDERS INSIDE GTCEU

        float totalTick = (Minecraft.getInstance().player.tickCount + partialTick);

        var pose = poseStack.last().pose();

        GTRecipe lastRecipe = machine.getRecipeLogic().getLastRecipe();
        if (lastRecipe == null) {
            cachedRecipe = null;
            cachedFluid = null;
        } else if (machine.self().getOffsetTimer() % 20 == 0 || lastRecipe.id != cachedRecipe) {
            cachedRecipe = lastRecipe.id;
            if (machine.isActive()) {
                cachedFluid = RenderUtil.getRecipeFluidToRender(lastRecipe);
            } else {
                cachedFluid = null;
            }
        }
        if (cachedFluid == null) {
            return;
        }

        poseStack.pushPose();

        Direction front = machine.getFrontFacing();
        Direction upwards = machine.getUpwardsFacing();
        boolean flipped = machine.isFlipped();

        Vec3i up = RelativeDirection.UP.getRelative(front, upwards, flipped).getNormal();
        Vec3i back = RelativeDirection.BACK.getRelative(front, upwards, flipped).getNormal();
        Direction.Axis leftAxis = RelativeDirection.LEFT.getRelative(front, upwards, flipped).getAxis();

        float x0ffset = 0, y0ffset = 0, zOffset = 0;

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            int upOffset = up.get(axis);
            int backOffset = back.get(axis);

            float offset = upOffset * (1f + (upOffset * 0.5f)) +
                    backOffset * (2f + (backOffset * 0.5f));
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

        FluidStack fluidStack = new FluidStack(cachedFluid, 1);
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(IClientFluidTypeExtensions.of(cachedFluid).getStillTexture(fluidStack));

        var flowingSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(IClientFluidTypeExtensions.of(cachedFluid).getFlowingTexture(fluidStack));
        VertexConsumer consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());

        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(cachedFluid);
        int color = fluidExt.getTintColor(fluidStack);

        // please
        bacVatRender(poseStack, consumer, poseStack.last(), color, LightTexture.FULL_BRIGHT, sprite, flowingSprite);
        poseStack.popPose();
    }

    public static void bacVatRender(PoseStack poseStack, VertexConsumer buffer, PoseStack.Pose pose,
                                    int color, int light,
                                    TextureAtlasSprite top, TextureAtlasSprite sides) {
        float minX = -1.5f;
        float minY = -1f;
        float minZ = -1.5f;
        float maxX = 1.5f;
        float maxY = 1f;
        float maxZ = 1.5f;

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) continue;

            TextureAtlasSprite sprite = switch (direction) {
                case UP -> top;
                case NORTH, EAST, WEST, SOUTH -> sides;
                default -> null;
            };

            if (sprite == null) continue;

            float uMin = sprite.getU0(), uMax = sprite.getU1();
            float vMin = sprite.getV0(), vMax = sprite.getV1();

            switch (direction) {
                case UP -> RenderBufferHelper.renderCubeFace(buffer, pose, color, light, Direction.UP,
                        minX, maxY, minZ, uMin, vMax,
                        minX, maxY, maxZ, uMax, vMax,
                        maxX, maxY, maxZ, uMax, vMin,
                        maxX, maxY, minZ, uMin, vMin);
                case NORTH -> RenderBufferHelper.renderCubeFace(buffer, pose, color, light, Direction.NORTH,
                        minX, minY, minZ, uMin, vMax,
                        minX, maxY, minZ, uMax, vMax,
                        maxX, maxY, minZ, uMax, vMin,
                        maxX, minY, minZ, uMin, vMin);
                case SOUTH -> RenderBufferHelper.renderCubeFace(buffer, pose, color, light, Direction.SOUTH,
                        minX, minY, maxZ, uMin, vMax,
                        maxX, minY, maxZ, uMax, vMax,
                        maxX, maxY, maxZ, uMax, vMin,
                        minX, maxY, maxZ, uMin, vMin);
                case WEST -> RenderBufferHelper.renderCubeFace(buffer, pose, color, light, Direction.WEST,
                        minX, minY, minZ, uMin, vMax,
                        minX, minY, maxZ, uMax, vMax,
                        minX, maxY, maxZ, uMax, vMin,
                        minX, maxY, minZ, uMin, vMin);
                case EAST -> RenderBufferHelper.renderCubeFace(buffer, pose, color, light, Direction.EAST,
                        maxX, minY, minZ, uMin, vMax,
                        maxX, maxY, minZ, uMax, vMax,
                        maxX, maxY, maxZ, uMax, vMin,
                        maxX, minY, maxZ, uMin, vMin);
            }
        }
    }
}
