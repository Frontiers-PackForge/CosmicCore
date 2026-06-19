package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAModifier;

import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.gregtechceu.gtceu.api.capability.IHPCAComputationProvider;
import com.gregtechceu.gtceu.api.capability.IHPCACoolantProvider;

public class HPCAComponentHatchWrapper extends AbstractHPCAComponentHatchWrapper<IHPCAComponentHatch> {

    public HPCAComponentHatchWrapper(IHPCAComponentHatch component, HPCAModifier columnModifier,
                                     HPCAModifier rowModifier) {
        super(component, columnModifier, rowModifier);
    }

    public HPCACoolantProviderWrapper getHPCACoolantProvider() {
        if (hpcaComponent instanceof IHPCACoolantProvider coolantProvider)
            return new HPCACoolantProviderWrapper(coolantProvider, columnModifier, rowModifier);
        return null;
    }

    public HPCAComputationProviderWrapper getHPCAComputationProvider() {
        if (hpcaComponent instanceof IHPCAComputationProvider computationProvider)
            return new HPCAComputationProviderWrapper(computationProvider, columnModifier, rowModifier);
        return null;
    }

    @Override
    public void setActive(boolean active) {
        active = true;
    }
}
