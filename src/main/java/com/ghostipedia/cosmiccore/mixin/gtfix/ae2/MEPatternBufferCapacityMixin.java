package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEPatternBufferCapacity;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEPatternBufferPartMachine.class, remap = false)
public abstract class MEPatternBufferCapacityMixin {

    @Shadow
    @Final
    @Mutable
    protected MEPatternBufferPartMachine.InternalSlot[] internalInventory;

    @ModifyConstant(method = { "<init>", "syncWorkerCount", "addWorker", "buildMainUI", "getAvailablePatterns" },
                    constant = @Constant(intValue = 27))
    private int cosmiccore$expandPatternCapacity(int original) {
        return MEPatternBufferCapacity.SLOTS;
    }

    @ModifyConstant(method = "buildMainUI", constant = @Constant(intValue = 54))
    private int cosmiccore$expandPatternGridHeight(int original) {
        return MEPatternBufferCapacity.ROWS * 18;
    }

    @Inject(method = "onLoad", at = @At("HEAD"))
    private void cosmiccore$expandSavedInternalSlots(CallbackInfo ci) {
        MEPatternBufferPartMachine machine = (MEPatternBufferPartMachine) (Object) this;
        internalInventory = MEPatternBufferCapacity.expandSavedSlots(internalInventory,
                MEPatternBufferPartMachine.InternalSlot[]::new, () -> machine.new InternalSlot());
    }
}
