package com.ghostipedia.cosmiccore.mixin.xaerominimap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;

@Mixin(value = MinimapElementMapRendererHandler.class, remap = false)
public interface MinimapElementMapRendererHandlerAccessor {

    @Accessor("ps")
    double cosmiccore$getTransformPs();

    @Accessor("pc")
    double cosmiccore$getTransformPc();
}
