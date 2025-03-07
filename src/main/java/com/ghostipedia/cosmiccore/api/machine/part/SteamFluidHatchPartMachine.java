package com.ghostipedia.cosmiccore.api.machine.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;

public class SteamFluidHatchPartMachine extends FluidHatchPartMachine {

    public SteamFluidHatchPartMachine(IMachineBlockEntity holder, IO io, long initialCapacity, int slots,
                                      Object... args) {
        super(holder, 1, io, 2000, 1, args);
    }
}
