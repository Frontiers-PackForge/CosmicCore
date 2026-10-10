package com.ghostipedia.cosmiccore.mixin.gttweak.lifecycle;

import com.ghostipedia.cosmiccore.common.rate.RateCalculatorTracker;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RateCalculatorRecipeSyncMixin {

    @Inject(method = "handleUpdateRecipes",
            at = @At(value = "INVOKE",
                     target = "Lnet/neoforged/neoforge/client/ClientHooks;onRecipesUpdated(Lnet/minecraft/world/item/crafting/RecipeManager;)V",
                     shift = At.Shift.BEFORE),
            order = 900)
    private void cosmiccore$invalidateRateCalculator(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        RateCalculatorTracker.invalidateAll();
    }
}
