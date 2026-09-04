package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ItemPipeTransferLedger;
import com.ghostipedia.cosmiccore.common.compat.gtceu.ItemPipeTransferLedgerState;

import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.item.ItemNetHandler;
import com.gregtechceu.gtceu.common.pipelike.item.ItemRoutePath;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = ItemNetHandler.class, remap = false)
public abstract class ItemNetHandlerTransferAccountingFixMixin {

    @Shadow
    @Final
    private Object2IntOpenHashMap<ItemRoutePath> simulatedTransfers;

    @Shadow
    @Final
    private ItemPipeBlockEntity pipe;

    @Unique
    private int cosmiccore$simulatedTransferTotal;

    @WrapOperation(
                   method = "insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
                   at = @At(
                            value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;putAll(Ljava/util/Map;)V"),
                   require = 2,
                   expect = 2,
                   allow = 2)
    private void cosmiccore$replaceSimulatedTransferSnapshot(Object2IntOpenHashMap<?> target, Map<?, ?> source,
                                                             Operation<Void> original) {
        if (target == simulatedTransfers) {
            cosmiccore$simulatedTransferTotal = ((ItemPipeTransferLedgerState) pipe)
                    .cosmiccore$getTransferredItemTotal();
        } else {
            original.call(target, source);
        }
    }

    @Inject(method = "getTotalSimulatedTransfers()I", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$returnSimulatedTransferTotal(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cosmiccore$simulatedTransferTotal);
    }

    @WrapOperation(
                   method = "transfer(Lcom/gregtechceu/gtceu/common/pipelike/item/ItemRoutePath;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)V",
                   at = @At(
                            value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;addTo(Ljava/lang/Object;I)I"),
                   require = 2,
                   expect = 2,
                   allow = 2)
    private int cosmiccore$accumulateSimulatedTransfer(Object2IntOpenHashMap<?> target, Object route, int amount,
                                                       Operation<Integer> original) {
        if (target == simulatedTransfers) {
            int previous = cosmiccore$simulatedTransferTotal;
            cosmiccore$simulatedTransferTotal = ItemPipeTransferLedger.saturatedAdd(previous, amount);
            return previous;
        }
        int previous = original.call(target, route, amount);
        ((ItemPipeTransferLedgerState) pipe).cosmiccore$addTransferredItems(amount);
        return previous;
    }

    @Inject(method = "decrementBy(I)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void cosmiccore$compactRoundRobinHistory(int amount, CallbackInfo ci) {
        ItemPipeTransferLedger.subtractAndCompact(pipe.getTransferredGlobalRoundRobin(), amount);
        ci.cancel();
    }
}
