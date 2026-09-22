package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.ghostipedia.cosmiccore.common.compat.gtceu.ae2.MEPatternBufferCapacity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine$1", remap = false)
public abstract class MEPatternBufferTerminalInventoryMixin {

    @ModifyConstant(method = "size", constant = @Constant(intValue = 27))
    private int cosmiccore$expandTerminalPatternCapacity(int original) {
        return MEPatternBufferCapacity.SLOTS;
    }
}
