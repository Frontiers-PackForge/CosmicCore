package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.utils.NumberUtils;

import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.widgets.CPUSelectionList;
import appeng.menu.me.crafting.CraftingStatusMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = CPUSelectionList.class)
public abstract class CPUFormattingMixin implements ICompositeWidget {

    /**
     * @author Ghostipedia
     * @reason Truncates CPU Crafting Storages with Formatting
     */
    @Overwrite(remap = false)
    private String formatStorage(CraftingStatusMenu.CraftingCpuListEntry cpu) {
        return NumberUtils.numberFormat(cpu.storage()).getString();
    }
}
