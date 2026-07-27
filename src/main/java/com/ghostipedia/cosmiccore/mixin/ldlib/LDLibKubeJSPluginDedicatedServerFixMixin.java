package com.ghostipedia.cosmiccore.mixin.ldlib;

import com.lowdragmc.lowdraglib.LDLib;

import dev.latvian.mods.kubejs.script.BindingRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.lowdragmc.lowdraglib.kjs.LDLibKubeJSPlugin", remap = false)
public class LDLibKubeJSPluginDedicatedServerFixMixin {

    @Inject(method = "registerBindings", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$skipClientBindingsOnDedicatedServer(BindingRegistry bindings, CallbackInfo ci) {
        if (!LDLib.isClient()) {
            ci.cancel();
        }
    }
}
