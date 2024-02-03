package com.ghostipedia.cosmiccore.gtbridge.pipenet.item;

import com.ghostipedia.cosmiccore.gtbridge.pipenet.blockentities.PressurePipeBlock;
import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class PressurePipeBlockItem extends PipeBlockItem implements IItemRendererProvider {

    public PressurePipeBlockItem(PipeBlock block, Properties properties) {
        super(block, properties);
    }

    @Override
    public PressurePipeBlock getBlock() {
        return (PressurePipeBlock) super.getBlock();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(ItemStack stack) {
        return getBlock().getRenderer(getBlock().defaultBlockState());
    }
}
