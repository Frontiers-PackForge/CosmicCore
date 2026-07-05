package com.ghostipedia.cosmiccore.mixin.ftbchunks;

import com.mojang.blaze3d.platform.InputConstants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "dev.ftb.mods.ftbchunks.client.FTBChunksClient", remap = false)
public class FTBChunksMapKeybindMixin {

    @ModifyArg(method = "registerKeys",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/KeyMapping;<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
                        ordinal = 0),
               index = 2)
    private int cosmiccore$unbindMapKey(int keyCode) {
        return InputConstants.UNKNOWN.getValue();
    }
}
