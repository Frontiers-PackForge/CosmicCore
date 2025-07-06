package com.ghostipedia.cosmiccore.client.renderer.machine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
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



public class HellFireFoundryWorkableRenderer extends DynamicRender<WorkableElectricMultiblockMachine, HellFireFoundryWorkableRenderer> {

    public static final Codec<HellFireFoundryWorkableRenderer> CODEC = Codec.unit(HellFireFoundryWorkableRenderer::new);
    public static final DynamicRenderType<WorkableElectricMultiblockMachine, HellFireFoundryWorkableRenderer> TYPE =
            new DynamicRenderType<>(CODEC);



//    public final ResourceLocation multipartSprite;
//
//    public HellFireFoundryWorkableRenderer(ResourceLocation texture, ResourceLocation multipartSprite,
//                                           ResourceLocation workableModel) {
//        super(texture, workableModel);
//        this.multipartSprite = multipartSprite;
//    }
//
//    @Override
//    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
//                                @Nullable Direction side, RandomSource rand, Direction modelFacing,
//                                ModelState modelState) {
//        if (modelFacing != null) {
//            if (part instanceof SoulHatchPartMachine) {
//                quads.add(StaticFaceBakery.bakeFace(modelFacing,
//                        ModelFactory.getBlockSprite(new ResourceLocation("occultism", "block/iesnium_block")),
//                        modelState));
//            } else {
//                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(multipartSprite),
//                        modelState));
//            }
//        }
//    }

    @Override
    public void render(WorkableElectricMultiblockMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            if (!machine.recipeLogic.isWorking() ) return;
            renderHellFire(machine, poseStack, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(WorkableElectricMultiblockMachine machine) {
        return super.shouldRenderOffScreen(machine);
    }

    @Override
    public boolean shouldRender(WorkableElectricMultiblockMachine machine, Vec3 cameraPos) {
        return super.shouldRender(machine, cameraPos);
    }

    @Override
    public AABB getRenderBoundingBox(WorkableElectricMultiblockMachine machine) {
        return super.getRenderBoundingBox(machine);
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return super.getBlockEntityType();
    }

    @Override
    public void render(@NotNull BlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull BlockEntity blockEntity) {
        return super.shouldRenderOffScreen(blockEntity);
    }

    @Override
    public int getViewDistance() {
        return super.getViewDistance();
    }

    @Override
    public boolean shouldRender(BlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return super.getRenderBoundingBox(blockEntity);
    }

    @Override
    public DynamicRenderType<WorkableElectricMultiblockMachine, HellFireFoundryWorkableRenderer> getType() {
        return null;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return super.getQuads(state, side, rand);
    }

    @Override
    public ItemTransforms getTransforms() {
        return super.getTransforms();
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state) {
        return super.useAmbientOcclusion(state);
    }

    @Override
    public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
        return super.useAmbientOcclusion(state, renderType);
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return super.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        return super.getModelData(level, pos, state, modelData);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return super.getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return super.getRenderTypes(state, rand, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return super.getRenderTypes(itemStack, fabulous);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return super.getRenderPasses(itemStack, fabulous);
    }

    private void renderHellFire(WorkableElectricMultiblockMachine machine, PoseStack poseStack, VertexConsumer buffer ){
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("occultism", "block/iesnium_block"));

        RenderBufferHelper.renderCube(buffer, poseStack.last(), 0xFF9900, 3, sprite, 0,0,0,5,5,5);
        RenderBufferHelper.renderRing(poseStack, buffer, 0.5F,0,5f,6, 0.2f, 10,20,30,30,30, 60, machine.getFrontFacing().getAxis());
    }
}
