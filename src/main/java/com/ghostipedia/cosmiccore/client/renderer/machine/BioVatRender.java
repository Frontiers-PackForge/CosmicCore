package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.client.renderer.block.FluidBlockRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BioVatRender extends DynamicRender<WorkableElectricMultiblockMachine, BioVatRender> {

    // spotless:off
    public static final BioVatRender INSTANCE = new BioVatRender();
    public static final Codec<BioVatRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, SufferingChamberRenderer> TYPE = new DynamicRenderType<>(SufferingChamberRenderer.CODEC);
    //spotless:on

    private final List<RelativeDirection> RENDER_FACES = List.of(new RelativeDirection[]{
            RelativeDirection.FRONT,
            RelativeDirection.BACK,
            RelativeDirection.LEFT,
            RelativeDirection.RIGHT,
            RelativeDirection.UP
    });

    private @Nullable Fluid cachedFluid;
    private @Nullable ResourceLocation cachedRecipe;

    @Override
    public DynamicRenderType getType() {
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

          }
}
