package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.gregtechceu.gtceu.integration.ae2.gui.AEConfigSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import net.minecraft.network.RegistryFriendlyByteBuf;

import appeng.api.stacks.GenericStack;
import brachy.modularui.value.sync.SyncHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Objects;

@Mixin(value = AEConfigSyncHandler.class, remap = false)
public abstract class AEConfigPacketSnapshotFixMixin extends SyncHandler<AEConfigSyncHandler> {

    @Shadow
    @Final
    private IConfigurableSlotList slotList;
    @Shadow
    @Final
    private int slotCount;
    @Shadow
    @Final
    private GenericStack[] cachedConfig;
    @Shadow
    @Final
    private GenericStack[] cachedStock;

    @Shadow
    private static void writeStack(RegistryFriendlyByteBuf buf, GenericStack stack) {}

    @Inject(method = "detectAndSendChanges", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$captureSlotChangesBeforeEncoding(boolean init, CallbackInfo ci) {
        ci.cancel();
        boolean[] changed = null;
        for (int i = 0; i < slotCount; i++) {
            var slot = slotList.getConfigurableSlot(i);
            GenericStack config = slot.getConfig();
            GenericStack stock = slot.getStock();
            if (init || !Objects.equals(config, cachedConfig[i]) || !Objects.equals(stock, cachedStock[i])) {
                if (changed == null) changed = new boolean[slotCount];
                changed[i] = true;
                cachedConfig[i] = config;
                cachedStock[i] = stock;
            }
        }
        if (changed == null) return;
        boolean[] changedSlots = changed;
        GenericStack[] configs = Arrays.copyOf(cachedConfig, slotCount);
        GenericStack[] stocks = Arrays.copyOf(cachedStock, slotCount);
        syncToClient(1, buf -> {
            for (int i = 0; i < changedSlots.length; i++) {
                buf.writeBoolean(changedSlots[i]);
                if (changedSlots[i]) {
                    writeStack(buf, configs[i]);
                    writeStack(buf, stocks[i]);
                }
            }
        });
    }
}
