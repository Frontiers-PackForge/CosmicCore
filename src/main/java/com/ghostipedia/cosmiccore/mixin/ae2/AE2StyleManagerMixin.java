package com.ghostipedia.cosmiccore.mixin.ae2;

import appeng.client.gui.style.StyleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = StyleManager.class, remap = false)
public class AE2StyleManagerMixin {

    @ModifyVariable(method = "loadStyleDoc", at = @At("HEAD"), argsOnly = true)
    private static String cosmicCore$replacePatternTerminalLayout(String path) {
        if (path.endsWith("wireless_pattern_encoding_terminal.json")) {
            return "/screens/wtlib/modify_wireless_pattern_encoding_terminal.json";
        } else if (path.endsWith("pattern_encoding_terminal.json")) {
            return "/screens/terminals/modify_pattern_encoding_terminal.json";
        }
        return path;
    }
}
