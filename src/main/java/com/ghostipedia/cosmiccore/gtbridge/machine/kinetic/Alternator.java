package com.ghostipedia.cosmiccore.gtbridge.machine.kinetic;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import com.ghostipedia.cosmiccore.gtbridge.machine.logic.MachineCasingType;
import com.ghostipedia.cosmiccore.gtbridge.machine.logic.feature.IAlternatorProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Alternator extends CoilWorkableElectricMultiblockMachine implements IAlternatorProvider {
    private MachineCasingType machineCasingType;
    @Setter
    private int tier = this.getMachineCasingTier();

    public Alternator(IMachineBlockEntity holder) {
        super(holder);
    }

    //////////////////////////////////////
    //***    Multiblock LifeCycle    ***//
    //////////////////////////////////////
    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getMultiblockState().getMatchContext().get("MachineCasing") instanceof MachineCasingType machineCasing) {
            this.machineCasingType = machineCasing;
        }
    }

    public int getMachineCasingTier() {
        if (this.machineCasingType != null) {
            return this.machineCasingType.getTier();
        }
        return 0;
    }


    public String getEnergyHatchLevel() {
        return this.machineCasingType.getEnergyHatchLevel();
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public Set<Alternator> getTypes() {
        return this.machineCasingType == null ? Set.of() : Set.of(this);
    }
}
