package com.ghostipedia.cosmiccore.common.item.tcon;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;

import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;

public class TiconUtils {

    public static ToolStack getTool(ItemStack stack) {
        return ToolStack.from(stack);
    }

    public static List<ModifierEntry> getModifierList(ToolStack tool) {
        return tool.getModifierList();
    }

    public static GTToolType getGTToolType(ToolDefinition def) {
        if (def == CosmicToolDefinitions.WRENCHES) {
            return GTToolType.WRENCH;
        } else if (def == CosmicToolDefinitions.WIRE_CUTTERS) {
            return GTToolType.WIRE_CUTTER;
        }
        return GTToolType.FILE;
    }
}
