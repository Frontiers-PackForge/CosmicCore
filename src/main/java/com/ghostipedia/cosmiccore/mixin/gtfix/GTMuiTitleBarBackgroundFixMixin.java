package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.theme.WidgetTheme;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GTMuiWidgets.class, remap = false)
public abstract class GTMuiTitleBarBackgroundFixMixin {

    @Redirect(
              method = "createTitleBar(Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;I)Lbrachy/modularui/widgets/layout/Flow;",
              at = @At(
                       value = "INVOKE",
                       target = "Lbrachy/modularui/theme/WidgetTheme;getBackground()Lbrachy/modularui/api/drawable/IDrawable;"))
    private static IDrawable cosmiccore$useTextureTitleBackground(WidgetTheme theme) {
        IDrawable background = theme.getBackground();
        return background instanceof UITexture ? background : GTGuiTextures.BACKGROUND;
    }
}
