package com.ghostipedia.cosmiccore.mixin.xaeroworldmap;

import com.ghostipedia.cosmiccore.client.map.xaero.worldmap.FieldBlobElementRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.map.element.MapElementRenderHandler;
import xaero.map.element.MapElementRenderer;

import java.util.List;

@Mixin(value = MapElementRenderHandler.Builder.class, remap = false)
public class CCoreMapElementRenderHandlerBuilderMixin {

    @ModifyVariable(method = "build", at = @At(value = "LOAD", ordinal = 3))
    private List<MapElementRenderer<?, ?, ?>> cosmiccore$addFieldBlobRenderer(List<MapElementRenderer<?, ?, ?>> value) {
        value.add(FieldBlobElementRenderer.Builder.begin().build());
        return value;
    }
}
