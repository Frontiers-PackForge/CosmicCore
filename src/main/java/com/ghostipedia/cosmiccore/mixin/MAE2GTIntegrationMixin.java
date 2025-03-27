package com.ghostipedia.cosmiccore.mixin;

import net.minecraftforge.eventbus.api.IEventBus;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stone.mae2.integration.GregTechIntegration;

@Debug(export = true)
@Mixin(value = GregTechIntegration.class, remap = false)
public abstract class MAE2GTIntegrationMixin {

    @Inject(method = "init",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraftforge/eventbus/api/IEventBus;addListener(Ljava/util/function/Consumer;)V"),
            cancellable = true,
            remap = false)
    private static void init(IEventBus bus, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }
}
