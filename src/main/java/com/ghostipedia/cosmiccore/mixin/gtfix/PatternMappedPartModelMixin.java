package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.api.machine.multiblock.PatternMappedPartAppearance;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.util.RenderUtil;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = MachineModel.class, remap = false)
public class PatternMappedPartModelMixin {

    @ModifyVariable(method = "renderPartOverrides", at = @At(value = "STORE", ordinal = 0), ordinal = 0, require = 1)
    private Map<String, TextureAtlasSprite> cosmiccore$applyPatternMappedAppearance(
                                                                                    Map<String, TextureAtlasSprite> overrides,
                                                                                    MachineModel controllerModel,
                                                                                    MultiblockControllerMachine controller,
                                                                                    List<BakedQuad> quads,
                                                                                    MultiblockPartMachine part,
                                                                                    Direction frontFacing,
                                                                                    @Nullable Direction side,
                                                                                    RandomSource rand,
                                                                                    ModelData modelData,
                                                                                    @Nullable RenderType renderType) {
        if (!(controller.getDefinition().getPartAppearance() instanceof PatternMappedPartAppearance appearance)) {
            return overrides;
        }
        var state = appearance.apply(controller, part, side == null ? frontFacing : side);
        var sprite = RenderUtil.getModelForState(state).getParticleIcon(modelData);
        Map<String, TextureAtlasSprite> mapped = new HashMap<>(overrides);
        mapped.replaceAll((key, value) -> sprite);
        return mapped;
    }
}
