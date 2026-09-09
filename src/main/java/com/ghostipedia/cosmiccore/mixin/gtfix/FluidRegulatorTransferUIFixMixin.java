package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.common.cover.FluidRegulatorCover;

import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FluidRegulatorCover.class, remap = false)
public abstract class FluidRegulatorTransferUIFixMixin {

    @Shadow
    private boolean shouldShowTransferSize() {
        return false;
    }

    @ModifyExpressionValue(method = "createCoverUIRows",
                           at = @At(value = "INVOKE",
                                    target = "Lcom/gregtechceu/gtceu/common/mui/GTMuiWidgets;createIntInputWithBucketMode(Lbrachy/modularui/value/sync/IntSyncValue;Lbrachy/modularui/value/sync/EnumSyncValue;Ljava/util/function/IntSupplier;)Lbrachy/modularui/widget/ParentWidget;"))
    private ParentWidget<?> cosmiccore$showTransferAmountWhenUsed(ParentWidget<?> row) {
        return row.setEnabledIf($ -> shouldShowTransferSize());
    }

    @Redirect(method = "createCoverUIRows",
              at = @At(value = "INVOKE",
                       target = "Lbrachy/modularui/widgets/layout/Flow;child(Lbrachy/modularui/api/widget/IWidget;)Lbrachy/modularui/api/widget/IParentWidget;",
                       ordinal = 1))
    private IParentWidget<?, ?> cosmiccore$omitDuplicateTransferAmount(Flow column, IWidget ignored) {
        return column;
    }
}
