package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.common.armor.FoundryModuleItemHandler;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.misc.EquipmentFoundryMachine;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EquipmentFoundryMachine.class, remap = false)
public abstract class EquipmentFoundryOxygenViewMixin {

    @Shadow
    @Final
    private CustomItemStackHandler equipmentSlot;
    @Shadow
    @Final
    @Mutable
    private CustomItemStackHandler moduleSlots;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void cosmiccore$liveOxygenModuleView(CallbackInfo ci) {
        moduleSlots = new FoundryModuleItemHandler(moduleSlots, () -> equipmentSlot.getStackInSlot(0));
    }
}
