package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ItemBusPartMachine.class, remap = false)
public abstract class ItemBusUiStorageFixMixin {

    @ModifyArg(
               method = "lambda$buildMainUI$1",
               at = @At(
                        value = "INVOKE",
                        target = "Lbrachy/modularui/value/sync/SyncHandlers;itemSlot(Lnet/neoforged/neoforge/items/IItemHandlerModifiable;I)Lbrachy/modularui/widgets/slot/ModularSlot;",
                        remap = false),
               index = 0,
               require = 1,
               remap = false)
    private IItemHandlerModifiable cosmiccore$useInternalStorage(IItemHandlerModifiable handler) {
        if (handler instanceof NotifiableItemStackHandler inventory) return inventory.storage;
        return handler;
    }
}
