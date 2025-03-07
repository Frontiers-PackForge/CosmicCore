package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.block.IMultiblockProvider;
import com.ghostipedia.cosmiccore.api.block.IMultiblockReciever;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public abstract class ModularizedWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine
                                                                   implements IMultiblockReciever {

    @Getter
    protected final int tier;
    @Getter
    protected final int moduleTier;
    @Getter
    protected final int minModuleTier;
    @Nullable
    @Getter
    @Setter
    private IMultiblockProvider multiBlockProvider;
    protected final long energyConsumption;

    @Getter
    private IEnergyContainer energyStorageContainer;

    protected boolean isActive;

    public ModularizedWorkableElectricMultiblockMachine(IMachineBlockEntity holder, int tier, int moduleTier,
                                                        int minModuleTier) {
        super(holder);
        this.tier = tier;
        this.moduleTier = moduleTier;
        this.minModuleTier = minModuleTier;
        this.energyConsumption = (long) (Math.pow(4, this.tier + 2) / 2.0);
        this.energyStorageContainer = new NotifiableEnergyContainer(this,
                (long) (160008000L * Math.pow(4, this.tier - 9)), this.energyConsumption, 1, 1, 1);
    }

    @Override
    public boolean checkPattern() {
        if (getModularMultiBlock() != null) {
            if (getModularMultiBlock().getModulatorTier() >= minModuleTier) {
                super.checkPatternWithLock();
            }
        }
        return super.checkPatternWithLock();
    }

    @Override
    public boolean isActive() {
        return this.isActive && isWorkingEnabled();
    }

    @Override
    public IMultiblockProvider getModularMultiBlock() {
        return this.multiBlockProvider;
    }

    public void setModularMultiBlock(IMultiblockProvider provider) {
        this.multiBlockProvider = provider;
    }
}
