package com.ghostipedia.cosmiccore.mixin.accessor;


import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorkableElectricMultiblockMachine.class)
public interface WorkableElectricMultiblockMachineMixin {

    @Accessor("tier")
    void cosCore$setOverclockTier(int tier);

    @Accessor("energyContainer")
    void cosCore$setEnergyContainer(EnergyContainerList list);

}