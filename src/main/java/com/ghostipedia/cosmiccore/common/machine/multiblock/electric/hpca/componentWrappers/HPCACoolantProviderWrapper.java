package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAModifier;

import com.gregtechceu.gtceu.api.capability.IHPCACoolantProvider;

public class HPCACoolantProviderWrapper extends AbstractHPCAComponentHatchWrapper<IHPCACoolantProvider>
                                        implements IHPCACoolantProvider {

    public HPCACoolantProviderWrapper(IHPCACoolantProvider component, HPCAModifier columnModifier,
                                      HPCAModifier rowModifier) {
        super(component, columnModifier, rowModifier);
    }

    @Override
    public int getCoolingAmount() {
        double coolingAmount = this.hpcaComponent.getCoolingAmount();

        // handle column modifier
        if (this.columnModifier == HPCAModifier.RED) coolingAmount *= 0.8;
        else if (this.columnModifier == HPCAModifier.GREEN) coolingAmount *= 2;

        // handle row modifier
        if (this.rowModifier == HPCAModifier.RED) coolingAmount *= 0.8;
        else if (this.rowModifier == HPCAModifier.GREEN) coolingAmount *= 2;

        return (int) Math.floor(coolingAmount);
    }

    @Override
    public boolean isActiveCooler() {
        return this.hpcaComponent.isActiveCooler();
    }

    @Override
    public int getMaxCoolantPerTick() {
        double maxCoolant = this.hpcaComponent.getMaxCoolantPerTick();

        // handle column modifier
        if (this.columnModifier == HPCAModifier.GREEN) maxCoolant *= 1.5;

        // handle row modifier
        if (this.rowModifier == HPCAModifier.GREEN) maxCoolant *= 1.5;

        return (int) Math.floor(maxCoolant);
    }
}
