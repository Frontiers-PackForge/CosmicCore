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
    public DynamicRenderType<WorkableElectricMultiblockMachine, HellFireFoundryWorkableRenderer> getType() {
        return TYPE;
    }


    private void renderHellFire(WorkableElectricMultiblockMachine machine, PoseStack poseStack, VertexConsumer buffer ){
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(new ResourceLocation("occultism", "block/iesnium_block"));

        RenderBufferHelper.renderCube(buffer, poseStack.last(), 0xFF9900, 3, sprite, 0,0,0,5,5,5);
        RenderBufferHelper.renderRing(poseStack, buffer, 0.5F,0,5f,6, 0.2f, 10,20,30,30,30, 60, machine.getFrontFacing().getAxis());
    }
}
