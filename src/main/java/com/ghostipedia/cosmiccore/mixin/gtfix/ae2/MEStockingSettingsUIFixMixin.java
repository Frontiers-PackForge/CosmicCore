package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEScrollableAmountField;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = IMEStockingPart.class, remap = false)
public interface MEStockingSettingsUIFixMixin {

    @Redirect(method = "lambda$getPanelBuilder$2",
              at = @At(value = "NEW",
                       target = "brachy/modularui/widgets/textfield/TextFieldWidget"),
              require = 2)
    private TextFieldWidget cosmiccore$scrollStockingSettings() {
        return new MEScrollableAmountField();
    }
}
