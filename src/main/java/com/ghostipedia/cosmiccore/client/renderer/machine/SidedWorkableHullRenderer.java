package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.block.WorkableSteamHullType;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SidedWorkableHullRenderer extends WorkableCasingMachineRenderer implements IControllerRenderer {

    public static final ResourceLocation BLOOM_OVERLAY = GTCEu.id("block/casings/firebox/machine_casing_firebox_bloom");
    public final WorkableSteamHullType hullType;

    public SidedWorkableHullRenderer(ResourceLocation texture, WorkableSteamHullType hullType,
                                     ResourceLocation workableModel) {
        super(texture, workableModel);
        this.hullType = hullType;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
                                @Nullable Direction side, RandomSource rand, Direction modelFacing,
                                ModelState modelState) {
        if (machine.self().getPos().atY(BlockPos.getY(machine.self().getPos().asLong())).getY() ==
                part.self().getPos().getY()) {
            if (side != null && modelFacing != null) {
                if (side == Direction.UP) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(hullType.top()), modelState));
                } else if (side == Direction.DOWN) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(hullType.bottom()), modelState));
                } else {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(hullType.side()), modelState));
                }
            }
        } else {
            if (side != null && modelFacing != null) {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(baseCasing), modelState));
            }
        }
    }
}
