package com.ghostipedia.cosmiccore.mixin.disable_advancements;

import net.minecraft.server.ServerAdvancementManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin {

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void cosmiccore$onApply(CallbackInfo ci) {
        // Cancelling this method prevents advancement definitions from being loaded on the logical server.
        // As a result, no advancements are available to synchronize to clients, so the client sees an empty advancement
        // tree.
        ci.cancel();
    }
}
