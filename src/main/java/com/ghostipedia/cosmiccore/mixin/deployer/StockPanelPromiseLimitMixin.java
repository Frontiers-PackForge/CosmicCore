package com.ghostipedia.cosmiccore.mixin.deployer;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimit;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;

import net.liukrast.deployer.lib.logistics.board.StockPanelBehaviour;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StockPanelBehaviour.class)
public abstract class StockPanelPromiseLimitMixin {

    @Inject(
            method = "tryRestock",
            at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Math;clamp(III)I"),
            cancellable = true)
    private void cosmiccore$limitRestock(
                                         CallbackInfo ci,
                                         @Local(ordinal = 2) int promised,
                                         @Local(ordinal = 4) LocalIntRef amountToOrder) {
        FactoryPanelBehaviour behaviour = (FactoryPanelBehaviour) (Object) this;
        if (!FactoryGaugePromiseLimitSupport.isFluid(behaviour) ||
                !(behaviour instanceof FactoryGaugePromiseLimit promiseLimit)) {
            return;
        }
        int configured = promiseLimit.cosmiccore$getPromiseLimit();
        if (configured < 0) return;
        int amount = Math.min(amountToOrder.get(), configured - promised);
        if (amount <= 0) {
            ci.cancel();
            return;
        }
        amountToOrder.set(amount);
    }
}
