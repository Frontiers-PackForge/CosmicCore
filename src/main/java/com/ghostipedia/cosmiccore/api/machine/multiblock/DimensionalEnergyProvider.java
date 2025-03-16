package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import net.minecraft.server.level.ServerLevel;


public class DimensionalEnergyProvider extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DimensionalEnergyProvider.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final long BASE_POWER_CONSUMPTION = GTValues.VA[GTValues.LuV];

    @Persisted
    private boolean isDuplicate = false; // Stops multiblock from working if one already exist in this dimension
    private IMaintenanceMachine maintenance;

    protected ConditionalSubscriptionHandler tickSubscription;

    public DimensionalEnergyProvider(IMachineBlockEntity holder) {
        super(holder);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::tick, this::isSubscriptionActive);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;

        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getHolder().getOwner().getUUID();
            var dimension = getLevel().dimension().location().toString();
            this.isDuplicate = data.isWirelessActive(owner, dimension); // If already enable should disable the multiblock
        }

        for (IMultiPart part : getParts()) {
            if (part instanceof IMaintenanceMachine maintenanceMachine)
                this.maintenance = maintenanceMachine;
        }

        if (this.maintenance == null) {
            onStructureInvalid();
            return;
        }

        tickSubscription.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getHolder().getOwner().getUUID();
            var dimension = getLevel().dimension().location().toString();
            data.removeWirelessDimensions(owner, dimension);
        }
    }

    @Override
    public void onPartUnload() {
        super.onUnload();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getHolder().getOwner().getUUID();
            var dimension = getLevel().dimension().location().toString();
            data.removeWirelessDimensions(owner, dimension);
        }
    }

    private boolean isSubscriptionActive() {
        if (!isFormed()) return false;
        if (isDuplicate) return false;
        if (energyContainer == null || energyContainer.getEnergyStored() <= 0) return false;
        return energyContainer.getEnergyStored() > calculateEnergyUsage();
    }

    private int calculateEnergyUsage() {
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        var maintenanceMultiplier = hasMaintenance ? (1 + ((float) this.maintenance.getNumMaintenanceProblems() / 10)): 1;
        return (int) Math.floor(BASE_POWER_CONSUMPTION * maintenanceMultiplier);
    }

    public void tick() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            if (isWorkingEnabled() && isFormed()) {
                getRecipeLogic().setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
                energyContainer.removeEnergy(calculateEnergyUsage());
                if (getOffsetTimer() % 100 == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getHolder().getOwner().getUUID();
                    var dimension = getLevel().dimension().location().toString();
                    data.addWirelessDimensions(owner, dimension);
                }
            } else {
                if (getOffsetTimer() % 100 == 0) {
                    var data = WirelessEnergySavedData.getOrCreate(serverLevel);
                    var owner = getHolder().getOwner().getUUID();
                    var dimension = getLevel().dimension().location().toString();
                    data.removeWirelessDimensions(owner, dimension);
                }
            }
        }
    }
}
