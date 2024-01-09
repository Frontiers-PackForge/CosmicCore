package com.ghostipedia.cosmiccore.gtbridge.machines.logic;

import com.ghostipedia.cosmiccore.gtbridge.machines.BasicAirMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.lowdragmc.lowdraglib.misc.FluidTransferList;
import lombok.Getter;

import javax.annotation.Nullable;




public class BasicAirMachineLogic extends RecipeLogic {


    public BasicAirMachineLogic(BasicAirMachine machine) {
        super(machine);
    }

    @Override
    public BasicAirMachine getMachine() {
        return (BasicAirMachine)super.getMachine();
    }
}

