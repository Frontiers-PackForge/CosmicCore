package com.ghostipedia.cosmiccore.mixin.xaerominimap;

import com.ghostipedia.cosmiccore.client.map.xaero.minimap.FieldBlobElementRenderer;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapFBORenderer;
import xaero.common.minimap.render.MinimapRenderer;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;

@Mixin(value = MinimapFBORenderer.class, remap = false)
public abstract class CCoreMinimapFBORendererMixin extends MinimapRenderer {

    @Shadow
    private MinimapElementMapRendererHandler minimapElementMapRendererHandler;

    public CCoreMinimapFBORendererMixin(HudMod modMain, Minecraft mc, WaypointMapRenderer waypointMapRenderer,
                                        Minimap minimap, CompassRenderer compassRenderer) {
        super(modMain, mc, waypointMapRenderer, minimap, compassRenderer);
    }

    @Inject(method = "loadFrameBuffer",
            at = @At(value = "INVOKE", target = "Lxaero/common/mods/SupportMods;worldmap()Z"))
    private void cosmiccore$injectFieldBlobs(MinimapProcessor minimapProcessor, CallbackInfo ci) {
        FieldBlobElementRenderer renderer = FieldBlobElementRenderer.Builder.begin()
                .build(this.minimapElementMapRendererHandler);
        minimapElementMapRendererHandler.add(renderer);
        this.minimap.getOverMapRendererHandler().add(renderer);
    }
}
