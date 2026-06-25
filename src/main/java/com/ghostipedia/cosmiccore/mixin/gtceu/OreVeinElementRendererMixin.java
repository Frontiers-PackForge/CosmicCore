package com.ghostipedia.cosmiccore.mixin.gtceu;

import com.gregtechceu.gtceu.integration.map.xaeros.common.ore.OreVeinElement;
import com.gregtechceu.gtceu.integration.map.xaeros.minimap.ore.OreVeinElementRenderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;

/**
 * Defense-in-depth companion to {@link GroupingMapRendererMixin}. GTCEu's removeAll (the pack's composite_veins.js
 * uses it on ~41 gtceu veins) does NOT delete a vein - it swaps the generator to NoopVeinGenerator (empty materials)
 * and keeps the gtceu: registry entry. If such a noop vein reaches this renderer (e.g. a marker replayed from Xaero's
 * persisted dim cache), OreVeinElementRenderer#renderElement does {@code veinGenerator().getAllMaterials().getFirst()}
 * which throws NoSuchElementException on the empty list, crashing Xaero's Minimap every render tick. Skip rendering
 * any vein with no materials.
 *
 * TODO(gtm-upstream): GTCEu's OreVeinElementRenderer/OreVeinElement/JourneymapRenderer all getFirst()/get(0) on
 * getAllMaterials() with no empty check - they should guard against noop'd (removed) veins.
 */
@Mixin(value = OreVeinElementRenderer.class, remap = false)
public abstract class OreVeinElementRendererMixin {

    @Inject(method = "renderElement(Lcom/gregtechceu/gtceu/integration/map/xaeros/common/ore/OreVeinElement;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0)
    private void cosmiccore$skipEmptyMaterialVein(OreVeinElement element, boolean highlit, boolean outOfBounds,
                                                  double optionalDepth, float optionalScale, double partialX,
                                                  double partialY, MinimapElementRenderInfo renderInfo,
                                                  GuiGraphics graphics,
                                                  MultiBufferSource.BufferSource renderTypeBuffers,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (element.getVein().definition().value().veinGenerator().getAllMaterials().isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}
