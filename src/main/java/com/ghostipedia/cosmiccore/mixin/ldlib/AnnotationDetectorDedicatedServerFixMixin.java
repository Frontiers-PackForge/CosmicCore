package com.ghostipedia.cosmiccore.mixin.ldlib;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.lowdragmc.lowdraglib.gui.editor.runtime.AnnotationDetector", remap = false)
public class AnnotationDetectorDedicatedServerFixMixin {

    @Inject(method = "checkNoArgsConstructor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cosmiccore$avoidClientAnnotationResolution(
                                                                   Object annotation, Class<?> type,
                                                                   CallbackInfoReturnable<Boolean> cir) {
        if (LDLib.isClient()) {
            return;
        }
        if (annotation instanceof LDLRegister register &&
                !register.modID().isEmpty() &&
                !LDLib.isModLoaded(register.modID())) {
            cir.setReturnValue(false);
            return;
        }
        try {
            type.getDeclaredConstructor();
            cir.setReturnValue(true);
        } catch (NoSuchMethodException ignored) {
            cir.setReturnValue(false);
        }
    }
}
