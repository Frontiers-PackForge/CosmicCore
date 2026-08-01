package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BloomwyrmHeartMachine extends LinkedWorkableElectricMultiblockMachine {

    public static final int MAX_PARTNERS = 32;
    public static final int MAX_LINK_DISTANCE = 64;
    public static final double MAX_LINK_DISTANCE_SQUARED = MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;
    public static final int CYCLE_DURATION_TICKS = 1_200;
    public static final int BOOTSTRAP_BIOPOWER = 16;
    public static final long CHARGE_CAPACITY = 1_000_000L;

    @SaveField
    private long storedCharge;
    @SaveField
    private int cycleTicksRemaining = CYCLE_DURATION_TICKS;
    @SaveField
    private boolean allocationBatchActive;
    @SaveField
    private int allocatedBiopowerCapacity;
    @SaveField
    private long[] allocationBatchParticipants = new long[0];
    @SaveField
    private int lastLimitedUnits;

    private long evaluatedPowerTick = Long.MIN_VALUE;
    private boolean poweredForEvaluatedTick;
    private long lastChunkTicketSync = Long.MIN_VALUE;
    private final Set<Long> forcedChildChunks = new HashSet<>();

    public BloomwyrmHeartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public LinkRole getLinkRole() {
        return LinkRole.CONTROLLER;
    }

    @Override
    public int getMaxPartners() {
        return MAX_PARTNERS;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        GlobalPos own = getGlobalPos();
        return own != null &&
                partnerMachine instanceof BloomwyrmUnitMachine &&
                own.dimension().equals(partner.dimension()) &&
                own.pos().distSqr(partner.pos()) <= MAX_LINK_DISTANCE_SQUARED;
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscribeServerTick(this::tickCampus);
        }
    }

    public long getStoredCharge() {
        return storedCharge;
    }

    public long getChargeCapacity() {
        return CHARGE_CAPACITY;
    }

    public int getBiopowerCapacity() {
        return saturatingAdd(BOOTSTRAP_BIOPOWER, allocatedBiopowerCapacity);
    }

    public int getAllocatedBiopower() {
        int total = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            total = saturatingAdd(total, unit.getAllocatedBiopower());
        }
        return total;
    }

    public long getAllocatedEUt() {
        long total = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            if (unit.isFormed()) {
                total = saturatingAdd(total, unit.getAllocatedEUt());
            }
        }
        return total;
    }

    public int getActiveUnitCount() {
        int active = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            if (unit.hasAllocation()) {
                active++;
            }
        }
        return active;
    }

    public int getLoadedUnitCount() {
        return getLoadedUnits().size();
    }

    public int getLastLimitedUnits() {
        return lastLimitedUnits;
    }

    public int getCycleTicksRemaining() {
        return Math.max(0, cycleTicksRemaining);
    }

    public int getCycleSecondsRemaining() {
        return (getCycleTicksRemaining() + 19) / 20;
    }

    public boolean isCycleBlockedByActiveBatch() {
        return allocationBatchActive && getCycleTicksRemaining() == 0;
    }

    public long acceptCharge(long amount) {
        if (amount <= 0) return 0;
        long accepted = Math.min(amount, CHARGE_CAPACITY - storedCharge);
        storedCharge += accepted;
        return accepted;
    }

    public boolean ensureCampusPowerForCurrentTick() {
        if (!(getLevel() instanceof ServerLevel level) || !isFormed()) {
            return false;
        }
        long gameTime = level.getGameTime();
        if (evaluatedPowerTick == gameTime) {
            return poweredForEvaluatedTick;
        }
        evaluatedPowerTick = gameTime;
        List<BloomwyrmUnitMachine> units = getLoadedUnits();
        for (BloomwyrmUnitMachine unit : units) {
            if (unit.hasAllocation() && !unit.isFormed()) {
                poweredForEvaluatedTick = false;
                return false;
            }
        }
        long demand = 0;
        for (BloomwyrmUnitMachine unit : units) {
            if (unit.hasAllocation()) {
                demand = saturatingAdd(demand, unit.getAllocatedEUt());
            }
        }
        if (demand <= 0) {
            poweredForEvaluatedTick = true;
            return true;
        }
        EnergyContainerList energy = getCampusEnergyContainer();
        if (energy == null || energy.getEnergyStored() < demand) {
            poweredForEvaluatedTick = false;
            return false;
        }
        poweredForEvaluatedTick = energy.changeEnergy(-demand) == -demand;
        return poweredForEvaluatedTick;
    }

    private void tickCampus() {
        syncChildChunkTickets();
        if (!isFormed()) {
            return;
        }
        if (cycleTicksRemaining > 0) {
            cycleTicksRemaining--;
        }
        int activeUnits = getActiveUnitCount();
        if (allocationBatchActive && allocationBatchParticipants.length == 0 && activeUnits > 0) {
            allocationBatchParticipants = getLoadedUnits().stream()
                    .filter(BloomwyrmUnitMachine::hasAllocation)
                    .mapToLong(unit -> unit.getBlockPos().asLong())
                    .toArray();
        }
        if (allocationBatchActive && activeUnits == 0 && !hasPendingBatchAllocations()) {
            allocationBatchActive = false;
            allocatedBiopowerCapacity = 0;
            allocationBatchParticipants = new long[0];
            evaluatedPowerTick = Long.MIN_VALUE;
        }
        if (!allocationBatchActive) {
            if (activeUnits > 0) {
                allocationBatchActive = true;
                allocatedBiopowerCapacity = getCurrentBiopowerOutput();
            } else if (cycleTicksRemaining == 0) {
                allocateBatch();
                cycleTicksRemaining = CYCLE_DURATION_TICKS;
            }
        }
    }

    private void allocateBatch() {
        List<BloomwyrmUnitMachine> units = getLoadedUnits();
        List<UnitRequest> requests = new ArrayList<>();
        for (BloomwyrmUnitMachine unit : units) {
            if (!unit.isAvailableForAllocation()) {
                continue;
            }
            var request = unit.getRecipeLogic().createRequest();
            if (request.isPresent()) {
                requests.add(new UnitRequest(unit, request.get()));
            } else {
                unit.recordHeartOffer(0, BloomwyrmAllocationConstraint.NO_RECIPE);
                unit.denyAllocation(BloomwyrmAllocationConstraint.NO_RECIPE);
            }
        }
        requests.sort(Comparator.comparing(
                request -> request.request().biopowerOutputPerParallel() <= 0));

        long remainingEU = getCampusEnergyBudget();
        long inputVoltage = getCampusInputVoltage();
        int remainingBiopower = BOOTSTRAP_BIOPOWER;
        int producedBiopower = 0;
        long remainingCharge = storedCharge;
        long reservedChargeOutput = getReservedChargeOutput();
        int limited = 0;
        List<Long> batchParticipants = new ArrayList<>();

        for (UnitRequest candidate : requests) {
            BloomwyrmWorkRequest request = candidate.request();
            int heartOffer = request.eligibleParallel();
            if (request.requiredVoltage() > inputVoltage) {
                heartOffer = 0;
            }
            heartOffer = limit(heartOffer, remainingEU, request.eutPerParallel());
            heartOffer = limit(heartOffer, remainingBiopower, request.biopowerInputPerParallel());
            heartOffer = limit(heartOffer, remainingCharge, request.chargeInputPerParallel());
            heartOffer = limit(heartOffer, CHARGE_CAPACITY - remainingCharge - reservedChargeOutput,
                    netChargeOutputPerParallel(request));

            BloomwyrmAllocationConstraint constraint = findConstraint(
                    request,
                    heartOffer,
                    inputVoltage,
                    remainingEU,
                    remainingBiopower,
                    remainingCharge,
                    CHARGE_CAPACITY - remainingCharge - reservedChargeOutput);
            candidate.unit().recordHeartOffer(heartOffer, constraint);
            int parallel = Math.min(request.requestedParallel(), heartOffer);
            if (parallel <= 0) {
                candidate.unit().denyAllocation(constraint);
                limited++;
                continue;
            }
            if (!candidate.unit().beginAllocation(request, parallel)) {
                candidate.unit().recordHeartOffer(0, BloomwyrmAllocationConstraint.LOCAL_IO);
                limited++;
                continue;
            }
            candidate.unit().recordHeartOffer(heartOffer, constraint);
            batchParticipants.add(candidate.unit().getBlockPos().asLong());

            long usedEU = multiply(request.eutPerParallel(), parallel);
            int usedBiopower = multiplyInt(request.biopowerInputPerParallel(), parallel);
            int outputBiopower = multiplyInt(request.biopowerOutputPerParallel(), parallel);
            long usedCharge = multiply(request.chargeInputPerParallel(), parallel);
            long outputCharge = multiply(request.chargeOutputPerParallel(), parallel);
            remainingEU = Math.max(0, remainingEU - usedEU);
            remainingBiopower = saturatingAdd(Math.max(0, remainingBiopower - usedBiopower), outputBiopower);
            producedBiopower = saturatingAdd(producedBiopower, outputBiopower);
            remainingCharge = Math.max(0, remainingCharge - usedCharge);
            reservedChargeOutput = saturatingAdd(reservedChargeOutput, outputCharge);
            storedCharge = remainingCharge;
            if (parallel < request.requestedParallel()) {
                candidate.unit().denyAllocation(constraint);
                limited++;
            }
        }
        allocatedBiopowerCapacity = producedBiopower;
        allocationBatchParticipants = batchParticipants.stream().mapToLong(Long::longValue).toArray();
        lastLimitedUnits = limited;
        allocationBatchActive = getActiveUnitCount() > 0;
        evaluatedPowerTick = Long.MIN_VALUE;
    }

    public void markAllocationComplete(BlockPos position) {
        long packedPosition = position.asLong();
        allocationBatchParticipants = Arrays.stream(allocationBatchParticipants)
                .filter(participant -> participant != packedPosition)
                .toArray();
    }

    private boolean hasPendingBatchAllocations() {
        if (!(getLevel() instanceof ServerLevel level)) return allocationBatchActive;
        Set<GlobalPos> linkedPartners = getLinkedPartners();
        for (long participant : allocationBatchParticipants) {
            GlobalPos position = GlobalPos.of(level.dimension(), BlockPos.of(participant));
            if (!linkedPartners.contains(position) || !isPartnerInRange(position)) continue;
            ILinkedMultiblock linked = getPartnerMachine(position);
            if (linked == null) return true;
            if (linked instanceof BloomwyrmUnitMachine unit && unit.hasAllocation()) return true;
        }
        return false;
    }

    private long getReservedChargeOutput() {
        long reserved = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            reserved = saturatingAdd(reserved, unit.getAllocatedChargeOutput());
        }
        return reserved;
    }

    private int getCurrentBiopowerOutput() {
        int output = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            if (unit.hasAllocation()) {
                output = saturatingAdd(output, unit.getAllocatedBiopowerOutput());
            }
        }
        return output;
    }

    private List<BloomwyrmUnitMachine> getLoadedUnits() {
        return getLinkedPartners().stream()
                .filter(this::isPartnerInRange)
                .sorted(Comparator
                        .comparing((GlobalPos pos) -> pos.dimension().location().toString())
                        .thenComparingInt(pos -> pos.pos().getX())
                        .thenComparingInt(pos -> pos.pos().getY())
                        .thenComparingInt(pos -> pos.pos().getZ()))
                .map(this::getPartnerMachine)
                .filter(BloomwyrmUnitMachine.class::isInstance)
                .map(BloomwyrmUnitMachine.class::cast)
                .toList();
    }

    private void syncChildChunkTickets() {
        if (!(getLevel() instanceof ServerLevel level)) return;
        long gameTime = level.getGameTime();
        if (lastChunkTicketSync != Long.MIN_VALUE && gameTime - lastChunkTicketSync < 20) return;
        lastChunkTicketSync = gameTime;

        if (!isFormed() || !BloomwyrmChunkLoading.isExternallyForced(level, getBlockPos())) {
            BloomwyrmChunkLoading.release(level, getBlockPos(), forcedChildChunks);
            return;
        }

        long heartChunk = new ChunkPos(getBlockPos()).toLong();
        Set<Long> required = new HashSet<>();
        for (GlobalPos partner : getLinkedPartners()) {
            if (!isPartnerInRange(partner)) continue;
            long controllerChunk = new ChunkPos(partner.pos()).toLong();
            if (controllerChunk != heartChunk) required.add(controllerChunk);

            ILinkedMultiblock linked = getPartnerMachine(partner);
            if (!(linked instanceof BloomwyrmUnitMachine unit) || !unit.isFormed()) continue;
            for (long packedPos : unit.getDefaultPatternState().getCache().keySet()) {
                long structureChunk = new ChunkPos(BlockPos.of(packedPos)).toLong();
                if (structureChunk != heartChunk) required.add(structureChunk);
            }
        }
        BloomwyrmChunkLoading.update(level, getBlockPos(), forcedChildChunks, required);
    }

    private boolean isPartnerInRange(GlobalPos partner) {
        GlobalPos own = getGlobalPos();
        return own != null &&
                own.dimension().equals(partner.dimension()) &&
                own.pos().distSqr(partner.pos()) <= MAX_LINK_DISTANCE_SQUARED;
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        if (getLevel() instanceof ServerLevel level) {
            BloomwyrmChunkLoading.release(level, getBlockPos(), forcedChildChunks);
        }
    }

    @Override
    public void onUnload() {
        if (getLevel() instanceof ServerLevel level) {
            BloomwyrmChunkLoading.release(level, getBlockPos(), forcedChildChunks);
        }
        super.onUnload();
    }

    @Override
    public void onMachineDestroyed() {
        if (getLevel() instanceof ServerLevel level) {
            BloomwyrmChunkLoading.release(level, getBlockPos(), forcedChildChunks);
        }
        super.onMachineDestroyed();
    }

    private EnergyContainerList getCampusEnergyContainer() {
        var handlers = getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        if (handlers == null || handlers.isEmpty()) {
            return null;
        }
        return new EnergyContainerList(handlers.stream()
                .filter(IEnergyContainer.class::isInstance)
                .map(IEnergyContainer.class::cast)
                .toList());
    }

    public long getCampusEnergyBudget() {
        EnergyContainerList energy = getCampusEnergyContainer();
        if (energy == null) return 0;
        return multiply(energy.getInputVoltage(), energy.getInputAmperage());
    }

    public long getCampusInputVoltage() {
        EnergyContainerList energy = getCampusEnergyContainer();
        return energy == null ? 0 : energy.getHighestInputVoltage();
    }

    private static int limit(int current, long available, long cost) {
        if (cost <= 0) return current;
        if (available <= 0) return 0;
        return Math.min(current, (int) Math.min(Integer.MAX_VALUE, available / cost));
    }

    private static BloomwyrmAllocationConstraint findConstraint(
                                                                BloomwyrmWorkRequest request,
                                                                int allocated,
                                                                long inputVoltage,
                                                                long energy,
                                                                int biopower,
                                                                long charge,
                                                                long chargeCapacity) {
        if (allocated >= request.requestedParallel()) {
            return BloomwyrmAllocationConstraint.NONE;
        }
        if (request.requiredVoltage() > inputVoltage) {
            return BloomwyrmAllocationConstraint.ENERGY;
        }
        if (request.eutPerParallel() > 0 && energy / request.eutPerParallel() <= allocated) {
            return BloomwyrmAllocationConstraint.ENERGY;
        }
        if (request.biopowerInputPerParallel() > 0 &&
                biopower / request.biopowerInputPerParallel() <= allocated) {
            return BloomwyrmAllocationConstraint.BIOPOWER;
        }
        if (request.chargeInputPerParallel() > 0 && charge / request.chargeInputPerParallel() <= allocated) {
            return BloomwyrmAllocationConstraint.CHARGE;
        }
        long netChargeOutput = netChargeOutputPerParallel(request);
        if (netChargeOutput > 0 && chargeCapacity / netChargeOutput <= allocated) {
            return BloomwyrmAllocationConstraint.HEART_CAPACITY;
        }
        return BloomwyrmAllocationConstraint.LOCAL_IO;
    }

    private static long netChargeOutputPerParallel(BloomwyrmWorkRequest request) {
        return Math.max(0, request.chargeOutputPerParallel() - request.chargeInputPerParallel());
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        LongSyncValue charge = new LongSyncValue(this::getStoredCharge);
        IntSyncValue biopower = new IntSyncValue(this::getBiopowerCapacity);
        IntSyncValue usedBiopower = new IntSyncValue(this::getAllocatedBiopower);
        LongSyncValue eut = new LongSyncValue(this::getAllocatedEUt);
        LongSyncValue energyBudget = new LongSyncValue(this::getCampusEnergyBudget);
        LongSyncValue inputVoltage = new LongSyncValue(this::getCampusInputVoltage);
        IntSyncValue linked = new IntSyncValue(this::getLoadedUnitCount);
        IntSyncValue active = new IntSyncValue(this::getActiveUnitCount);
        IntSyncValue limited = new IntSyncValue(this::getLastLimitedUnits);
        IntSyncValue cycleSeconds = new IntSyncValue(this::getCycleSecondsRemaining);
        BooleanSyncValue powered = new BooleanSyncValue(this::ensureCampusPowerForCurrentTick);
        BooleanSyncValue cycleBlocked = new BooleanSyncValue(this::isCycleBlockedByActiveBatch);
        syncManager.syncValue("bloomwyrm_heart_charge", charge);
        syncManager.syncValue("bloomwyrm_heart_biopower", biopower);
        syncManager.syncValue("bloomwyrm_heart_used_biopower", usedBiopower);
        syncManager.syncValue("bloomwyrm_heart_eut", eut);
        syncManager.syncValue("bloomwyrm_heart_energy_budget", energyBudget);
        syncManager.syncValue("bloomwyrm_heart_input_voltage", inputVoltage);
        syncManager.syncValue("bloomwyrm_heart_linked", linked);
        syncManager.syncValue("bloomwyrm_heart_active", active);
        syncManager.syncValue("bloomwyrm_heart_limited", limited);
        syncManager.syncValue("bloomwyrm_heart_cycle_seconds", cycleSeconds);
        syncManager.syncValue("bloomwyrm_heart_powered", powered);
        syncManager.syncValue("bloomwyrm_heart_cycle_blocked", cycleBlocked);
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.charge",
                coloredValue(FormattingUtil.formatNumbers(charge.getLongValue()), ChatFormatting.AQUA),
                coloredValue(FormattingUtil.formatNumbers(CHARGE_CAPACITY), ChatFormatting.DARK_AQUA))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.biopower",
                coloredValue(FormattingUtil.formatNumbers(usedBiopower.getIntValue()), ChatFormatting.LIGHT_PURPLE),
                coloredValue(FormattingUtil.formatNumbers(biopower.getIntValue()), ChatFormatting.LIGHT_PURPLE))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.energy",
                coloredValue(
                        FormattingUtil.formatNumbers(eut.getLongValue()),
                        powered.getBoolValue() ? ChatFormatting.GREEN : ChatFormatting.RED))
                .withStyle(ChatFormatting.WHITE))
                .asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.supply",
                coloredValue(FormattingUtil.formatNumbers(energyBudget.getLongValue()), ChatFormatting.YELLOW),
                coloredValue(FormattingUtil.formatNumbers(inputVoltage.getLongValue()), ChatFormatting.AQUA))
                .withStyle(ChatFormatting.WHITE))
                .asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.units",
                coloredValue(active.getIntValue(), ChatFormatting.GREEN),
                coloredValue(linked.getIntValue(), ChatFormatting.AQUA))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.limited",
                coloredValue(
                        limited.getIntValue(),
                        limited.getIntValue() > 0 ? ChatFormatting.RED : ChatFormatting.GREEN))
                .withStyle(ChatFormatting.WHITE))
                .asWidget());
        widgets.add(Text.dynamic(() -> cycleBlocked.getBoolValue() ?
                Component.translatable("cosmiccore.bloomwyrm.heart.cycle_blocked")
                        .withStyle(ChatFormatting.GOLD) :
                Component.translatable(
                        "cosmiccore.bloomwyrm.heart.cycle",
                        coloredValue(formatCycleSeconds(cycleSeconds.getIntValue()), ChatFormatting.AQUA))
                        .withStyle(ChatFormatting.WHITE))
                .asWidget());
        return widgets;
    }

    @Override
    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        return BloomwyrmDisplayUI.create(this, syncManager);
    }

    private static Component coloredValue(Object value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }

    public static String formatCycleSeconds(int totalSeconds) {
        int boundedSeconds = Math.max(0, totalSeconds);
        return boundedSeconds / 60 + ":" + String.format("%02d", boundedSeconds % 60);
    }

    private static long multiply(long value, long multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }

    private static int multiplyInt(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Integer.MAX_VALUE / multiplier) return Integer.MAX_VALUE;
        return value * multiplier;
    }

    private static long saturatingAdd(long left, long right) {
        if (right > 0 && left > Long.MAX_VALUE - right) return Long.MAX_VALUE;
        return left + right;
    }

    private static int saturatingAdd(int left, int right) {
        if (right > 0 && left > Integer.MAX_VALUE - right) return Integer.MAX_VALUE;
        return left + right;
    }

    private record UnitRequest(BloomwyrmUnitMachine unit, BloomwyrmWorkRequest request) {}
}
