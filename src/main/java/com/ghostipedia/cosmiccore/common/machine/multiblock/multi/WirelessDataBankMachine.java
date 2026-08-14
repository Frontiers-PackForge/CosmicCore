package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.wireless.WirelessDataStore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.level.block.Block;

import java.util.*;

// TODO(8.0.0 MUI2): custom display text shelved; base default getWidgetsForDisplay UI used for now (original
// energy-usage / working-status / owner display text in git history).
public class WirelessDataBankMachine extends WorkableElectricMultiblockMachine
                                     implements IControllable {

    public static final int EUT_PER_HATCH_CHAINED = GTValues.VA[GTValues.LuV];

    private MaintenanceHatchPartMachine maintenance;
    private IEnergyContainer energyContainer;

    private final ConditionalSubscriptionHandler tickSubscription;

    protected UUID getTeamUUID() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    public WirelessDataBankMachine(BlockEntityCreationInfo info) {
        super(info);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::tick, this::isSubscriptionActive);
    }

    protected boolean isSubscriptionActive() {
        return isFormed();
    }

    private void tick() {
        if (isWorkingEnabled() && isFormed()) {
            getRecipeLogic()
                    .setStatus(isSubscriptionActive() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.SUSPEND);
            energyContainer.removeEnergy(calculateEnergyUsage());
            addHatchesToWirelessNetwork();
        } else {
            removeHatchesFromWirelessNetwork();
        }
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);
        if (getLevel() == null || getLevel().isClientSide) return;

        List<IEnergyContainer> energyContainers = new ArrayList<>();
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MaintenanceHatchPartMachine maintenanceMachine)
                this.maintenance = maintenanceMachine;
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.OUT) continue;
                if (handler.hasCapability(EURecipeCapability.CAP) &&
                        handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }
        }

        if (this.maintenance == null) {
            invalidateStructure(substructureName);
            return;
        }

        this.energyContainer = new EnergyContainerList(new ArrayList<>(energyContainers));

        tickSubscription.updateSubscription();
    }

    private int calculateEnergyUsage() {
        int receivers = getOpticalHatches().size();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        var maintenanceMultiplier = hasMaintenance ? (1 + ((float) this.maintenance.getNumMaintenanceProblems() / 10)) :
                1;
        return (int) Math.floor(receivers * maintenanceMultiplier * EUT_PER_HATCH_CHAINED);
    }

    @Override
    public void invalidateStructure(String name) {
        if (isWorkingEnabled() && getRecipeLogic().getStatus() == RecipeLogic.Status.WORKING)
            removeHatchesFromWirelessNetwork();
        super.invalidateStructure(name);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        getRecipeLogic().setStatus(RecipeLogic.Status.SUSPEND);
        tickSubscription.unsubscribe();
    }

    private void addHatchesToWirelessNetwork() {
        WirelessDataStore.addHatches(getTeamUUID(), getOpticalHatches());
    }

    private void removeHatchesFromWirelessNetwork() {
        WirelessDataStore.removeHatches(getTeamUUID(), getOpticalHatches());
    }

    private List<IDataAccessHatch> getOpticalHatches() {
        List<IDataAccessHatch> hatches = new ArrayList<>();

        for (var part : getParts()) {
            Block block = part.getBlockState().getBlock();
            if (part instanceof IDataAccessHatch hatch && PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                hatches.add(hatch);
            }
        }

        return hatches;
    }
}
