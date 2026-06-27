package com.ghostipedia.cosmiccore.mixin.gtfix;

import net.neoforged.neoforge.common.ModConfigSpec;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTarget.class)
public class RenderTargetStencilConfigFixMixin {

    @Redirect(method = "createBuffers",
              at = @At(value = "INVOKE",
                       target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;get()Ljava/lang/Object;"),
              require = 0)
    private Object cosmiccore$guardStencilConfigBeforeLoaded(ModConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return Boolean.FALSE;
        }
    }
}
