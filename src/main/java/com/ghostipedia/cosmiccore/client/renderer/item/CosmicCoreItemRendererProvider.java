package com.ghostipedia.cosmiccore.client.renderer.item;

import com.gregtechceu.gtceu.api.item.component.ICustomRenderer;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import net.minecraft.world.item.ItemStack;

public interface CosmicCoreItemRendererProvider extends IItemRendererProvider {
    ICustomRenderer getRenderInfo(ItemStack itemStack);
}
