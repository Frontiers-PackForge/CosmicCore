package com.ghostipedia.cosmiccore.common.item;

import com.ghostipedia.cosmiccore.api.item.component.IBlockCustomRendererProvider;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRenderer;
import com.ghostipedia.cosmiccore.api.item.component.ICustomRendererProvider;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

public class RenderBlockItem extends BlockItem implements ICustomRendererProvider {

    public RenderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public ICustomRenderer getRenderInfo(ItemStack stack) {
        if (getBlock() instanceof IBlockCustomRendererProvider provider) {
            return provider.getRenderInfo(getBlock().defaultBlockState());
        }
        return null;
    }
}
