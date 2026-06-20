package com.ghostipedia.cosmiccore.client.renderer.utility;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.mojang.blaze3d.systems.RenderSystem;

public class CosmicCoreRenderUtils {

    public static void bindTexture(ResourceLocation resloc) {
        RenderSystem.setShaderTexture(0, resloc);
    }

    public static void bindBlockAtlas() {
        bindTexture(InventoryMenu.BLOCK_ATLAS);
    }
}
