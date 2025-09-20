package com.ghostipedia.cosmiccore.common.item.tcon;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

public class TiconUtils {

    public static ToolStack getTool(ItemStack stack) {
        return ToolStack.from(stack);
    }

    public static List<ModifierEntry> getModifierList(ToolStack tool) {
        return tool.getModifierList();
    }
}
