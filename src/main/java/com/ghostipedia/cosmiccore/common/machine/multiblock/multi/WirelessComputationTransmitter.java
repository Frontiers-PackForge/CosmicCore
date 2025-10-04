package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.wireless.WirelessCwuStore;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class WirelessComputationTransmitter extends WorkableElectricMultiblockMachine
    implements IFancyUIMachine, IDisplayUIMachine, IControllable, IOpticalComputationProvider {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.LuV];

    private final WirelessComputationHandler computationHandler = new WirelessComputationHandler(this);

    private IMaintenanceMachine maintenance;
    private IEnergyContainer energyContainer;

    @Getter
    private int energyUsage = 0;

    @Nullable
    private TickableSubscription tickSubscription;

    protected UUID getTeamUUID() {
        var team = ((FTBOwner) getOwner()).getPlayerTeam(getOwnerUUID());
        return team != null ? team.getTeamId() : getOwnerUUID();
    }

    public WirelessComputationTransmitter(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof DummyWorld) return;

        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);

        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (part instanceof IMaintenanceMachine maintenanceMachine)
                this.maintenance = maintenanceMachine;
            if (io == IO.NONE || io == IO.OUT) continue;
            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(io)) continue;
                handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .forEach(energyContainers::add);
            }
        }

        this.energyContainer = new EnergyContainerList(energyContainers);
        this.energyUsage = calculateEnergyUsage();

        if (this.maintenance == null) {
            onStructureInvalid();
            return;
        }

        List<IOpticalComputationHatch> receivers = new ArrayList<>();
        for (var part : getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (!PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) continue;
            if (part instanceof IOpticalComputationHatch hatch) receivers.add(hatch);
            else {
                var handlerLists = part.getRecipeHandlers();
                for (var handlerList : handlerLists) {
                    handlerList.getCapability(CWURecipeCapability.CAP).stream()
                            .filter(IOpticalComputationHatch.class::isInstance)
                            .map(IOpticalComputationHatch.class::cast)
                            .forEach(receivers::add);
                }
            }
        }

        computationHandler.onStructureFormed(receivers);
        WirelessCwuStore.setWirelessSource(getTeamUUID(), this);

        if (getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
    }

    protected int calculateEnergyUsage() {
        int receivers = 0;
        int transmitters = 0;
        for (var part : this.getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) {
                ++receivers;
            }
            if (PartAbility.COMPUTATION_DATA_TRANSMISSION.isApplicable(block)) {
                ++transmitters;
            }
        }
        return EUT_PER_HATCH * (receivers + transmitters);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        WirelessCwuStore.resetWirelessSource(getTeamUUID());
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.energyUsage = 0;
        this.computationHandler.reset();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel)
            serverLevel.getServer().tell(new TickTask(0, this::updateTickSubscription));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() && !getRecipeLogic().isWaiting() ? computationHandler.requestCWUt(cwut, simulate, seen) : 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isFormed() ? computationHandler.getMaxCWUt(seen) : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    private void updateTickSubscription() {
        if (isFormed()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tick() {
        int energyToConsume = this.getEnergyUsage();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        if (hasMaintenance) energyToConsume += this.maintenance.getNumMaintenanceProblems() * energyToConsume / 10;

        if (getRecipeLogic().isWaiting() && energyContainer.getInputPerSec() > 19L * energyToConsume) {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!getRecipeLogic().isWaiting()) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == energyToConsume) getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
                else getRecipeLogic().setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in")
                        .append(": ").append(EURecipeCapability.CAP.getName()));
            }
        } else getRecipeLogic().setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in")
                .append(": ").append(EURecipeCapability.CAP.getName()));

        updateTickSubscription();
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(getEnergyUsage())
                .addComputationUsageLine(computationHandler.getMaxCWUtForDisplay())
                .addWorkingStatusLine();
    }

    private static class WirelessComputationHandler extends NotifiableComputationContainer {

        private final Set<IOpticalComputationHatch> providers = new ObjectOpenHashSet<>();

        public WirelessComputationHandler(MetaMachine machine) {
            super(machine, IO.IN, false);
        }

        public void onStructureFormed(Collection<IOpticalComputationHatch> providers) {
            reset();
            this.providers.addAll(providers);
        }

        private void reset() {
            providers.clear();
        }

        @Override
        public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            seen.add(this);

            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int allocatedCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                int allocated = provider.requestCWUt(cwut, simulate, seen);
                allocatedCWUt += allocated;
                cwut -= allocated;
                if (cwut == 0) break;
            }
            return allocatedCWUt;
        }

        public int getMaxCWUtForDisplay() {
            Collection<IOpticalComputationProvider> seen = new ArrayList<>();
            seen.add(this);

            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                maximumCWUt += provider.getMaxCWUt(seen);
            }
            return maximumCWUt;
        }

        @Override
        public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            seen.add(this);

            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                maximumCWUt += provider.getMaxCWUt(seen);
            }
            return maximumCWUt;
        }

        @Override
        public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return false;
            seen.add(this);
            for (var provider : providers) {
                if (provider.canBridge(seen)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public IOpticalComputationProvider getComputationProvider() {
            return this;
        }
    }
}
