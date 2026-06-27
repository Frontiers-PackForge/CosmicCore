package com.ghostipedia.cosmiccore.mixin.modularui;

import brachy.modularui.ModularUIConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Sibling of {@link ModularUIConfigMixin} for the SAME MUI2 3.3.0-SNAPSHOT startup-order bug down a different path:
 * OverlayStack.onOpenScreen reads ModularUIConfig.Dev.debugUI() on the FIRST screen-open, which - with loading-screen
 * mods like fancymenu/drippyloadingscreen present - fires during Minecraft.&lt;init&gt; BEFORE NeoForge loads configs,
 * so ModConfigSpec throws "Cannot get config value before config is loaded" and the client dies before the title
 * screen. Guard the static read: if the spec isn't loaded yet, report the debug UI off (the production default).
 * Once configs load, the real value is used. debugUI() gates the rest of onOpenScreen, so guarding it alone is enough.
 */
@Mixin(value = ModularUIConfig.Dev.class, remap = false)
public class ModularUIConfigDevMixin {

    @Inject(method = "debugUI", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$guardDebugUiBeforeConfigLoaded(CallbackInfoReturnable<Boolean> cir) {
        if (!ModularUIConfig.CONFIG.isLoaded()) {
            cir.setReturnValue(false);
        }
    }
}
