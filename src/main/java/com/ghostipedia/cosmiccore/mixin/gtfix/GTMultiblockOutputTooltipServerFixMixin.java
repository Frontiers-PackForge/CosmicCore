package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;

import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = GTMultiblockTextUtil.class, remap = false)
public abstract class GTMultiblockOutputTooltipServerFixMixin {

    @Redirect(
              method = "createItemLineForOutput",
              at = @At(
                       value = "INVOKE",
                       target = "Lbrachy/modularui/widget/Widget;tooltip(Ljava/util/function/Consumer;)Lbrachy/modularui/api/widget/ITooltip;"))
    private static ITooltip<?> cosmiccore$deferItemTooltip(Widget<?> widget, Consumer<RichTooltip> tooltipBuilder) {
        return widget.tooltipBuilder(tooltipBuilder);
    }
}
