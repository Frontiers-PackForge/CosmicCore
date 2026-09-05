package com.ghostipedia.cosmiccore.mixin.modularui;

import brachy.modularui.widgets.menu.AbstractMenuButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractMenuButton.class, remap = false)
public abstract class MenuButtonCloseLifecycleMixin {

    @Inject(method = "checkClose",
            at = {
                    @At("HEAD"),
                    @At(value = "INVOKE",
                        target = "Lbrachy/modularui/widgets/menu/AbstractMenuButton;closeMenu(Z)V",
                        shift = At.Shift.AFTER)
            },
            cancellable = true)
    private void cosmiccore$stopCloseTraversalAfterDisposal(boolean soft, boolean requireNoHover, CallbackInfo ci) {
        if (!((AbstractMenuButton<?>) (Object) this).isValid()) {
            ci.cancel();
        }
    }
}
