package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.slot.FluidSlot;
import com.mojang.blaze3d.platform.InputConstants;

/**
 * A phantom fluid slot whose middle-click runs a callback (used to select the slot for the per-slot options popup)
 * instead of clearing the tank. Left/right/scroll fall through to the normal fluid behaviour.
 */
public class ConfigurableFluidSlot extends FluidSlot {

    private final Runnable onConfigure;

    public ConfigurableFluidSlot(FluidSlotSyncHandler handler, Runnable onConfigure) {
        syncHandler(handler);
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
