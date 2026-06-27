package com.ghostipedia.cosmiccore.mixin.modularui;

import brachy.modularui.ModularUIConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MUI2 3.3.0-SNAPSHOT (jar-jar'd in GTCEu 8.0) has a startup-order bug: TestHandler.onOpenScreen reads
 * ModularUIConfig.enableTestOverlays() on the FIRST screen-open, which fires during Minecraft.&lt;init&gt;
 * BEFORE NeoForge loads configs -> ModConfigSpec throws "Cannot get config value before config is loaded"
 * and the client dies before the title screen. Guard the static read: if the spec isn't loaded yet, report
 * test overlays off (the production default) instead of crashing. Once configs load, the real value is used.
 */
@Mixin(value = ModularUIConfig.class, remap = false)
public class ModularUIConfigMixin {

    @Inject(method = "enableTestOverlays", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$guardTestOverlaysBeforeConfigLoaded(CallbackInfoReturnable<Boolean> cir) {
        if (!ModularUIConfig.CONFIG.isLoaded()) {
            cir.setReturnValue(false);
        }
    }
}
