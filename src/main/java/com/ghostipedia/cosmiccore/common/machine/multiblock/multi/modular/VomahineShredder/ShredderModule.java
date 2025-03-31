package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

public class ShredderModule extends WorkableElectricMultiblockMachine {

    @Getter
    @Setter
    private ShredderMultiblock shredderMultiblock;
    private int index;
    @Getter
    private int value;

    public ShredderModule(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }


    @Override
    public EnergyContainerList getEnergyContainer() {
        if (shredderMultiblock != null && shredderMultiblock.isFormed()) {
            return shredderMultiblock.getEnergyContainer();
        }
        return new EnergyContainerList(new ArrayList<>());
    }


    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        value = getMultiblockState().getMatchContext().get("CoilType") instanceof ICoilType coil ?
                coil.getCoilTemperature() : -1;
        if(value == -1) {
            onStructureInvalid();
        }
    }
}
