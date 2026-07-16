package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.theme.WidgetTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MachineUIPanel.class, remap = false)
public abstract class MachineUIPanelBackgroundFixMixin {

    @Redirect(
              method = "<init>",
              at = @At(
                       value = "INVOKE",
                       target = "Lbrachy/modularui/theme/WidgetTheme;getBackground()Lbrachy/modularui/api/drawable/IDrawable;"))
    private IDrawable cosmiccore$useTexturePanelBackground(WidgetTheme theme) {
        IDrawable background = theme.getBackground();
        return background instanceof UITexture ? background : GTGuiTextures.BACKGROUND;
    }
}
