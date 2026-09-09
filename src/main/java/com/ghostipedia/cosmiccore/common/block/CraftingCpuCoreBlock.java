package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.nebulaeae2.crafting.CraftingComputeTuning;
import com.ghostipedia.nebulaeae2.crafting.api.ICraftingCpuComponent;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;

import java.util.List;

public class CraftingCpuCoreBlock extends AbstractCraftingUnitBlock<CraftingBlockEntity>
                                  implements ICraftingCpuComponent {

    private final CraftingCpuCoreType coreType;

    public CraftingCpuCoreBlock(Properties properties, CraftingCpuCoreType coreType) {
        super(properties, coreType);
        this.coreType = coreType;
    }

    @Override
    public int accelerationCores() {
        return coreType == CraftingCpuCoreType.ACCELERATION ? 1 : 0;
    }

    @Override
    public int parallelCores() {
        return coreType == CraftingCpuCoreType.PARALLEL ? 1 : 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (coreType == CraftingCpuCoreType.ACCELERATION) {
            tooltip.add(Component.translatable("cosmiccore.crafting.acceleration_core.effect"));
            tooltip.add(Component.translatable("cosmiccore.crafting.core.limit",
                    CraftingComputeTuning.MAX_ACCELERATION_CORES));
        } else {
            tooltip.add(Component.translatable("cosmiccore.crafting.parallel_core.effect"));
            tooltip.add(
                    Component.translatable("cosmiccore.crafting.core.limit", CraftingComputeTuning.MAX_PARALLEL_CORES));
        }
    }
}
