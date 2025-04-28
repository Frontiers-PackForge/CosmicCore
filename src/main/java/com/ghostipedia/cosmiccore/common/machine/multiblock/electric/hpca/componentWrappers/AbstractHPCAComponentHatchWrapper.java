package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.componentWrappers;

import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.hpca.HPCAModifier;
import com.gregtechceu.gtceu.api.capability.IHPCAComponentHatch;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public abstract class AbstractHPCAComponentHatchWrapper<T extends IHPCAComponentHatch> implements IHPCAComponentHatch {

    protected final HPCAModifier columnModifier;
    protected final HPCAModifier rowModifier;
    protected final T hpcaComponent;

    public AbstractHPCAComponentHatchWrapper(T component, HPCAModifier columnModifier, HPCAModifier rowModifier) {
        this.hpcaComponent = component;
        this.columnModifier = columnModifier;
        this.rowModifier = rowModifier;
    }

    @Override
    public int getMaxEUt() {
        return hpcaComponent.getMaxEUt();
    }

    @Override
    public boolean isDamaged() {
        return hpcaComponent.isDamaged();
    }

    @Override
    public void setDamaged(boolean damaged) {
        hpcaComponent.setDamaged(damaged);
    }

    @Override
    public int getUpkeepEUt() {
        return 0;
    }

    @Override
    public boolean canBeDamaged() {
        return hpcaComponent.canBeDamaged();
    }

    @Override
    public boolean isBridge() {
        return hpcaComponent.isBridge();
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return hpcaComponent.getComponentIcon();
    }
}
