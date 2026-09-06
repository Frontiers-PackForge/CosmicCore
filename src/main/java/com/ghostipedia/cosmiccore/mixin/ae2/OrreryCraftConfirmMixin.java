package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.common.orrery.OrreryCraftingMenuHost;

import appeng.menu.me.crafting.CraftConfirmMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftConfirmMenu.class)
public abstract class OrreryCraftConfirmMixin {

    @Inject(method = "startJob", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$validateOrrery(CallbackInfo ci) {
        if (!OrreryCraftingMenuHost.allowCraft(((CraftConfirmMenu) (Object) this).getHost())) ci.cancel();
    }
}
