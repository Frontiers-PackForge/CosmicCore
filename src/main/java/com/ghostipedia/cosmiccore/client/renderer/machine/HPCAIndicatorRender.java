package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAModifier;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.ModelUtils;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.client.util.StaticFaceBakery.SLIGHTLY_OVER_BLOCK;

public class HPCAIndicatorRender extends DynamicRender<HPCAIndicatorPartMachine, HPCAIndicatorRender> {

    public static final HPCAIndicatorRender INSTANCE = new HPCAIndicatorRender();
    public static final Codec<HPCAIndicatorRender> CODEC = Codec.unit(HPCAIndicatorRender.INSTANCE);
    public static final DynamicRenderType<HPCAIndicatorPartMachine, HPCAIndicatorRender> TYPE = new DynamicRenderType<>(
            HPCAIndicatorRender.CODEC);

    private static final Map<HPCAModifier, TextureAtlasSprite> MODIFIER_SPRITES = new EnumMap<>(HPCAModifier.class);
    private static boolean isOverlayListenerInitialized = false;

    private HPCAIndicatorRender() {
        if (!isOverlayListenerInitialized) {
            ModelUtils.registerAtlasStitchedEventListener(true, InventoryMenu.BLOCK_ATLAS, event -> {
                MODIFIER_SPRITES.clear();
                for (HPCAModifier modifier : HPCAModifier.VALUES) {
                    MODIFIER_SPRITES.put(modifier, event.getAtlas().getSprite(modifier.overlay));
                }
            });
            isOverlayListenerInitialized = true;
        }
    }

    @Override
    public @NotNull List<BakedQuad> getRenderQuads(@Nullable HPCAIndicatorPartMachine machine,
                                                   @Nullable BlockAndTintGetter level,
                                                   @Nullable BlockPos pos, @Nullable BlockState blockState,
                                                   @Nullable Direction side, RandomSource rand,
                                                   @NotNull ModelData modelData, @Nullable RenderType renderType) {
        if (machine == null) return Collections.emptyList();

        SortedSet<IMultiController> controllers = machine.getControllers();
        if (controllers.isEmpty() || !(controllers.first() instanceof HPCAMachine controller)) {
            return Collections.emptyList();
        }

        Direction front = machine.getFrontFacing();
        HPCAModifier modifier = controller.getModifier(machine.getBlockPos());

        BakedQuad q = StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, front,
                MODIFIER_SPRITES.get(modifier), BlockModelRotation.X0_Y0, -1, 15, true, false);
        return Collections.singletonList(q);
    }

    @Override
    public @NotNull DynamicRenderType<HPCAIndicatorPartMachine, HPCAIndicatorRender> getType() {
        return TYPE;
    }

    @Override
    public void render(HPCAIndicatorPartMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (RelativeDirection dir : RelativeDirection.values()) {

        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(HPCAIndicatorPartMachine machine) {
        return false;
    }

    @Override
    public boolean shouldRender(HPCAIndicatorPartMachine machine, Vec3 cameraPos) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(HPCAIndicatorPartMachine machine) {
        return super.getRenderBoundingBox(machine);
    }

    // @Override
    // public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
    // @NotNull RandomSource rand) {
    // return super.getQuads(state, side, rand);
    // }
}
