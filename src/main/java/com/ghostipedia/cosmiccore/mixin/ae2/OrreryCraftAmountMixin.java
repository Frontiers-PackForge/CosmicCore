package com.ghostipedia.cosmiccore.mixin.ae2;

import com.ghostipedia.cosmiccore.common.orrery.OrreryCraftingMenuHost;

import appeng.menu.me.crafting.CraftAmountMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftAmountMenu.class)
public abstract class OrreryCraftAmountMixin {

    @Inject(method = "confirm", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$validateOrrery(int amount, boolean missing, boolean autoStart, CallbackInfo ci) {
        if (!OrreryCraftingMenuHost.allowCraft(((CraftAmountMenu) (Object) this).getHost())) ci.cancel();
    }
}
