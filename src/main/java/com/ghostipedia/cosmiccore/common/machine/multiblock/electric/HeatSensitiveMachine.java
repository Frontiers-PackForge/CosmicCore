package com.ghostipedia.cosmiccore.common.machine.multiblock.electric;

import com.ghostipedia.cosmiccore.api.capability.recipe.HeatRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.recipe.IHeatContainer;
import com.ghostipedia.cosmiccore.api.machine.multiblock.HeatWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.MagnetWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class HeatSensitiveMachine extends HeatWorkableElectricMultiblockMachine implements ITieredMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MagneticFieldMachine.class,
            MagnetWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);
    @Getter
    private long overHeatLimit;
    @Getter
    private long freezeLimit;
    @Nullable
    protected EnergyContainerList inputEnergyContainers;
    @Nullable
    protected TickableSubscription preHeatSubs;

    public HeatSensitiveMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        List<IHeatContainer> heatContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.IN);
            if (io == IO.NONE || io == IO.OUT) continue;
            for (var handler : part.getRecipeHandlers()) {
                IO handlerIO = handler.getHandlerIO();
                if (handler.getCapability() == HeatRecipeCapability.CAP &&
                        handler instanceof IHeatContainer container) {
                    heatContainers.add(container);
                }
                if (handler.getCapability() == EURecipeCapability.CAP &&
                        handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }

            }
        }
        this.inputEnergyContainers = new EnergyContainerList(energyContainers);
    }
}
