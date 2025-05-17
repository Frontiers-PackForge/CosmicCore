package com.ghostipedia.cosmiccore.common.machine.multiblock.electric.modular;

import com.ghostipedia.cosmiccore.api.machine.multiblock.modular.WorkableElectricMultiblockMachineModule;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

public class ModuleTest extends WorkableElectricMultiblockMachineModule {

    protected ConditionalSubscriptionHandler tickSubscription;

    public ModuleTest(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::tick, this::isSubscriptionActive);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;
        tickSubscription.updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        notifyBases();
    }

    public boolean isSubscriptionActive() {
        if (!isFormed()) return false;

        for (var base : getBaseMultiBlocks()) {
            if (!base.isWorking()) return false;
        }

        return true;
    }

    public void tick() {
        if (isWorkingEnabled()) {
            getRecipeLogic()
                    .setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
        }
        if (isWorkingEnabled()) {
            energyContainer.removeEnergy(32);
        }
        tickSubscription.updateSubscription();
    }

    @Override
    public void onBaseUpdate() {
        super.onBaseUpdate();
        tickSubscription.updateSubscription();
        getRecipeLogic().setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
    }
}
