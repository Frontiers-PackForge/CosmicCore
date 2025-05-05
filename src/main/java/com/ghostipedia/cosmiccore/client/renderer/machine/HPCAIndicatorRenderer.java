package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.TieredHullMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HPCAIndicatorRenderer extends TieredHullMachineRenderer {

    public static final ResourceLocation BASE = CosmicCore.id("block/overlay/machine/hpca/indicator");

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f, 1.001f, 1.001f, 1.001f);

    public HPCAIndicatorRenderer() {
        super(GTValues.ZPM, GTCEu.id("block/computer_casing"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing,
                              ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side != frontFacing || modelFacing == null) return;

        quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing, ModelFactory.getBlockSprite(BASE),
                modelState, -1, 0, true, false));

        if (machine instanceof HPCAIndicatorPartMachine indicatorPart) {
            var controllers = indicatorPart.getControllers();
            if (controllers.isEmpty()) return;
            if (controllers.first() instanceof HPCAMachine controller) {
                var modifier = controller.getModifier(machine.getPos());
                quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing,
                        ModelFactory.getBlockSprite(modifier.overlay), modelState, -1, 15, true, false));
            }
        }
    }
}
