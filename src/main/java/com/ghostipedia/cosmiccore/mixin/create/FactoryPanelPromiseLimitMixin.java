package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimit;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryPanelPromiseLimitMixin implements FactoryGaugePromiseLimit {

    @Unique
    private static final String COSMICCORE_PROMISE_LIMIT_KEY = "CosmicCorePromiseLimit";

    @Shadow
    public boolean active;
    @Shadow
    public int recipeOutput;
    @Shadow
    public FactoryPanelBlock.PanelSlot slot;

    @Shadow
    public abstract FactoryPanelBlockEntity panelBE();

    @Shadow
    public abstract int getPromised();

    @Unique
    private int cosmiccore$promiseLimit = -1;

    @Override
    public int cosmiccore$getPromiseLimit() {
        return cosmiccore$promiseLimit;
    }

    @Override
    public void cosmiccore$setPromiseLimit(int limit) {
        cosmiccore$promiseLimit = limit < 0 ? -1 : limit;
    }

    @Inject(
            method = "tryRestock",
            at = @At(value = "INVOKE_ASSIGN", target = "Lorg/joml/Math;clamp(III)I"),
            cancellable = true)
    private void cosmiccore$limitRestock(
                                         CallbackInfo ci,
                                         @Local(ordinal = 2) int promised,
                                         @Local(ordinal = 5) LocalIntRef amountToOrder) {
        if (cosmiccore$promiseLimit < 0 || !FactoryGaugePromiseLimitSupport.supports(cosmiccore$self())) return;
        int amount = Math.min(amountToOrder.get(), cosmiccore$promiseLimit - promised);
        if (amount <= 0) {
            ci.cancel();
            return;
        }
        amountToOrder.set(amount);
    }

    @Inject(
            method = "tickRequests",
            at = @At(value = "INVOKE",
                     target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;resetTimer()V",
                     shift = At.Shift.AFTER),
            cancellable = true)
    private void cosmiccore$limitRecipePromises(CallbackInfo ci) {
        if (cosmiccore$promiseLimit < 0 || !FactoryGaugePromiseLimitSupport.supports(cosmiccore$self())) return;
        int limit = panelBE().restocker ? cosmiccore$promiseLimit :
                FactoryGaugePromiseLimitSupport.effectiveRecipeLimit(cosmiccore$self(), cosmiccore$promiseLimit);
        if (limit <= 0 || getPromised() >= limit) ci.cancel();
    }

    @Inject(method = "writeSafe", at = @At("RETURN"))
    private void cosmiccore$writeSafe(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci) {
        cosmiccore$writeLimit(nbt);
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void cosmiccore$write(
                                  CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket,
                                  CallbackInfo ci) {
        cosmiccore$writeLimit(nbt);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void cosmiccore$read(
                                 CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket,
                                 CallbackInfo ci) {
        if (!active) return;
        CompoundTag panelTag = nbt.getCompound(CreateLang.asId(slot.name()));
        cosmiccore$setPromiseLimit(panelTag.contains(COSMICCORE_PROMISE_LIMIT_KEY, CompoundTag.TAG_INT) ?
                panelTag.getInt(COSMICCORE_PROMISE_LIMIT_KEY) : -1);
    }

    @Unique
    private void cosmiccore$writeLimit(CompoundTag nbt) {
        if (!active || !FactoryGaugePromiseLimitSupport.supports(cosmiccore$self())) return;
        String key = CreateLang.asId(slot.name());
        CompoundTag panelTag = nbt.getCompound(key);
        panelTag.putInt(COSMICCORE_PROMISE_LIMIT_KEY, cosmiccore$promiseLimit);
        nbt.put(key, panelTag);
    }

    @Unique
    private FactoryPanelBehaviour cosmiccore$self() {
        return (FactoryPanelBehaviour) (Object) this;
    }
}
