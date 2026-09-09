package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigWidget;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.text.ModularComponent;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AEConfigWidget.class, remap = false)
public abstract class AEConfigWidgetInteractionFixMixin extends Widget<AEConfigWidget> {

    @Shadow
    private PanelSyncManager syncManager;
    @Shadow
    private AEConfigSyncHandler configSyncHandler;

    @Shadow
    private boolean isStocking() {
        return false;
    }

    @Shadow
    private boolean isAutoPull() {
        return false;
    }

    @Shadow
    private int getSlotAtLocal(double x, double y) {
        return -1;
    }

    @Shadow
    private int slotY(int index) {
        return 0;
    }

    @Inject(method = "onMouseScrolled", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$scrollLocalConfiguration(double scrollX, double scrollY,
                                                     CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        if (isStocking() || isAutoPull() || syncManager == null || configSyncHandler == null ||
                scrollY == 0 || !Double.isFinite(scrollY))
            return;
        double x = getContext().getMouseX();
        double y = getContext().getMouseY();
        int index = getSlotAtLocal(x, y);
        if (index < 0 || y >= slotY(index) + 18) return;
        var config = configSyncHandler.getClientConfig(index);
        if (config == null) return;
        boolean control = Interactable.hasControlDown();
        syncManager.callSyncedAction("cosmiccore_ae_config_scroll", buf -> {
            buf.writeVarInt(index);
            buf.writeBoolean(scrollY > 0);
            buf.writeBoolean(control);
        });
        cir.setReturnValue(true);
    }

    @Inject(method = "openAmountEditor", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$keepStockingConfigurationKeyOnly(int index, CallbackInfo ci) {
        if (isStocking()) ci.cancel();
    }

    @Redirect(method = "buildAmountEditor",
              at = @At(value = "INVOKE",
                       target = "Lbrachy/modularui/api/drawable/Text;str(Ljava/lang/String;)Lbrachy/modularui/drawable/text/ModularComponent;",
                       ordinal = 0))
    private ModularComponent cosmiccore$translateAmountTitle(String ignored) {
        return Text.lang("cosmiccore.gui.me.amount");
    }
}
