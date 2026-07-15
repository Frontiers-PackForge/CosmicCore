package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.misc.IOFilteredInvWrapper;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IOFilteredInvWrapper.class, remap = false)
public abstract class MachineInputSlotExtractFixMixin extends CombinedInvWrapper {

    private MachineInputSlotExtractFixMixin() {
        super();
        throw new AssertionError();
    }

    @Inject(method = "extractItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void cosmiccore$denyInputSlotExtraction(int slot, int amount, boolean simulate,
                                                    CallbackInfoReturnable<ItemStack> cir) {
        IItemHandlerModifiable handler = getHandlerFromIndex(getIndexForSlot(slot));
        if (handler instanceof NotifiableItemStackHandler notifiable &&
                !notifiable.getHandlerIO().support(IO.OUT)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
