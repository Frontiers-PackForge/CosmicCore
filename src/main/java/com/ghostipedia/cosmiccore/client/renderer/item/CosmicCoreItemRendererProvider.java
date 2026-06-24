package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import net.minecraft.world.item.ItemStack;

public interface CosmicCoreItemRendererProvider extends IItemRendererProvider {

    ICustomRenderer getRenderInfo(ItemStack itemStack);

    /**
     * Shared default so the item mixins (ComponentItemMixin, TagPrefixItemMixin) only need to supply
     * getRenderInfo - the getRenderer wiring lives in one place. (LDLib's IRenderer resolves fine at runtime
     * now that LDLib is a runtimeOnly dep; GTCEu 8.0 stopped bundling it, see dependencies.gradle.)
     */
    @Override
    default IRenderer getRenderer(ItemStack itemStack) {
        ICustomRenderer info = getRenderInfo(itemStack);
        return info != null ? info.getRenderer() : null;
    }
}
