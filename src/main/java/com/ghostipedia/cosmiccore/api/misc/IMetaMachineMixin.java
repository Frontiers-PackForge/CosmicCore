package com.ghostipedia.cosmiccore.api.misc;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import slimeknights.tconstruct.library.tools.item.ModifiableItem;

public interface IMetaMachineMixin {

    public default InteractionResult ccore$onToolClick(ModifiableItem ticonItem, UseOnContext context) {
        return InteractionResult.PASS;
    }
}
