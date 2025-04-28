package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAModifier;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;

public class HPCAComputationProviderWrapper extends AbstractHPCAComponentHatchWrapper<IHPCAComputationProvider>
        implements IHPCAComputationProvider {

    public HPCAComputationProviderWrapper(IHPCAComputationProvider component, HPCAModifier columnModifier, HPCAModifier rowModifier) {
        super(component, columnModifier, rowModifier);
    }

    @Override
    public int getCWUPerTick() {
        double cwuAmount = this.hpcaComponent.getCWUPerTick();

        // handle column modifier
        if (this.columnModifier == HPCAModifier.YELLOW) cwuAmount *= 1.5;

        // handle row modifier
        if (this.rowModifier == HPCAModifier.YELLOW) cwuAmount *= 1.5;

        return (int) Math.floor(cwuAmount);
    }

    @Override
    public int getCoolingPerTick() {
        double coolingAmount = this.hpcaComponent.getCoolingPerTick();

        // handle column modifier
        if (this.columnModifier == HPCAModifier.RED) coolingAmount *= 2;
        else if (this.columnModifier == HPCAModifier.YELLOW) coolingAmount *= 2;

        // handle row modifier
        if (this.rowModifier == HPCAModifier.RED) coolingAmount *= 2;
        else if (this.columnModifier == HPCAModifier.YELLOW) coolingAmount *= 2;

        return (int) Math.floor(coolingAmount);
    }
}
