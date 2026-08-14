package com.ghostipedia.cosmiccore.api.item.component;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface ICustomRendererProvider {

    @Nullable
    ICustomRenderer getRenderInfo(ItemStack itemStack);
}
