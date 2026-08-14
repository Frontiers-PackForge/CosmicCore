package com.ghostipedia.cosmiccore.api.item.component;

import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public interface IBlockCustomRendererProvider {

    @Nullable
    ICustomRenderer getRenderInfo(BlockState blockState);
}
