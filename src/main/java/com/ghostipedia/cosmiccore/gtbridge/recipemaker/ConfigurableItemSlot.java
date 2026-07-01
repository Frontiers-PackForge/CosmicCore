package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.value.sync.ItemSlotSyncHandler;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * A phantom item slot whose middle-click runs a callback (used to select the slot for the per-slot options popup).
 * Left/right/scroll fall through to the slot's sync handler, which is a {@link RecipeSlotSyncHandler}.
 */
public class ConfigurableItemSlot extends PhantomItemSlot {

    private final Runnable onConfigure;

    public ConfigurableItemSlot(ItemSlotSyncHandler handler, Runnable onConfigure) {
        syncHandler(handler);
        background(GTGuiTextures.SLOT);
        this.onConfigure = onConfigure;
    }

    @Override
    public Interactable.Result onMousePressed(int button) {
        if (button == InputConstants.MOUSE_BUTTON_MIDDLE && onConfigure != null) {
            onConfigure.run();
            return Interactable.Result.SUCCESS;
        }
        return super.onMousePressed(button);
    }
}
