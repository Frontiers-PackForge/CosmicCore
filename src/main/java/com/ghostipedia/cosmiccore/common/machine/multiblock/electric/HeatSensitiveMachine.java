package com.ghostipedia.cosmiccore.common.machine.multiblock.electric;

import com.ghostipedia.cosmiccore.api.capability.recipe.HeatRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;
import com.ghostipedia.cosmiccore.api.machine.multiblock.HeatWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HeatSensitiveMachine extends HeatWorkableElectricMultiblockMachine implements ITieredMachine {

    @Getter
    private long overHeatLimit;
    @Getter
    private long freezeLimit;
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @Nullable
    protected TickableSubscription preHeatSubs;

    public HeatSensitiveMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IHeatContainer> heatContainers = new ArrayList<>();
        for (MultiblockPartMachine part : getParts()) {
            for (var handler : part.getRecipeHandlers()) {
                IO handlerIO = handler.getHandlerIO();
                if (handler.hasCapability(HeatRecipeCapability.CAP) &&
                        handler instanceof IHeatContainer container) {
                    heatContainers.add(container);
                }
                if (handler.hasCapability(EURecipeCapability.CAP) &&
                        handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }

            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
    }
}
