package com.ghostipedia.cosmiccore.client.renderer.machine;
import appeng.api.config.RelativeDirection;
import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;


import com.gregtechceu.gtceu.client.renderer.machine.impl.FusionRingRender;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HPCAIndicatorRenderer extends DynamicRender<HPCAIndicatorPartMachine, HPCAIndicatorRenderer> {

    public static final Codec<HPCAIndicatorRenderer> CODEC = Codec.unit(HPCAIndicatorRenderer::new);
    public static final DynamicRenderType<HPCAIndicatorPartMachine, HPCAIndicatorRenderer> TYPE = new DynamicRenderType<>(HPCAIndicatorRenderer.CODEC);

    public HPCAIndicatorRenderer() {}

    public static final ResourceLocation BASE = CosmicCore.id("block/overlay/machine/hpca/indicator");
    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f, 1.001f, 1.001f, 1.001f);

//    public HPCAIndicatorRenderer() {
//        super(GTValues.ZPM, GTCEu.id("block/computer_casing"));
//    }


//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
//                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
//                              ModelState modelState) {
//        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
//        if (side != frontFacing || modelFacing == null) return;
//
//        quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing, ModelFactory.getBlockSprite(BASE),
//                modelState, -1, 0, true, false));
//
//        if (machine instanceof HPCAIndicatorPartMachine indicatorPart) {
//            var controllers = indicatorPart.getControllers();
//            if (controllers.isEmpty()) return;
//            if (controllers.first() instanceof HPCAMachine controller) {
//                var modifier = controller.getModifier(machine.getPos());
//                quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing,
//                        ModelFactory.getBlockSprite(modifier.overlay), modelState, -1, 15, true, false));
//            }
//        }
//    }

    @Override
    public @NotNull DynamicRenderType<HPCAIndicatorPartMachine, HPCAIndicatorRenderer> getType() {
        return TYPE;
    }

    @Override
    public void render(HPCAIndicatorPartMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        for(RelativeDirection dir : RelativeDirection.values()) {

        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }


    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull BlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return super.getRenderBoundingBox(blockEntity);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return super.getQuads(state, side, rand);
    }

}
