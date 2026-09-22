package com.ghostipedia.cosmiccore.mixin.gtfix.ae2;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTAEMachines.class, remap = false)
public abstract class MEInterfaceTierMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void cosmiccore$useHVTier(CallbackInfo ci) {
        for (var definition : new MachineDefinition[] {
                GTAEMachines.ITEM_IMPORT_BUS_ME, GTAEMachines.FLUID_IMPORT_HATCH_ME,
                GTAEMachines.ITEM_EXPORT_BUS_ME, GTAEMachines.FLUID_EXPORT_HATCH_ME,
                GTAEMachines.STOCKING_IMPORT_BUS_ME, GTAEMachines.STOCKING_IMPORT_HATCH_ME,
                GTAEMachines.ME_PATTERN_BUFFER, GTAEMachines.ME_PATTERN_BUFFER_PROXY }) {
            definition.setTier(GTValues.HV);
        }
    }
}
