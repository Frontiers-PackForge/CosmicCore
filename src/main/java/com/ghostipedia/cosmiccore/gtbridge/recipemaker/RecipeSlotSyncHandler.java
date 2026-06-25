package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import net.minecraft.world.item.ItemStack;

import brachy.modularui.utils.MouseData;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.widgets.slot.ModularSlot;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * Phantom item slot handler that swaps the count-edit buttons: left-click adds one, right-click removes one (the
 * reverse of ModularUI2's default). Placement, scroll, and shift-clear stay vanilla.
 */
public class RecipeSlotSyncHandler extends PhantomItemSlotSyncHandler {

    public RecipeSlotSyncHandler(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        ItemStack slotStack = getSlot().getItem();
        if (!slotStack.isEmpty() && cursorStack.isEmpty()) {
            if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_LEFT) {
                if (mouseData.shift()) {
                    getSlot().set(ItemStack.EMPTY);
                } else {
                    incrementStackCount(1);
                }
            } else if (mouseData.mouseButton() == InputConstants.MOUSE_BUTTON_RIGHT) {
                incrementStackCount(-1);
            }
            return;
        }
        super.phantomClick(mouseData, cursorStack);
    }
}
