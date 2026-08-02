package com.ghostipedia.cosmiccore.mixin.malum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "com.sammy.malum.client.renderer.renderpass.ParallelWorldRenderer", remap = false)
public abstract class ParallelWorldRendererInitialSizeFixMixin {

    @ModifyArgs(
                method = "<init>",
                at = @At(
                         value = "INVOKE",
                         target = "Lcom/mojang/blaze3d/pipeline/TextureTarget;<init>(IIZZ)V",
                         remap = false),
                require = 1)
    private void cosmiccore$clampInitialFramebufferSize(Args args) {
        args.set(0, Math.max(1, args.<Integer>get(0)));
        args.set(1, Math.max(1, args.<Integer>get(1)));
    }
}
