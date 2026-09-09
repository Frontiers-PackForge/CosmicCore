package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEStockingUIRefresh;

import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { MEStockingBusPartMachine.class, MEStockingHatchPartMachine.class }, remap = false)
public abstract class MEStockingUIRefreshMixin implements MEStockingUIRefresh {

    @Unique
    private boolean cosmiccore$refreshQueued;

    @Shadow(remap = false)
    protected abstract void syncME();

    @Shadow(remap = false)
    private void refreshList() {}

    @Override
    public void cosmiccore$requestStockRefresh() {
        var part = (IMEStockingPart) this;
        if (part.self().isRemote() || cosmiccore$refreshQueued) return;
        cosmiccore$refreshQueued = true;
        part.self().scheduleForNextServerTick(() -> {
            cosmiccore$refreshQueued = false;
            var gridMachine = (IGridConnectedMachine) this;
            if (gridMachine.getMainNode().getGrid() == null || !gridMachine.isOnline()) return;
            if (part.isAutoPull()) refreshList();
            syncME();
        });
    }

    @Inject(method = { "setMinStackSize", "setTicksPerCycle" }, at = @At("TAIL"))
    private void cosmiccore$refreshChangedSettings(int amount, CallbackInfo ci) {
        cosmiccore$requestStockRefresh();
    }
}
