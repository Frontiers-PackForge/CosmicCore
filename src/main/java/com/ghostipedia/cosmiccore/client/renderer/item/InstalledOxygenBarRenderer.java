package com.ghostipedia.cosmiccore.client.renderer.item;

import com.ghostipedia.cosmiccore.common.armor.InstalledOxygen;
import com.ghostipedia.cosmiccore.common.item.OxygenTankItem;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public final class InstalledOxygenBarRenderer implements IItemDecorator {

    public static final InstalledOxygenBarRenderer INSTANCE = new InstalledOxygenBarRenderer();

    private InstalledOxygenBarRenderer() {}

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        ItemStack tank = InstalledOxygen.tankItem(stack);
        if (tank.isEmpty()) return false;
        int offset = stack.isBarVisible() ? 2 : 0;
        var electric = GTCapabilityHelper.getElectricItem(stack);
        if (electric != null && electric.getCharge() > 0 && electric.getMaxCharge() > 0) offset += 2;
        int color = 0xFF000000 | OxygenTankItem.BAR_COLOR;
        ToolChargeBarRenderer.render(graphics, OxygenTankItem.oxygenBarWidth(tank), x, y, offset, true,
                color, color, false);
        return true;
    }
}
