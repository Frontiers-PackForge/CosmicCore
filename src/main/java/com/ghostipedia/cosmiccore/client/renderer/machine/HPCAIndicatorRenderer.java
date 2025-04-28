package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.HPCAIndicatorPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class HPCAIndicatorRenderer extends MachineRenderer {

    public static final ResourceLocation BASE = CosmicCore.id("block/overlay/machine/hpca/indicator");
    public static final ResourceLocation RED_OVERLAY = CosmicCore.id("block/overlay/machine/hpca/indicator_red");
    public static final ResourceLocation YELLOW_OVERLAY = CosmicCore.id("block/overlay/machine/hpca/indicator_yellow");
    public static final ResourceLocation GREEN_OVERLAY = CosmicCore.id("block/overlay/machine/hpca/indicator_green");

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f, 1.001f, 1.001f, 1.001f);

    public HPCAIndicatorRenderer() {
        super(GTCEu.id("block/machine/hull_machine"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine,
                              Direction frontFacing, @Nullable Direction side, RandomSource rand,
                              @Nullable Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (side != frontFacing || modelFacing == null) return;

        quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing, ModelFactory.getBlockSprite(BASE),
                modelState, -1, 0, true, true));
        if (machine instanceof HPCAIndicatorPartMachine indicatorPart) {
            var controllers = indicatorPart.getControllers();
            if (controllers.isEmpty()) return;
            if (controllers.first() instanceof HPCAMachine controller) {
                var modifier = controller.getModifier(machine.getPos());
                quads.add(StaticFaceBakery.bakeFace(SLIGHTLY_OVER_BLOCK, modelFacing,
                        ModelFactory.getBlockSprite(modifier.overlay), modelState, -1, 0, true, true));
            }
        }
    }

    @Override
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(BASE);
            register.accept(RED_OVERLAY);
            register.accept(YELLOW_OVERLAY);
            register.accept(GREEN_OVERLAY);
        }
    }
}
