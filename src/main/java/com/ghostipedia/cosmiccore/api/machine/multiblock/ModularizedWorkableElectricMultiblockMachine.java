package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.block.IMultiblockProvider;
import com.ghostipedia.cosmiccore.api.block.IMultiblockReciever;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

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

    public ModularizedWorkableElectricMultiblockMachine(BlockEntityCreationInfo info, int tier, int moduleTier,
                                                        int minModuleTier) {
        super(info);
        this.tier = tier;
        this.moduleTier = moduleTier;
        this.minModuleTier = minModuleTier;
        this.energyConsumption = (long) (Math.pow(4, this.tier + 2) / 2.0);
        // 8.0.0: NotifiableEnergyContainer ctor dropped the machine arg (5 longs: capacity, inV, inA, outV, outA);
        // attach it as a trait so it binds to this machine.
        this.energyStorageContainer = attachTrait(new NotifiableEnergyContainer(
                (long) (160008000L * Math.pow(4, this.tier - 9)), this.energyConsumption, 1, 1, 1));
    }

    // 8.0.0: checkPattern()/checkPatternWithLock() were removed; the structure-check entry point is now
    // checkStructurePattern(String) (driven by checkAndFormStructure()). Gate formation on the modular
    // multiblock providing at least the minimum modulator tier; otherwise skip the pattern check so the
    // structure does not form (returns the current, non-valid PatternState).
    @Override
    public PatternState checkStructurePattern(String structureName) {
        if (getModularMultiBlock() == null ||
                getModularMultiBlock().getModulatorTier() < minModuleTier) {
            return getPatternState(structureName);
        }
        return super.checkStructurePattern(structureName);
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
