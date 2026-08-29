package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.sync_system.ClassSyncData;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.NBTSerializableTransformer;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PowerSubstationMachine.class, remap = false)
public class PowerSubstationEnergyBankSerializationFixMixin {

    @Inject(method = "<init>(Lcom/gregtechceu/gtceu/api/blockentity/BlockEntityCreationInfo;)V", at = @At("TAIL"))
    private void cosmiccore$selectEnergyBankSerializer(BlockEntityCreationInfo info, CallbackInfo ci) {
        ClassSyncData.getClassData(PowerSubstationMachine.class)
                .setCustomTransformerForField("energyBank", new NBTSerializableTransformer());
    }
}
