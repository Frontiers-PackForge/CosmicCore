package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ItemPipeTransferLedger;
import com.ghostipedia.cosmiccore.common.compat.gtceu.ItemPipeTransferLedgerState;

import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.item.ItemRoutePath;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemPipeBlockEntity.class, remap = false)
public abstract class ItemPipeTransferLedgerFixMixin implements ItemPipeTransferLedgerState {

    @Shadow
    @Final
    private Object2IntOpenHashMap<ItemRoutePath> transferredItems;

    @Shadow
    private long timer;

    @Unique
    private int cosmiccore$transferredItemTotal;

    @Shadow
    public abstract long getLevelTime();

    @Inject(method = "updateTransferredState()V", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$compactExpiredTransferRoutes(CallbackInfo ci) {
        long currentTime = getLevelTime();
        long elapsed = currentTime - timer;
        if (elapsed >= 20 || elapsed < 0) {
            cosmiccore$transferredItemTotal = ItemPipeTransferLedger.decayAndCompact(transferredItems,
                    route -> Math.round(route.getProperties().getTransferRate() * 64));
            timer = currentTime;
        }
        ci.cancel();
    }

    @Inject(method = "getTransferredItemCount()I", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$returnTransferredItemTotal(CallbackInfoReturnable<Integer> cir) {
        ((ItemPipeBlockEntity) (Object) this).getTransferredItems();
        cir.setReturnValue(cosmiccore$transferredItemTotal);
    }

    @Override
    public int cosmiccore$getTransferredItemTotal() {
        return cosmiccore$transferredItemTotal;
    }

    @Override
    public void cosmiccore$addTransferredItems(int amount) {
        cosmiccore$transferredItemTotal = ItemPipeTransferLedger.saturatedAdd(cosmiccore$transferredItemTotal, amount);
    }
}
