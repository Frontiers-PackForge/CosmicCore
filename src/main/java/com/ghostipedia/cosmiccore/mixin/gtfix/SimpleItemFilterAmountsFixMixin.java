package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimpleItemFilter.class, remap = false)
public abstract class SimpleItemFilterAmountsFixMixin {

    @Shadow
    protected int maxStackSize;

    @Inject(method = { "<init>()V", "<init>(ZZLjava/util/List;)V" }, at = @At("RETURN"), require = 2)
    private void cosmiccore$allowConfiguredCounts(CallbackInfo ci) {
        maxStackSize = TransferMode.TRANSFER_EXACT.maxStackSize;
    }
}
