package com.ghostipedia.cosmiccore.api.misc;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import com.mojang.datafixers.util.Pair;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

public interface IPipeBlockEntityMixin {

    public default Pair<ToolDefinition, InteractionResult> ccore$onToolClick(ModifiableItem ticonItem,
                                                                             UseOnContext context) {
        return Pair.of(null, InteractionResult.PASS);
    }
}
