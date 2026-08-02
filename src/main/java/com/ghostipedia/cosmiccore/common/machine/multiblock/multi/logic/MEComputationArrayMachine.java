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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
    private long committedCwut;
    private long currentEuPerTick;
    private long currentRelayEuPerTick;
    private boolean[] onlineCores = new boolean[0];
    private long standbyCoreEuThisTick;
    private long paidCoreEuThisTick;
    private long powerCycleTick = Long.MIN_VALUE;
    private long activeGridLeaseTick = Long.MIN_VALUE;
    @Nullable
    private UUID activeGridLeaseId;
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
        onlineCores = new boolean[computationCores.size()];
        if (uplink != null) {
            uplink.clampRelayPowerToCapacity();
        }
        scheduleForNextServerTick(this::updateTickSubscription);
    }

    @Override
    public void invalidateStructure(String name) {
        setCommittedCwut(0);
        setCurrentConsumption(0, 0);
        computationCores.clear();
        powerRelays.clear();
        onlineCores = new boolean[0];
        standbyCoreEuThisTick = 0;
        paidCoreEuThisTick = 0;
        powerCycleTick = Long.MIN_VALUE;
        activeGridLeaseTick = Long.MIN_VALUE;
        activeGridLeaseId = null;
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
        setCommittedCwut(0);
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

    public long getCommittedCwut() {
        return committedCwut;
    }

    public long getMaximumCwut() {
        return computationCores.stream().mapToLong(MEComputationComponentPartMachine::cwutPerTick).sum();
    }

    public long getInstalledCwut() {
        return isFormed() && isWorkingEnabled() && energyContainer != null && uplink != null ? getMaximumCwut() : 0;
    }

    public long getOnlineCwut() {
        long onlineCwut = 0;
        for (int index = 0; index < computationCores.size(); index++) {
            if (onlineCores[index]) {
                onlineCwut += computationCores.get(index).cwutPerTick();
            }
        }
        return onlineCwut;
    }

    public long getEuDemandPerTick() {
        return isFormed() && isWorkingEnabled() ? currentEuPerTick : 0;
    }

    public long getCurrentRelayEuPerTick() {
        return currentRelayEuPerTick;
    }

    public long getMaximumRelayEuPerTick() {
        return powerRelays.stream().mapToLong(MEComputationComponentPartMachine::euPerTick).sum();
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
        return (double) getMaximumRelayEuPerTick() * MEComputationArrayTuning.UPLINK_BUFFER_TICKS;
    }

    public boolean isUplinkOnline() {
        return uplink != null && uplink.isGridOnline();
    }

    public long commitCwut(UUID gridLeaseId, long serverTick, long targetTotalCwut) {
        if (!isFormed() || !isWorkingEnabled() || energyContainer == null || uplink == null) {
            return 0;
        }
        if (serverTick != getCurrentServerTick()) {
            return 0;
        }
        ensurePowerCycle(serverTick);
        if (activeGridLeaseTick == serverTick && !gridLeaseId.equals(activeGridLeaseId)) {
            return 0;
        }
        if (activeGridLeaseTick != serverTick && targetTotalCwut > 0) {
            activeGridLeaseTick = serverTick;
            activeGridLeaseId = gridLeaseId;
        } else if (activeGridLeaseTick != serverTick) {
            return 0;
        }
        long requestedCwut = Math.min(Math.max(0, targetTotalCwut), getMaximumCwut());
        long targetCwut = Math.min(requestedCwut, getOnlineCwut());
        if (targetCwut <= committedCwut) {
            if (requestedCwut > committedCwut) {
                getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
            }
            return committedCwut;
        }
        long targetCoreEu = Math.max(
                standbyCoreEuThisTick,
                ceilMultiplyDivide(
                        targetCwut,
                        MEComputationArrayTuning.CORE_EU_RATIO_NUMERATOR,
                        MEComputationArrayTuning.CORE_EU_RATIO_DENOMINATOR));
        long euToConsume = Math.min(
                Math.max(0, targetCoreEu - paidCoreEuThisTick),
                energyContainer.getEnergyStored());
        long additionalEu = euToConsume <= 0 ? 0 : energyContainer.removeEnergy(euToConsume);
        paidCoreEuThisTick += additionalEu;
        long fundedCwut = Math.min(
                targetCwut,
                paidCoreEuThisTick * MEComputationArrayTuning.CORE_EU_RATIO_DENOMINATOR /
                        MEComputationArrayTuning.CORE_EU_RATIO_NUMERATOR);
        setCommittedCwut(Math.max(committedCwut, fundedCwut));
        setCurrentConsumption(currentEuPerTick + additionalEu, currentRelayEuPerTick);
        if (committedCwut < requestedCwut) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        } else if (additionalEu > 0) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        }
        return committedCwut;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        LongSyncValue currentCwut = new LongSyncValue(this::getCommittedCwut);
        LongSyncValue maximumCwut = new LongSyncValue(this::getMaximumCwut);
        LongSyncValue euDemand = new LongSyncValue(this::getEuDemandPerTick);
        LongSyncValue relayEut = new LongSyncValue(this::getCurrentRelayEuPerTick);
        LongSyncValue maximumRelayEut = new LongSyncValue(this::getMaximumRelayEuPerTick);
        IntSyncValue cores = new IntSyncValue(this::getCoreCount);
        IntSyncValue relays = new IntSyncValue(this::getRelayCount);
        DoubleSyncValue storedPowerEu = new DoubleSyncValue(this::getStoredPowerEu);
        DoubleSyncValue maximumStoredPowerEu = new DoubleSyncValue(this::getMaximumStoredPowerEu);
        syncManager.syncValue("me_computation_current_cwut", currentCwut);
        syncManager.syncValue("me_computation_maximum_cwut", maximumCwut);
        syncManager.syncValue("me_computation_eu_demand", euDemand);
        syncManager.syncValue("me_computation_relay_eut", relayEut);
        syncManager.syncValue("me_computation_maximum_relay_eut", maximumRelayEut);
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
                coloredValue(FormattingUtil.formatNumbers(maximumRelayEut.getLongValue()), ChatFormatting.GOLD))));
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
        long serverTick = getCurrentServerTick();
        if (serverTick != Long.MIN_VALUE) {
            ensurePowerCycle(serverTick);
        }
    }

    private long getCurrentServerTick() {
        return getLevel() == null || getLevel().getServer() == null ?
                Long.MIN_VALUE : getLevel().getServer().getTickCount();
    }

    private void ensurePowerCycle(long serverTick) {
        if (powerCycleTick == serverTick) {
            return;
        }
        resetPowerCycle(serverTick);
        if (!isWorkingEnabled()) {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
            return;
        }
        if (energyContainer == null || uplink == null) {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
            return;
        }
        long relayConsumption = fillUplinkBuffer();
        long standbyConsumption = powerCoreStandby();
        long consumed = relayConsumption + standbyConsumption;
        setCurrentConsumption(consumed, relayConsumption);
        if (consumed > 0) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        } else if (hasUnmetDemand()) {
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        } else {
            getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
        }
    }

    private void resetPowerCycle(long serverTick) {
        setCommittedCwut(0);
        setCurrentConsumption(0, 0);
        Arrays.fill(onlineCores, false);
        standbyCoreEuThisTick = 0;
        paidCoreEuThisTick = 0;
        powerCycleTick = serverTick;
        activeGridLeaseTick = Long.MIN_VALUE;
        activeGridLeaseId = null;
        setActiveComponents(computationCores, false);
        setActiveComponents(powerRelays, false);
    }

    private long powerCoreStandby() {
        long consumedEu = 0;
        for (int index = 0; index < computationCores.size(); index++) {
            MEComputationComponentPartMachine core = computationCores.get(index);
            long standbyEu = core.standbyEuPerTick();
            if (energyContainer.getEnergyStored() < standbyEu) {
                continue;
            }
            long consumed = energyContainer.removeEnergy(standbyEu);
            if (consumed != standbyEu) {
                continue;
            }
            onlineCores[index] = true;
            core.setActive(true);
            consumedEu += consumed;
        }
        standbyCoreEuThisTick = consumedEu;
        paidCoreEuThisTick = consumedEu;
        return consumedEu;
    }

    private long fillUplinkBuffer() {
        long euToConsume = Math.min(getRelayDemandEuPerTick(), energyContainer.getEnergyStored());
        if (euToConsume <= 0) {
            return 0;
        }
        long consumed = energyContainer.removeEnergy(euToConsume);
        uplink.acceptRelayPower(MEComputationArrayTuning.euToAe(consumed));
        setRelayActivity(consumed);
        return consumed;
    }

    private long getRelayDemandEuPerTick() {
        if (uplink == null) {
            return 0;
        }
        long euForDemand = MEComputationArrayTuning.aeToEuFloor(uplink.getRelayPowerDemandAe());
        return Math.min(euForDemand, getMaximumRelayEuPerTick());
    }

    private boolean hasUnmetDemand() {
        return getOnlineCwut() < getMaximumCwut() ||
                !powerRelays.isEmpty() && uplink.getRelayPowerDemandAe() > 0;
    }

    private void setCurrentConsumption(long currentEuPerTick, long currentRelayEuPerTick) {
        this.currentEuPerTick = currentEuPerTick;
        this.currentRelayEuPerTick = currentRelayEuPerTick;
    }

    private void setRelayActivity(long relayEu) {
        long remainingEu = relayEu;
        for (MEComputationComponentPartMachine relay : powerRelays) {
            relay.setActive(remainingEu > 0);
            remainingEu = Math.max(0, remainingEu - relay.euPerTick());
        }
    }

    private static long ceilMultiplyDivide(long value, long multiplier, long divisor) {
        if (value <= 0 || multiplier <= 0) {
            return 0;
        }
        return Math.ceilDiv(Math.multiplyExact(value, multiplier), divisor);
    }

    private static void setActiveComponents(List<MEComputationComponentPartMachine> components, boolean active) {
        for (MEComputationComponentPartMachine component : components) {
            component.setActive(active);
        }
    }

    private void setCommittedCwut(long committedCwut) {
        if (this.committedCwut == committedCwut) {
            return;
        }
        this.committedCwut = committedCwut;
        getSyncDataHolder().markClientSyncFieldDirty("committedCwut");
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
