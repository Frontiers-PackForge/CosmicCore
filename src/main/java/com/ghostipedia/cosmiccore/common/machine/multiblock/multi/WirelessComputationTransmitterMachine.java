package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.wireless.WirelessComputationStore;
import com.ghostipedia.cosmiccore.utils.OwnershipUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Wireless Computation Transmitter multiblock.
 * Aggregates computation from receiver hatches and makes it available wirelessly to the team's network.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessComputationTransmitterMachine extends WorkableElectricMultiblockMachine
        implements IFancyUIMachine, IDisplayUIMachine, IControllable {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.LuV];

    private IMaintenanceMachine maintenance;
    private IEnergyContainer energyContainer;
    private List<IOpticalComputationHatch> computationHatches = new ArrayList<>();
    private boolean hatchesRegistered = false;

    private final ConditionalSubscriptionHandler tickSubscription;

    public WirelessComputationTransmitterMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.tickSubscription = new ConditionalSubscriptionHandler(this, this::tick, this::isSubscriptionActive);
    }

    protected UUID getTeamUUID() {
        var owner = getOwner();
        if (owner == null) {
            return getOwnerUUID();
        }
        if (owner instanceof FTBOwner ftbOwner) {
            var team = ftbOwner.getPlayerTeam(getOwnerUUID());
            return team != null ? team.getTeamId() : getOwnerUUID();
        }
        return getOwnerUUID();
    }

    protected boolean isSubscriptionActive() {
        return isFormed();
    }

    private void tick() {
        if (isWorkingEnabled() && isFormed()) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
            energyContainer.removeEnergy(calculateEnergyUsage());

            // Only register hatches when state changes to working
            if (!hatchesRegistered) {
                addHatchesToWirelessNetwork();
                hatchesRegistered = true;
            }
        } else {
            // Only unregister hatches when state changes to not working
            if (hatchesRegistered) {
                removeHatchesFromWirelessNetwork();
                hatchesRegistered = false;
            }
            getRecipeLogic().setStatus(RecipeLogic.Status.SUSPEND);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;

        List<IEnergyContainer> energyContainers = new ArrayList<>();
        computationHatches = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);

        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);

            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                this.maintenance = maintenanceMachine;
            }

            if (io == IO.NONE || io == IO.OUT) continue;

            // Collect energy containers
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                if (io != IO.BOTH && handlerIO != IO.BOTH && io != handlerIO) continue;
                if (handler.hasCapability(EURecipeCapability.CAP) &&
                        handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                }
            }

            // Collect computation hatches (receivers only)
            Block block = part.self().getBlockState().getBlock();
            if (PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) {
                if (part instanceof IOpticalComputationHatch hatch) {
                    computationHatches.add(hatch);
                } else {
                    var handlerLists = part.getRecipeHandlers();
                    for (var handlerList : handlerLists) {
                        for (var cwu : handlerList.getCapability(CWURecipeCapability.CAP)) {
                            if (cwu instanceof IOpticalComputationHatch hatch) {
                                computationHatches.add(hatch);
                            }
                        }
                    }
                }
            }
        }

        if (this.maintenance == null) {
            onStructureInvalid();
            return;
        }

        this.energyContainer = new EnergyContainerList(new ArrayList<>(energyContainers));
        this.hatchesRegistered = false;

        tickSubscription.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        if (hatchesRegistered) {
            removeHatchesFromWirelessNetwork();
            hatchesRegistered = false;
        }

        super.onStructureInvalid();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.computationHatches = new ArrayList<>();
        getRecipeLogic().setStatus(RecipeLogic.Status.SUSPEND);
        tickSubscription.unsubscribe();
    }

    private int calculateEnergyUsage() {
        int receivers = computationHatches.size();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        var maintenanceMultiplier = hasMaintenance ? (1 + ((float) this.maintenance.getNumMaintenanceProblems() / 10)) : 1;
        return (int) Math.floor(receivers * maintenanceMultiplier * EUT_PER_HATCH);
    }

    private void addHatchesToWirelessNetwork() {
        var uuid = getTeamUUID();
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info("Adding {} computation hatches to wireless network for team {}", computationHatches.size(), uuid);
        WirelessComputationStore.addHatches(uuid, computationHatches);
        var store = WirelessComputationStore.getStore(uuid);
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info("Store now has {} providers, max CWU: {}", store.getProviderCount(), store.getMaxCWUt());
    }

    private void removeHatchesFromWirelessNetwork() {
        WirelessComputationStore.removeHatches(getTeamUUID(), computationHatches);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        var store = WirelessComputationStore.getStore(getTeamUUID());
        int allocatedCWU = store.getAllocatedCWUt();
        int maxCWU = store.getMaxCWUt();
        int totalProviders = store.getProviderCount();

        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled())
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "cosmiccore.multiblock.wireless_computation_transmitter.transmitting")
                .addEnergyUsageExactLine(calculateEnergyUsage())
                .addCustom(list -> {
                    list.add(Component.translatable("cosmiccore.multiblock.wireless_computation_transmitter.network_providers", totalProviders));
                    list.add(Component.translatable("cosmiccore.multiblock.wireless_computation_transmitter.cwu_usage", allocatedCWU, maxCWU));
                })
                .addWorkingStatusLine()
                .addEmptyLine()
                .addCustom(list -> OwnershipUtils.addOwnerLine(list, getOwner(), true));
    }
}
