package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DimensionalEnergyInterface extends MuiWorkableMultiblockMachine {

    protected static final long ticks_between_save_data_operations = 5L * 20L; // Once per 5s

    protected MaintenanceHatchPartMachine maintenance;
    protected EnergyContainerList inputHatches;
    protected EnergyContainerList outputHatches;
    protected long passiveDrain;

    @SaveField
    protected IEnergyContainer energyBuffer;

    // Stats tracked for UI display
    private long netInLastSec;
    private long netOutLastSec;
    private long averageInLastSec;
    private long averageOutLastSec;
    protected boolean localDisplay;

    protected ConditionalSubscriptionHandler tickSubscription;

    public DimensionalEnergyInterface(BlockEntityCreationInfo info) {
        super(info);
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::transferEnergyTick, this::isActive);
        this.localDisplay = true;
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (getLevel() == null || getLevel().isClientSide) return;

        initializeAbilities();
        setEnergyBuffer();

        tickSubscription.updateSubscription();
    }

    private void initializeAbilities() {
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();

        for (MultiblockPartMachine part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                if (handlerList.getHandlerIO() == IO.IN) {
                    handlerList.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .forEach(inputs::add);
                } else if (handlerList.getHandlerIO() == IO.OUT) {
                    handlerList.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .forEach(outputs::add);
                }
            }
        }

        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);
    }

    protected UUID getTeamUUID() {
        var owner = getOwner();
        var ownerUUID = getOwnerUUID();
        // Faultcheck the Owner and OwnerUUID
        if (owner == null) return MachineOwner.EMPTY;
        if (ownerUUID == null) return MachineOwner.EMPTY;

        var team = owner instanceof FTBOwner ftbOwner ? ftbOwner.getPlayerTeam(ownerUUID) : null;
        if (team == null) return ownerUUID;

        return team.getTeamId();
    }

    @Override
    public void invalidateStructure(String name) {
        if (getLevel() instanceof ServerLevel serverLevel) { // Transfer buffer content to avoid losses
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getTeamUUID();
            if (owner != MachineOwner.EMPTY) {
                if (energyBuffer != null) {
                    data.addEUToGlobalWirelessEnergy(owner, energyBuffer.getEnergyStored());
                    energyBuffer.removeEnergy(energyBuffer.getEnergyStored());
                }
                data.removeEnergyBuffered(owner, getBlockPos());
                data.removeEnergyInput(owner, getBlockPos());
                data.removeEnergyOutput(owner, getBlockPos());
                data.removePassiveDrain(owner, getBlockPos());
            }
            this.inputHatches = null;
            this.outputHatches = null;
            this.energyBuffer = null;
            this.passiveDrain = 0;
            this.netInLastSec = 0;
            this.averageInLastSec = 0;
            this.netOutLastSec = 0;
            this.averageOutLastSec = 0;
        }

        tickSubscription.unsubscribe();
        super.invalidateStructure(name);
    }

    public boolean isActive() {
        return isFormed();
    }

    private void setEnergyBuffer() {
        long totalIOPerTick = (inputHatches.getInputVoltage() + outputHatches.getOutputVoltage());
        // Size is the totalIOPerTick over the duration between operations doubled
        long bufferSize = totalIOPerTick *
                (ticks_between_save_data_operations + (ticks_between_save_data_operations / 2L)) * 2L;
        bufferSize += (getPassiveDrainPerTick() * 8 * 2) * ticks_between_save_data_operations;
        if (bufferSize < 0L)
            throw new RuntimeException("DimensionalEnergyCapacitor: Calculated buffer size is too big.");
        // 8.0.0: NotifiableEnergyContainer ctor no longer takes the machine; attach as a trait
        // (mirrors the old `new NotifiableEnergyContainer(this, ...)` which registered it on construction).
        this.energyBuffer = attachTrait(new NotifiableEnergyContainer(bufferSize, Long.MAX_VALUE, Long.MAX_VALUE,
                Long.MAX_VALUE, Long.MAX_VALUE));
    }

    public long getPassiveDrainPerTick() {
        return 20_000L; // 0 in the interfaces, Overridden in the Capacitor
    }

    public long getPassiveDrain() {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (maintenance == null) {
                for (MultiblockPartMachine part : getParts()) {
                    if (part instanceof MaintenanceHatchPartMachine maintenanceMachine) {
                        this.maintenance = maintenanceMachine;
                        break;
                    }
                }
            }
            if (maintenance == null) return getPassiveDrainPerTick();
            int multiplier = 1 + maintenance.getNumMaintenanceProblems();
            double modifier = maintenance.getDurationMultiplier();
            return (long) (getPassiveDrainPerTick() * multiplier * modifier);
        }
        return getPassiveDrainPerTick();
    }

    protected void transferEnergyTick() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            var data = WirelessEnergySavedData.getOrCreate(serverLevel);
            var owner = getTeamUUID();

            if (isWorkingEnabled() && isFormed() && owner != MachineOwner.EMPTY) {
                if (getOffsetTimer() % 20 == 0) {
                    getRecipeLogic().setStatus((energyBuffer != null && energyBuffer.getEnergyStored() > 0) ?
                            RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);

                    averageInLastSec = netInLastSec / 20;
                    averageOutLastSec = netOutLastSec / 20;
                    netInLastSec = 0;
                    netOutLastSec = 0;

                    // Send IO values to global Storage to display in the Dimensional Storage.
                    data.setEnergyInput(owner, getBlockPos(), averageInLastSec);
                    data.setEnergyOutput(owner, getBlockPos(), averageOutLastSec);
                    data.setEnergyBuffered(owner, getBlockPos(), energyBuffer.getEnergyStored());
                }

                // Handle inputs
                long energyBuffered = energyBuffer.addEnergy(inputHatches.getEnergyStored());
                inputHatches.changeEnergy(-energyBuffered);
                netInLastSec += energyBuffered;

                // Passive Drain
                long energyPassiveDrained = energyBuffer.removeEnergy(getPassiveDrain());
                netOutLastSec += energyPassiveDrained;

                // Handle outputs
                long energyNeed = outputHatches.getEnergyCapacity() - outputHatches.getEnergyStored();
                long energyDeBuffered = energyBuffer.removeEnergy(energyNeed);
                outputHatches.changeEnergy(energyDeBuffered);
                netOutLastSec += energyDeBuffered;

                // Handle buffer transfer to WirelessEnergySavedData
                if (getOffsetTimer() % ticks_between_save_data_operations == 0) {
                    if (data.isActive(owner)) {
                        // After operation buffer should aim to be 50% full
                        var euToTransfer = energyBuffer.getEnergyStored() - (energyBuffer.getEnergyCapacity() / 2);
                        var euTransferred = data.addEUToGlobalWirelessEnergy(owner, euToTransfer);
                        energyBuffer.changeEnergy(-(euToTransfer - euTransferred));
                        data.setEnergyBuffered(owner, getBlockPos(), energyBuffer.getEnergyStored());
                        data.setPassiveDrain(owner, getBlockPos(), getPassiveDrain());
                    }
                }
            } else {
                data.removeEnergyBuffered(owner, getBlockPos());
                data.removeEnergyInput(owner, getBlockPos());
                data.removeEnergyOutput(owner, getBlockPos());
                data.removePassiveDrain(owner, getBlockPos());
            }
        }
    }
}
