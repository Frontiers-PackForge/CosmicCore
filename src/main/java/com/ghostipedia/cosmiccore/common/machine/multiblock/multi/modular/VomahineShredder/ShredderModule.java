package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.modular.VomahineShredder;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import lombok.Getter;
import lombok.Setter;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

public class ShredderModule extends WorkableMultiblockMachine {

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
    public void onStructureFormed() {
        super.onStructureFormed();
        value = getMultiblockState().getMatchContext().get("CoilType") instanceof ICoilType coil ?
                coil.getCoilTemperature() : -1;
        if(value == -1) {
            onStructureInvalid();
        }
    }
}
