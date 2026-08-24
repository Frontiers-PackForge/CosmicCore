package com.ghostipedia.cosmiccore.common.block;

import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PowerCapacitorBatteryBlock extends BatteryBlock {

    public PowerCapacitorBatteryBlock(Properties properties, PowerCapacitorBatteryData data) {
        super(properties, data);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        if (getData().getTier() == -1) {
            tooltip.add(Component.translatable("cosmiccore.block.power_capacitor.tooltip_empty"));
        } else {
            tooltip.add(Component.translatable("block.gtceu.substation_capacitor.tooltip_filled",
                    FormattingUtil.formatNumbers(getData().getCapacity())));
        }
    }
}
