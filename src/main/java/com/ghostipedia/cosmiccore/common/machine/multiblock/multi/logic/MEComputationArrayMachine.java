package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.MEComputationComponentPartMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.MEComputationUplinkPartMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.bloom.BloomRenderTicket;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public final class MEComputationArrayMachine extends WorkableElectricMultiblockMachine {

    private final List<MEComputationComponentPartMachine> computationCores = new ArrayList<>(
            MEComputationArrayTuning.COMPONENT_POSITIONS);
    private final List<MEComputationComponentPartMachine> powerRelays = new ArrayList<>(
            MEComputationArrayTuning.COMPONENT_POSITIONS);
    @Nullable
    private MEComputationUplinkPartMachine uplink;
    @Nullable
    private TickableSubscription tickSubscription;
    @SyncToClient
    private long availableCwut;
    private long currentEuPerTick;
    private long currentRelayEuPerTick;
    private BloomRenderTicket registeredBloomTicket = BloomRenderTicket.INVALID;

    public MEComputationArrayMachine(BlockEntityCreationInfo info) {
        super(info, new ComputationArrayRecipeLogic());
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        computationCores.clear();
        powerRelays.clear();
        uplink = null;
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MEComputationComponentPartMachine component) {
                if (component.role() == MEComputationComponentPartMachine.Role.COMPUTATION_CORE) {
                    computationCores.add(component);
                } else {
                    powerRelays.add(component);
                }
            } else if (part instanceof MEComputationUplinkPartMachine computationUplink) {
                uplink = computationUplink;
            }
        }
        Comparator<MultiblockPartMachine> componentOrder = Comparator.comparingInt(
                RelativeDirection.UP.getMultiSorter(getFrontFacing(), getUpwardsFacing(), isFlipped()));
        computationCores.sort(componentOrder);
        powerRelays.sort(componentOrder);
        scheduleForNextServerTick(this::updateTickSubscription);
    }

    @Override
    public void invalidateStructure(String name) {
        setAvailableCwut(0);
        setCurrentConsumption(0, 0);
        computationCores.clear();
        powerRelays.clear();
        uplink = null;
        super.invalidateStructure(name);
        updateTickSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scheduleForNextServerTick(this::updateTickSubscription);
    }

    @Override
    public void onUnload() {
        setAvailableCwut(0);
        setCurrentConsumption(0, 0);
        if (registeredBloomTicket.isValid()) {
            registeredBloomTicket.invalidate();
            registeredBloomTicket = BloomRenderTicket.INVALID;
        }
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        super.onUnload();
    }

    public long getAvailableCwut() {
        return availableCwut;
    }

    public long getMaximumCwut() {
        return (long) computationCores.size() * MEComputationArrayTuning.CORE_CWU_PER_TICK;
    }

    public long getEuDemandPerTick() {
        if (!isFormed() || !isWorkingEnabled()) {
            return 0;
        }
        return (long) computationCores.size() * MEComputationArrayTuning.CORE_EU_PER_TICK +
                getRelayDemandEuPerTick();
    }

    public long getCurrentRelayEuPerTick() {
        return currentRelayEuPerTick;
    }

    public int getCoreCount() {
        return computationCores.size();
    }

    public int getRelayCount() {
        return powerRelays.size();
    }

    public BloomRenderTicket getRegisteredBloomTicket() {
        return registeredBloomTicket;
    }

    public void setRegisteredBloomTicket(BloomRenderTicket registeredBloomTicket) {
        this.registeredBloomTicket = registeredBloomTicket;
    }

    public double getStoredPowerEu() {
        return uplink == null ? 0 : MEComputationArrayTuning.aeToEu(uplink.getAECurrentPower());
    }

    public double getMaximumStoredPowerEu() {
        return MEComputationArrayTuning.aeToEu(MEComputationArrayTuning.uplinkBufferCapacityAe());
    }

    public boolean isUplinkOnline() {
        return uplink != null && uplink.isGridOnline();
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        LongSyncValue currentCwut = new LongSyncValue(this::getAvailableCwut);
        LongSyncValue maximumCwut = new LongSyncValue(this::getMaximumCwut);
        LongSyncValue euDemand = new LongSyncValue(this::getEuDemandPerTick);
        LongSyncValue relayEut = new LongSyncValue(this::getCurrentRelayEuPerTick);
        IntSyncValue cores = new IntSyncValue(this::getCoreCount);
        IntSyncValue relays = new IntSyncValue(this::getRelayCount);
        DoubleSyncValue storedPowerEu = new DoubleSyncValue(this::getStoredPowerEu);
        DoubleSyncValue maximumStoredPowerEu = new DoubleSyncValue(this::getMaximumStoredPowerEu);
        syncManager.syncValue("me_computation_current_cwut", currentCwut);
        syncManager.syncValue("me_computation_maximum_cwut", maximumCwut);
        syncManager.syncValue("me_computation_eu_demand", euDemand);
        syncManager.syncValue("me_computation_relay_eut", relayEut);
        syncManager.syncValue("me_computation_cores", cores);
        syncManager.syncValue("me_computation_relays", relays);
        syncManager.syncValue("me_computation_stored_power_eu", storedPowerEu);
        syncManager.syncValue("me_computation_maximum_stored_power_eu", maximumStoredPowerEu);
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        widgets.add(telemetryLine(() -> Component.translatable(
                "cosmiccore.machine.me_computation_array.display.components",
                coloredValue(cores.getIntValue(), ChatFormatting.AQUA),
                coloredValue(relays.getIntValue(), ChatFormatting.GOLD))));
        widgets.add(telemetryLine(() -> Component.translatable(
                "cosmiccore.machine.me_computation_array.display.cwu",
                coloredValue(FormattingUtil.formatNumbers(currentCwut.getLongValue()), ChatFormatting.GREEN),
                coloredValue(FormattingUtil.formatNumbers(maximumCwut.getLongValue()), ChatFormatting.AQUA))));
        widgets.add(telemetryLine(() -> Component.translatable(
                "cosmiccore.machine.me_computation_array.display.energy",
                coloredValue(FormattingUtil.formatNumbers(euDemand.getLongValue()), ChatFormatting.YELLOW))));
        widgets.add(telemetryLine(() -> Component.translatable(
                "cosmiccore.machine.me_computation_array.display.relay",
                coloredValue(FormattingUtil.formatNumbers(relayEut.getLongValue()), ChatFormatting.YELLOW),
                coloredValue(
                        FormattingUtil.formatNumbers(
                                (long) relays.getIntValue() * MEComputationArrayTuning.RELAY_EU_PER_TICK),
                        ChatFormatting.GOLD))));
        widgets.add(telemetryLine(() -> Component.translatable(
                "cosmiccore.machine.me_computation_array.display.stored_power",
                coloredValue(FormattingUtil.formatNumber2Places(storedPowerEu.getDoubleValue()), ChatFormatting.AQUA),
                coloredValue(FormattingUtil.formatNumber2Places(maximumStoredPowerEu.getDoubleValue()),
                        ChatFormatting.DARK_AQUA))));
        return widgets;
    }

    private void updateTickSubscription() {
        if (isFormed()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tickComputationArray);
        } else if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tickComputationArray() {
        if (!isWorkingEnabled()) {
            setAvailableCwut(0);
            setCurrentConsumption(0, 0);
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
            return;
        }
        if (energyContainer == null || uplink == null) {
            setAvailableCwut(0);
            setCurrentConsumption(0, 0);
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
            return;
        }
        long relayConsumption = fillUplinkBuffer();
        long coreConsumption = powerComputationCores();
        long consumed = relayConsumption + coreConsumption;
        setCurrentConsumption(consumed, relayConsumption);
        if (consumed > 0) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        } else if (hasUnmetDemand()) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        } else {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
        }
    }

    private long powerComputationCores() {
        long affordableCores = Math.min(computationCores.size(),
                energyContainer.getEnergyStored() / MEComputationArrayTuning.CORE_EU_PER_TICK);
        long euToConsume = affordableCores * MEComputationArrayTuning.CORE_EU_PER_TICK;
        if (euToConsume <= 0) {
            setAvailableCwut(0);
            return 0;
        }
        long consumed = energyContainer.removeEnergy(euToConsume);
        long poweredCores = consumed / MEComputationArrayTuning.CORE_EU_PER_TICK;
        setAvailableCwut(poweredCores * MEComputationArrayTuning.CORE_CWU_PER_TICK);
        return consumed;
    }

    private long fillUplinkBuffer() {
        long euToConsume = Math.min(getRelayDemandEuPerTick(), energyContainer.getEnergyStored());
        if (euToConsume <= 0) {
            return 0;
        }
        long consumed = energyContainer.removeEnergy(euToConsume);
        uplink.acceptRelayPower(MEComputationArrayTuning.euToAe(consumed));
        return consumed;
    }

    private long getRelayDemandEuPerTick() {
        if (uplink == null) {
            return 0;
        }
        long euForDemand = MEComputationArrayTuning.aeToEuFloor(uplink.getRelayPowerDemandAe());
        long relayLimit = (long) powerRelays.size() * MEComputationArrayTuning.RELAY_EU_PER_TICK;
        return Math.min(euForDemand, relayLimit);
    }

    private boolean hasUnmetDemand() {
        return !computationCores.isEmpty() || !powerRelays.isEmpty() && uplink.getRelayPowerDemandAe() > 0;
    }

    private void setCurrentConsumption(long currentEuPerTick, long currentRelayEuPerTick) {
        this.currentEuPerTick = currentEuPerTick;
        this.currentRelayEuPerTick = currentRelayEuPerTick;
        int activeCores = (int) ((currentEuPerTick - currentRelayEuPerTick) /
                MEComputationArrayTuning.CORE_EU_PER_TICK);
        int activeRelays = (int) Math.ceilDiv(currentRelayEuPerTick,
                MEComputationArrayTuning.RELAY_EU_PER_TICK);
        setActiveComponents(computationCores, activeCores);
        setActiveComponents(powerRelays, activeRelays);
    }

    private static void setActiveComponents(List<MEComputationComponentPartMachine> components, int activeCount) {
        for (int index = 0; index < components.size(); index++) {
            components.get(index).setActive(index < activeCount);
        }
    }

    private void setAvailableCwut(long availableCwut) {
        if (this.availableCwut == availableCwut) {
            return;
        }
        this.availableCwut = availableCwut;
        getSyncDataHolder().markClientSyncFieldDirty("availableCwut");
    }

    private static Component coloredValue(Object value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }

    private static IWidget telemetryLine(Supplier<Component> componentSupplier) {
        return Text.dynamic(componentSupplier).asWidget().color(Color.WHITE.main).scale(0.8f);
    }

    private static final class ComputationArrayRecipeLogic extends RecipeLogic {

        @Override
        public void updateTickSubscription() {}
    }
}
