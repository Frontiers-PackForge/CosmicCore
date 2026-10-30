package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.compat.gtceu.transformer.ConfigurableTransformer;
import com.ghostipedia.cosmiccore.common.compat.gtceu.transformer.TransformerStepDownPolicy;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.model.machine.overlays.EnergyIOOverlay;
import com.gregtechceu.gtceu.client.util.quad.transformers.GTQuadTransformers;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.core.util.extensions.BakedQuadExt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mixin(value = MachineModel.class, remap = false)
public abstract class TransformerModelConfigurationMixin {

    @Inject(method = "renderMachine", at = @At("RETURN"), cancellable = true, require = 1)
    private void cosmiccore$renderConfiguredTransformer(@Nullable MetaMachine machine,
                                                        @Nullable BlockAndTintGetter level,
                                                        @Nullable BlockPos pos,
                                                        @Nullable BlockState blockState,
                                                        @Nullable Direction side,
                                                        RandomSource rand,
                                                        ModelData modelData,
                                                        @Nullable RenderType renderType,
                                                        CallbackInfoReturnable<List<BakedQuad>> cir) {
        if (!(machine instanceof TransformerMachine transformer) ||
                !(transformer instanceof ConfigurableTransformer configurable)) {
            return;
        }
        EnergyIOOverlay front = transformer.isTransformUp() ?
                EnergyIOOverlay.OUT_OVERLAYS_FOR_AMP.get(transformer.getBaseAmp()) :
                EnergyIOOverlay.IN_OVERLAYS_FOR_AMP.get(transformer.getBaseAmp());
        int otherAmperage = transformer.isTransformUp() ? transformer.getBaseAmp() * 4 :
                TransformerStepDownPolicy.overlayAmperage(configurable.cosmiccore$getStepDownOutputAmperage());
        EnergyIOOverlay other = transformer.isTransformUp() ?
                EnergyIOOverlay.IN_OVERLAYS_FOR_AMP.get(otherAmperage) :
                EnergyIOOverlay.OUT_OVERLAYS_FOR_AMP.get(otherAmperage);
        cir.setReturnValue(cosmiccore$retexture(cir.getReturnValue(), Map.of(
                "overlay_in_io", cosmiccore$sprite(front.getIoPart()),
                "overlay_in_io_emissive", cosmiccore$sprite(front.getIoPartEmissive()),
                "overlay_in_tinted", cosmiccore$sprite(front.getTintedPart()),
                "overlay_out_io", cosmiccore$sprite(other.getIoPart()),
                "overlay_out_io_emissive", cosmiccore$sprite(other.getIoPartEmissive()),
                "overlay_out_tinted", cosmiccore$sprite(other.getTintedPart()))));
    }

    @Unique
    private static List<BakedQuad> cosmiccore$retexture(List<BakedQuad> quads,
                                                        Map<String, TextureAtlasSprite> overrides) {
        List<BakedQuad> retextured = new LinkedList<>();
        for (BakedQuad quad : quads) {
            String key = ((BakedQuadExt) (Object) quad).gtceu$getTextureKey();
            if (key != null && key.startsWith("#")) key = key.substring(1);
            TextureAtlasSprite sprite = key == null ? null : overrides.get(key);
            retextured.add(sprite == null ? quad : GTQuadTransformers.setSprite(quad, sprite));
        }
        return retextured;
    }

    @Unique
    private static TextureAtlasSprite cosmiccore$sprite(net.minecraft.resources.ResourceLocation texture) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
    }
}
