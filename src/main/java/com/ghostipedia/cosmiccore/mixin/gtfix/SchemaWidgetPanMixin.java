package com.ghostipedia.cosmiccore.mixin.gtfix;

import brachy.modularui.widgets.SchemaWidget;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SchemaWidget.class, remap = false)
public class SchemaWidgetPanMixin {

    @ModifyVariable(method = "onMouseDrag(IDD)V",
                    at = @At("HEAD"),
                    argsOnly = true,
                    ordinal = 0,
                    remap = false,
                    require = 0)
    private int cosmiccore$rightDragPans(int button) {
        return button == InputConstants.MOUSE_BUTTON_RIGHT ? InputConstants.MOUSE_BUTTON_MIDDLE : button;
    }
}
