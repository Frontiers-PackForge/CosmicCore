package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.model.machine.IControllerModelRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.klikli_dev.occultism.registry.OccultismBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HellFireFoundryPartRender extends
                                       DynamicRender<WorkableElectricMultiblockMachine, HellFireFoundryPartRender>
                                       implements IControllerModelRenderer {

    // spotless:off
    public static final Codec<HellFireFoundryPartRender> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("casing_block").forGetter(HellFireFoundryPartRender::getCasing)
    ).apply(instance, HellFireFoundryPartRender::new));
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HellFireFoundryPartRender> TYPE = new DynamicRenderType<>(CODEC);
    // spotless:on

    private final BlockState iesniumBlock;
    @Getter
    private final BlockState casing;
    private BakedModel iesniumBlockModel;
    private BakedModel casingModel;

    public HellFireFoundryPartRender(BlockState casing) {
        this.iesniumBlock = OccultismBlocks.IESNIUM_BLOCK.get().defaultBlockState();
        this.casing = casing;
        ModelUtils.registerBakeEventListener(event -> {
            this.iesniumBlockModel = event.getModels().get(BlockModelShaper.stateToModelLocation(this.iesniumBlock));
            this.casingModel = event.getModels().get(BlockModelShaper.stateToModelLocation(this.casing));
        });
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, HellFireFoundryPartRender> getType() {
        return TYPE;
    }

    @Override
    public void renderPartModel(List<BakedQuad> quads, IMultiController controller,
                                IMultiPart part, Direction frontFacing, @Nullable Direction side,
                                RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        BlockPos partPos = part.self().getPos();
        MultiblockControllerMachine machine = controller.self();
        BakedModel model = part instanceof SoulHatchPartMachine ? iesniumBlockModel : casingModel;
        emitQuads(quads, model, machine.getLevel(), partPos, casing, side, rand, modelData, renderType);
    }

    private static void emitQuads(List<BakedQuad> quads, @Nullable BakedModel model,
                                  BlockAndTintGetter level, BlockPos pos, BlockState state,
                                  @Nullable Direction side, RandomSource rand,
                                  ModelData modelData, @Nullable RenderType renderType) {
        if (model == null) return;
        modelData = model.getModelData(level, pos, state, modelData);
        quads.addAll(model.getQuads(state, side, rand, modelData, renderType));
    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {}

    @Override
    public boolean shouldRender(WorkableElectricMultiblockMachine machine, Vec3 cameraPos) {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }
}
