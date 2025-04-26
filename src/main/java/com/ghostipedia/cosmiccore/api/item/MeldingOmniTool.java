package com.ghostipedia.cosmiccore.api.item;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.common.item.tool.behavior.DisableShieldBehavior;
import com.gregtechceu.gtceu.common.item.tool.behavior.ToolModeSwitchBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;

public final class MeldingOmniTool {

    public static final GTToolType MELD_TOOL_LUV = MeldingOmniTool(GTToolType.builder("luv_meld_tool"));

    private static GTToolType MeldingOmniTool(GTToolType.Builder builder) {
        return builder.idFormat("%s_meld_tool")
                .toolTag(CustomTags.WRENCHES)
                .toolTag(CustomTags.WRENCH)
                .toolTag(CustomTags.WIRE_CUTTERS)
                .toolTag(ItemTags.PICKAXES)
                .toolTag(ItemTags.SHOVELS)
                .toolTag(ItemTags.HOES)
                .toolTag(ItemTags.AXES)
                .harvestTag(BlockTags.MINEABLE_WITH_AXE)
                .harvestTag(BlockTags.MINEABLE_WITH_HOE)
                .harvestTag(BlockTags.MINEABLE_WITH_PICKAXE)
                .harvestTag(BlockTags.MINEABLE_WITH_SHOVEL)
                .harvestTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WIRE_CUTTER)
                .harvestTag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .toolStats(s -> s.crafting().blockBreaking().sneakBypassUse().attacking().attackSpeed(3.5F)
                        .behaviors(DisableShieldBehavior.INSTANCE, ToolModeSwitchBehavior.INSTANCE))
                .toolClasses(GTToolType.WRENCH, GTToolType.WIRE_CUTTER, GTToolType.PICKAXE, GTToolType.SHEARS,
                        GTToolType.AXE)
                .build();
    }
}
