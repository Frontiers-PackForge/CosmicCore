package com.ghostipedia.cosmiccore.client.renderer.machine;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SoulHatchPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HellFireFoundryWorkableRenderer extends WorkableCasingMachineRenderer implements IControllerRenderer {

    public final ResourceLocation multipartSprite;

    public HellFireFoundryWorkableRenderer(ResourceLocation texture, ResourceLocation multipartSprite,
                                           ResourceLocation workableModel) {
        super(texture, workableModel);
        this.multipartSprite = multipartSprite;
    }

    @Override
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
                                @Nullable Direction side, RandomSource rand, Direction modelFacing,
                                ModelState modelState) {
        if (modelFacing != null) {
            if (part instanceof SoulHatchPartMachine) {
                quads.add(StaticFaceBakery.bakeFace(modelFacing,
                        ModelFactory.getBlockSprite(new ResourceLocation("occultism", "block/iesnium_block")),
                        modelState));
            } else {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(multipartSprite),
                        modelState));
            }
        }
    }
}
