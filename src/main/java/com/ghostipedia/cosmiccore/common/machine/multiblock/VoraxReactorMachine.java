package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.ghostipedia.cosmiccore.api.machine.multiblock.MagnetWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.electric.MagneticFieldMachine;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VoraxReactorMachine extends WorkableElectricMultiblockMachine {
    @Getter
    private int contagionDelta;
    @Getter
    @Persisted
    private int contagionStrength = 0;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(VoraxReactorMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);


    @Nullable
    protected TickableSubscription contagionSubscription;
    @Nullable
    protected EnergyContainerList outputEnergyContainers;

    public VoraxReactorMachine(IMachineBlockEntity holder) {
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

        List<IEnergyContainer> outputEnergyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.IN);
            if (io == IO.NONE || io == IO.IN) continue;
            var handlers = part.getRecipeHandlers();
            for (var handler : handlers) {
                IO handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.IN) {
                    var containers = handler.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .toList();
                    outputEnergyContainers.addAll(containers);
                    traitSubscriptions.add(handler.subscribe(this::updateContagionSubs));

                }
            }
        }
        this.outputEnergyContainers = new EnergyContainerList(outputEnergyContainers);
        updateContagionSubs();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            updateContagionSubs();
        }
    }
    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.outputEnergyContainers = null;
        contagionStrength = 0;
        updateContagionSubs();
    }

    protected void updateContagionSubs() {
        if ((outputEnergyContainers != null && outputEnergyContainers.getEnergyStored() > 0)) {
            contagionSubscription = subscribeServerTick(contagionSubscription, this::updateContagion);
        } else if (contagionSubscription != null) {
            contagionSubscription.unsubscribe();
            contagionSubscription = null;
        }
    }


    public void updateContagion() {
        if (recipeLogic.isWorking()){
            contagionStrength++;
        }
        if (recipeLogic.isIdle()){

        }
    }


    @Override
    public boolean beforeWorking(@org.jetbrains.annotations.Nullable GTRecipe recipe) {
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        updateContagion();
        return super.onWorking();
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }


}
