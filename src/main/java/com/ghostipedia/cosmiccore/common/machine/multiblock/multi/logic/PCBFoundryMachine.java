package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

public class PCBFoundryMachine extends WorkableElectricMultiblockMachine {

    public PCBFoundryMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    // TODO(8.0.0 MUI2): custom UI shelved; default UI used (orig in git)
    // IDisplayUIMachine was REMOVED in 8.0.0 (folded into IMuiMachine, which WorkableElectricMultiblockMachine
    // already implements) and the addDisplayText(List<Component>) hook no longer exists; display is now driven
    // by getWidgetsForDisplay(PanelSyncManager). The old readout showed the parallel count
    // ("cosmic.multiblock.parallel[.exact]", numParallels = getParallelHatch().getCurrentParallel() * 4) plus the
    // standard energy/working/progress/output lines. Re-add via getWidgetsForDisplay + MUI2 when the UI is rebuilt.
}
