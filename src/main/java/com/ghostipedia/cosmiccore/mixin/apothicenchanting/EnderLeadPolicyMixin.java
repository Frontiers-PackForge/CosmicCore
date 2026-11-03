package com.ghostipedia.cosmiccore.mixin.apothicenchanting;

import net.minecraft.world.InteractionResult;

import dev.shadowsoffire.apothic_enchanting.objects.EnderLeadItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderLeadItem.class)
public abstract class EnderLeadPolicyMixin {

    @Inject(method = "canLeash", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableCapture(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$disableUse(CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
