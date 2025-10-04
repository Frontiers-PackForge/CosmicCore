package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;

public class TemporaryResearchStationMachine extends ResearchStationMachine {
    public TemporaryResearchStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    @Override
    public ResearchStationRecipeLogic getRecipeLogic() {
        return super.getRecipeLogic();
    }
}
