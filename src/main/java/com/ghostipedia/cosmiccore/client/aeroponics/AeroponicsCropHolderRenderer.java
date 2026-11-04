package com.ghostipedia.cosmiccore.client.aeroponics;

import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock;
import com.ghostipedia.cosmiccore.common.block.AeroponicsCropHolderBlock.Planter;
import com.ghostipedia.cosmiccore.common.blockentity.AeroponicsCropHolderBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public final class AeroponicsCropHolderRenderer extends BakedModelWrapper<BakedModel> {

    private static final ModelProperty<Geometry> PLANTS = new ModelProperty<>();

    public AeroponicsCropHolderRenderer(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        var data = originalModel.getModelData(level, pos, state, modelData);
        var seedlings = modelData.get(AeroponicsCropHolderBlockEntity.SEEDLINGS);
        if (seedlings == null) return data;
        var layers = new HashMap<RenderType, List<BakedQuad>>();
        var minecraft = Minecraft.getInstance();
        for (var planter : Planter.ALL) {
            var seed = seedlings.get(planter);
            if (seed.isEmpty()) continue;
            var visual = SeedVisualRegistration.INSTANCE.resolve(seed);
            var pose = plantPose(state.getValue(AeroponicsCropHolderBlock.FACING), planter, visual);
            if (visual == null) {
                var model = minecraft.getItemRenderer().getModel(seed, null, null, 0)
                        .applyTransform(ItemDisplayContext.NONE, pose, false);
                pose.translate(-0.5, -0.5, -0.5);
                for (var pass : model.getRenderPasses(seed, true)) {
                    appendPlantQuads(layers.computeIfAbsent(RenderType.cutout(), ignored -> new ArrayList<>()),
                            pass, null, null, pose, tint -> minecraft.getItemColors().getColor(seed, tint));
                }
            } else if (visual.state().isPresent()) {
                var plants = plantStates(visual);
                for (int part = 0; part < plants.size(); part++) {
                    var plant = plants.get(part);
                    var plantPos = pos.above(part);
                    var model = minecraft.getBlockRenderer().getBlockModel(plant);
                    pose.pushPose();
                    pose.translate(0, part, 0);
                    for (var layer : model.getRenderTypes(plant, RandomSource.create(42), ModelData.EMPTY)) {
                        appendPlantQuads(layers.computeIfAbsent(layer, ignored -> new ArrayList<>()),
                                model, plant, layer, pose,
                                tint -> minecraft.getBlockColors().getColor(plant, level, plantPos, tint));
                    }
                    pose.popPose();
                }
            } else {
                var model = minecraft.getModelManager()
                        .getModel(ModelResourceLocation.standalone(visual.model().orElseThrow()));
                appendPlantQuads(layers.computeIfAbsent(RenderType.cutout(), ignored -> new ArrayList<>()),
                        model, null, null, pose, tint -> -1);
            }
        }
        layers.replaceAll((layer, quads) -> List.copyOf(quads));
        return data.derive().with(PLANTS, new Geometry(Map.copyOf(layers),
                layers.values().stream().flatMap(List::stream).toList(), ChunkRenderTypeSet.of(layers.keySet())))
                .build();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        var plants = data.get(PLANTS);
        var base = originalModel.getRenderTypes(state, random, data);
        return plants == null ? base : ChunkRenderTypeSet.union(base, plants.layers());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        var base = state == null || renderType == null ||
                originalModel.getRenderTypes(state, random, data).contains(renderType) ?
                        originalModel.getQuads(state, side, random, data, renderType) : List.<BakedQuad>of();
        var plants = data.get(PLANTS);
        if (side != null || plants == null) return base;
        var quads = renderType == null ? plants.all() : plants.quads().getOrDefault(renderType, List.of());
        if (quads.isEmpty()) return base;
        if (base.isEmpty()) return quads;
        var combined = new ArrayList<>(base);
        combined.addAll(quads);
        return combined;
    }

    static List<BlockState> plantStates(SeedVisualRegistration.Visual visual) {
        var plant = visual.state().orElseThrow();
        if (visual.upperState().isPresent()) return List.of(plant, visual.upperState().get());
        var half = BlockStateProperties.DOUBLE_BLOCK_HALF;
        if (plant.hasProperty(half)) {
            return List.of(plant.setValue(half, DoubleBlockHalf.LOWER), plant.setValue(half, DoubleBlockHalf.UPPER));
        }
        return List.of(plant);
    }

    static PoseStack plantPose(Direction facing, Planter planter,
                               @Nullable SeedVisualRegistration.Visual visual) {
        var pose = new PoseStack();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() - 180));
        pose.translate(-0.5, 0, -0.5);
        var origin = planter.plantOrigin();
        pose.translate(origin.x, origin.y, origin.z);
        if (visual == null) {
            pose.translate(0, 0.125, 0);
            pose.scale(0.25F, 0.25F, 0.25F);
        } else {
            pose.translate(visual.offset().x, visual.offset().y, visual.offset().z);
            pose.scale(visual.scale(), visual.scale(), visual.scale());
            pose.translate(-0.5, 0, -0.5);
        }
        return pose;
    }

    static void appendPlantQuads(List<BakedQuad> target, BakedModel model, @Nullable BlockState plant,
                                 @Nullable RenderType layer, PoseStack pose, IntUnaryOperator tintColor) {
        var transform = QuadTransformers.applying(new Transformation(pose.last().pose()));
        var random = RandomSource.create(42);
        for (int face = 0; face <= Direction.values().length; face++) {
            Direction side = face == Direction.values().length ? null : Direction.values()[face];
            random.setSeed(42);
            for (var source : model.getQuads(plant, side, random, ModelData.EMPTY, layer)) {
                var quad = new BakedQuad(source.getVertices().clone(), -1,
                        Direction.rotate(pose.last().pose(), source.getDirection()), source.getSprite(),
                        source.isShade(), source.hasAmbientOcclusion());
                transform.processInPlace(quad);
                if (source.isTinted()) {
                    int color = QuadTransformers.toABGR(tintColor.applyAsInt(source.getTintIndex()) | 0xFF000000);
                    for (int vertex = 0; vertex < 4; vertex++) {
                        int offset = vertex * IQuadTransformer.STRIDE + IQuadTransformer.COLOR;
                        quad.getVertices()[offset] = FastColor.ARGB32.multiply(quad.getVertices()[offset], color);
                    }
                }
                target.add(quad);
            }
        }
    }

    private record Geometry(Map<RenderType, List<BakedQuad>> quads, List<BakedQuad> all,
                            ChunkRenderTypeSet layers) {}
}
