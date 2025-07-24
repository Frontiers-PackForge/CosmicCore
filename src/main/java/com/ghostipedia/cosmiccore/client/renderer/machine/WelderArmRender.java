package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WelderArmRender extends DynamicRender<WorkableElectricMultiblockMachine, WelderArmRender> {

    public static final WelderArmRender INSTANCE = new WelderArmRender();
    public static final Codec<WelderArmRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, WelderArmRender> TYPE = new DynamicRenderType<>(
            WelderArmRender.CODEC);

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, WelderArmRender> getType() {
        return this.TYPE;
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
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
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
}
