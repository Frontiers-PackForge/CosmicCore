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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BloomwyrmHeartMachine extends LinkedWorkableElectricMultiblockMachine {

    public static final int MAX_PARTNERS = 32;
    public static final int MAX_LINK_DISTANCE = 64;
    public static final double MAX_LINK_DISTANCE_SQUARED = MAX_LINK_DISTANCE * MAX_LINK_DISTANCE;
    public static final int SEASON_TICKS = 1200;
    public static final int BOOTSTRAP_BIOPOWER = 16;
    public static final long CHARGE_CAPACITY = 1_000_000L;
    public static final long SEASONAL_CHARGE_CAPACITY = 250_000L;

    @SaveField
    private long storedCharge;
    @SaveField
    private long germinationCharge;
    @SaveField
    private long proliferationCharge;
    @SaveField
    private long bloomCharge;
    @SaveField
    private long senescenceCharge;
    @SaveField
    private int seasonOrdinal;
    @SaveField
    private int seasonProgress;
    @SaveField
    private boolean seasonAllocated;
    @SaveField
    private int seasonalBiopowerCapacity;
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

    public BloomwyrmSeason getSeason() {
        return BloomwyrmSeason.fromOrdinal(seasonOrdinal);
    }

    public int getSeasonProgress() {
        return seasonProgress;
    }

    public long getStoredCharge() {
        return storedCharge;
    }

    public long getChargeCapacity() {
        return CHARGE_CAPACITY;
    }

    public long getSeasonalCharge(BloomwyrmSeason season) {
        return switch (season) {
            case GERMINATION -> germinationCharge;
            case PROLIFERATION -> proliferationCharge;
            case BLOOM -> bloomCharge;
            case SENESCENCE -> senescenceCharge;
        };
    }

    public long getSeasonalChargeCapacity() {
        return SEASONAL_CHARGE_CAPACITY;
    }

    public int getBiopowerCapacity() {
        return saturatingAdd(BOOTSTRAP_BIOPOWER, seasonalBiopowerCapacity);
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

    public long acceptCharge(long amount) {
        if (amount <= 0) return 0;
        long accepted = Math.min(amount, CHARGE_CAPACITY - storedCharge);
        storedCharge += accepted;
        return accepted;
    }

    public long acceptSeasonalCharge(BloomwyrmSeason season, long amount) {
        if (season == null || amount <= 0) return 0;
        long stored = getSeasonalCharge(season);
        long accepted = Math.min(amount, SEASONAL_CHARGE_CAPACITY - stored);
        setSeasonalCharge(season, stored + accepted);
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
        if (!seasonAllocated) {
            allocateSeason();
        }
        if (ensureCampusPowerForCurrentTick() && seasonProgress < SEASON_TICKS) {
            seasonProgress++;
        }
        if (seasonProgress >= SEASON_TICKS && getActiveUnitCount() == 0) {
            seasonOrdinal = getSeason().next().ordinal();
            seasonProgress = 0;
            seasonAllocated = false;
            seasonalBiopowerCapacity = 0;
            evaluatedPowerTick = Long.MIN_VALUE;
        }
    }

    private void allocateSeason() {
        List<BloomwyrmUnitMachine> units = getLoadedUnits();
        List<UnitRequest> requests = new ArrayList<>();
        for (BloomwyrmUnitMachine unit : units) {
            if (!unit.isAvailableForAllocation()) {
                continue;
            }
            var request = unit.getRecipeLogic().createRequest(getSeason());
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
        int remainingBiopower = BOOTSTRAP_BIOPOWER;
        int producedBiopower = 0;
        long remainingCharge = storedCharge;
        long reservedChargeOutput = getReservedChargeOutput();
        long[] remainingSeasonalCharge = getSeasonalChargeSnapshot();
        long[] reservedSeasonalChargeOutput = getReservedSeasonalChargeOutput();
        int limited = 0;

        for (UnitRequest candidate : requests) {
            BloomwyrmWorkRequest request = candidate.request();
            int heartOffer = request.eligibleParallel();
            heartOffer = limit(heartOffer, remainingEU, request.eutPerParallel());
            heartOffer = limit(heartOffer, remainingBiopower, request.biopowerInputPerParallel());
            heartOffer = limit(heartOffer, remainingCharge, request.chargeInputPerParallel());
            heartOffer = limit(heartOffer, CHARGE_CAPACITY - remainingCharge - reservedChargeOutput,
                    request.chargeOutputPerParallel());

            int standardOffer = heartOffer;
            int seasonalInputOffer = heartOffer;
            int seasonalOutputOffer = heartOffer;
            if (request.favoredSeason() != null) {
                int season = request.favoredSeason().ordinal();
                seasonalInputOffer = limitSeasonalCharge(
                        candidate.unit(),
                        request,
                        seasonalInputOffer,
                        getSeason(),
                        remainingSeasonalCharge[season]);
                seasonalOutputOffer = limit(
                        seasonalInputOffer,
                        SEASONAL_CHARGE_CAPACITY - remainingSeasonalCharge[season] -
                                reservedSeasonalChargeOutput[season],
                        request.seasonalChargeOutputPerParallel());
                heartOffer = seasonalOutputOffer;
            }

            BloomwyrmAllocationConstraint constraint;
            if (seasonalInputOffer < standardOffer) {
                constraint = BloomwyrmAllocationConstraint.SEASONAL_CHARGE;
            } else if (seasonalOutputOffer < seasonalInputOffer) {
                constraint = BloomwyrmAllocationConstraint.HEART_CAPACITY;
            } else {
                constraint = findConstraint(
                        request,
                        heartOffer,
                        remainingEU,
                        remainingBiopower,
                        remainingCharge,
                        CHARGE_CAPACITY - remainingCharge - reservedChargeOutput);
            }
            candidate.unit().recordHeartOffer(heartOffer, constraint);
            int parallel = Math.min(request.requestedParallel(), heartOffer);
            if (parallel <= 0) {
                candidate.unit().denyAllocation(constraint);
                limited++;
                continue;
            }
            if (!candidate.unit().beginAllocation(request, parallel, getSeason())) {
                candidate.unit().recordHeartOffer(0, BloomwyrmAllocationConstraint.LOCAL_IO);
                limited++;
                continue;
            }
            candidate.unit().recordHeartOffer(heartOffer, constraint);

            long usedEU = multiply(request.eutPerParallel(), parallel);
            int usedBiopower = multiplyInt(request.biopowerInputPerParallel(), parallel);
            int outputBiopower = multiplyInt(request.biopowerOutputPerParallel(), parallel);
            long usedCharge = multiply(request.chargeInputPerParallel(), parallel);
            long outputCharge = multiply(request.chargeOutputPerParallel(), parallel);
            long usedSeasonalCharge = candidate.unit().getSeasonalChargeCost(request, parallel, getSeason());
            long outputSeasonalCharge = multiply(request.seasonalChargeOutputPerParallel(), parallel);
            remainingEU = Math.max(0, remainingEU - usedEU);
            remainingBiopower = saturatingAdd(Math.max(0, remainingBiopower - usedBiopower), outputBiopower);
            producedBiopower = saturatingAdd(producedBiopower, outputBiopower);
            remainingCharge = Math.max(0, remainingCharge - usedCharge);
            reservedChargeOutput = saturatingAdd(reservedChargeOutput, outputCharge);
            storedCharge = remainingCharge;
            if (request.favoredSeason() != null) {
                int season = request.favoredSeason().ordinal();
                remainingSeasonalCharge[season] = Math.max(
                        0,
                        remainingSeasonalCharge[season] - usedSeasonalCharge);
                reservedSeasonalChargeOutput[season] = saturatingAdd(
                        reservedSeasonalChargeOutput[season],
                        outputSeasonalCharge);
                setSeasonalCharge(request.favoredSeason(), remainingSeasonalCharge[season]);
            }
            if (parallel < request.requestedParallel()) {
                candidate.unit().denyAllocation(constraint);
                limited++;
            }
        }
        seasonalBiopowerCapacity = producedBiopower;
        lastLimitedUnits = limited;
        seasonAllocated = true;
        evaluatedPowerTick = Long.MIN_VALUE;
    }

    private long getReservedChargeOutput() {
        long reserved = 0;
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            reserved = saturatingAdd(reserved, unit.getAllocatedChargeOutput());
        }
        return reserved;
    }

    private long[] getSeasonalChargeSnapshot() {
        BloomwyrmSeason[] seasons = BloomwyrmSeason.values();
        long[] snapshot = new long[seasons.length];
        for (BloomwyrmSeason season : seasons) {
            snapshot[season.ordinal()] = getSeasonalCharge(season);
        }
        return snapshot;
    }

    private long[] getReservedSeasonalChargeOutput() {
        long[] reserved = new long[BloomwyrmSeason.values().length];
        for (BloomwyrmUnitMachine unit : getLoadedUnits()) {
            BloomwyrmSeason season = unit.getAllocatedSeasonalChargeSeason();
            if (season != null) {
                reserved[season.ordinal()] = saturatingAdd(
                        reserved[season.ordinal()],
                        unit.getAllocatedSeasonalChargeOutput());
            }
        }
        return reserved;
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

    private long getCampusEnergyBudget() {
        EnergyContainerList energy = getCampusEnergyContainer();
        if (energy == null) return 0;
        return multiply(energy.getInputVoltage(), energy.getInputAmperage());
    }

    private static int limit(int current, long available, long cost) {
        if (cost <= 0) return current;
        if (available <= 0) return 0;
        return Math.min(current, (int) Math.min(Integer.MAX_VALUE, available / cost));
    }

    private static int limitSeasonalCharge(
                                           BloomwyrmUnitMachine unit,
                                           BloomwyrmWorkRequest request,
                                           int current,
                                           BloomwyrmSeason season,
                                           long available) {
        int limited = current;
        while (limited > 0 && unit.getSeasonalChargeCost(request, limited, season) > available) {
            limited--;
        }
        return limited;
    }

    private void setSeasonalCharge(BloomwyrmSeason season, long amount) {
        long bounded = Math.max(0, Math.min(SEASONAL_CHARGE_CAPACITY, amount));
        switch (season) {
            case GERMINATION -> germinationCharge = bounded;
            case PROLIFERATION -> proliferationCharge = bounded;
            case BLOOM -> bloomCharge = bounded;
            case SENESCENCE -> senescenceCharge = bounded;
        }
    }

    private static BloomwyrmAllocationConstraint findConstraint(
                                                                BloomwyrmWorkRequest request,
                                                                int allocated,
                                                                long energy,
                                                                int biopower,
                                                                long charge,
                                                                long chargeCapacity) {
        if (allocated >= request.eligibleParallel()) {
            return BloomwyrmAllocationConstraint.NONE;
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
        if (request.chargeOutputPerParallel() > 0 &&
                chargeCapacity / request.chargeOutputPerParallel() <= allocated) {
            return BloomwyrmAllocationConstraint.HEART_CAPACITY;
        }
        return BloomwyrmAllocationConstraint.LOCAL_IO;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        IntSyncValue season = new IntSyncValue(() -> getSeason().ordinal());
        IntSyncValue progress = new IntSyncValue(this::getSeasonProgress);
        LongSyncValue charge = new LongSyncValue(this::getStoredCharge);
        LongSyncValue germination = new LongSyncValue(() -> getSeasonalCharge(BloomwyrmSeason.GERMINATION));
        LongSyncValue proliferation = new LongSyncValue(() -> getSeasonalCharge(BloomwyrmSeason.PROLIFERATION));
        LongSyncValue bloom = new LongSyncValue(() -> getSeasonalCharge(BloomwyrmSeason.BLOOM));
        LongSyncValue senescence = new LongSyncValue(() -> getSeasonalCharge(BloomwyrmSeason.SENESCENCE));
        IntSyncValue biopower = new IntSyncValue(this::getBiopowerCapacity);
        IntSyncValue usedBiopower = new IntSyncValue(this::getAllocatedBiopower);
        LongSyncValue eut = new LongSyncValue(this::getAllocatedEUt);
        IntSyncValue linked = new IntSyncValue(this::getLoadedUnitCount);
        IntSyncValue active = new IntSyncValue(this::getActiveUnitCount);
        IntSyncValue limited = new IntSyncValue(this::getLastLimitedUnits);
        BooleanSyncValue powered = new BooleanSyncValue(this::ensureCampusPowerForCurrentTick);
        syncManager.syncValue("bloomwyrm_heart_season", season);
        syncManager.syncValue("bloomwyrm_heart_progress", progress);
        syncManager.syncValue("bloomwyrm_heart_charge", charge);
        syncManager.syncValue("bloomwyrm_heart_germination_charge", germination);
        syncManager.syncValue("bloomwyrm_heart_proliferation_charge", proliferation);
        syncManager.syncValue("bloomwyrm_heart_bloom_charge", bloom);
        syncManager.syncValue("bloomwyrm_heart_senescence_charge", senescence);
        syncManager.syncValue("bloomwyrm_heart_biopower", biopower);
        syncManager.syncValue("bloomwyrm_heart_used_biopower", usedBiopower);
        syncManager.syncValue("bloomwyrm_heart_eut", eut);
        syncManager.syncValue("bloomwyrm_heart_linked", linked);
        syncManager.syncValue("bloomwyrm_heart_active", active);
        syncManager.syncValue("bloomwyrm_heart_limited", limited);
        syncManager.syncValue("bloomwyrm_heart_powered", powered);
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.season",
                Component.translatable(BloomwyrmSeason.fromOrdinal(season.getIntValue()).translationKey())
                        .withStyle(seasonColor(BloomwyrmSeason.fromOrdinal(season.getIntValue()))),
                coloredValue(formatSeconds(progress.getIntValue()), ChatFormatting.WHITE),
                coloredValue(formatSeconds(SEASON_TICKS), ChatFormatting.WHITE))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.charge",
                coloredValue(FormattingUtil.formatNumbers(charge.getLongValue()), ChatFormatting.AQUA),
                coloredValue(FormattingUtil.formatNumbers(CHARGE_CAPACITY), ChatFormatting.DARK_AQUA))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(seasonalChargeLine(BloomwyrmSeason.GERMINATION, germination));
        widgets.add(seasonalChargeLine(BloomwyrmSeason.PROLIFERATION, proliferation));
        widgets.add(seasonalChargeLine(BloomwyrmSeason.BLOOM, bloom));
        widgets.add(seasonalChargeLine(BloomwyrmSeason.SENESCENCE, senescence));
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
        return widgets;
    }

    @Override
    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        return BloomwyrmDisplayUI.create(this, syncManager);
    }

    private static Component coloredValue(Object value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }

    private static IWidget seasonalChargeLine(BloomwyrmSeason season, LongSyncValue charge) {
        return Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.heart.seasonal_charge",
                Component.translatable(season.essenceTranslationKey()).withStyle(seasonColor(season)),
                coloredValue(FormattingUtil.formatNumbers(charge.getLongValue()), ChatFormatting.WHITE),
                coloredValue(FormattingUtil.formatNumbers(SEASONAL_CHARGE_CAPACITY), ChatFormatting.DARK_AQUA))
                .withStyle(ChatFormatting.WHITE)).asWidget();
    }

    private static String formatSeconds(int ticks) {
        return String.format(Locale.ROOT, "%.2f", ticks / 20.0);
    }

    private static ChatFormatting seasonColor(BloomwyrmSeason season) {
        return switch (season) {
            case GERMINATION -> ChatFormatting.GREEN;
            case PROLIFERATION -> ChatFormatting.AQUA;
            case BLOOM -> ChatFormatting.LIGHT_PURPLE;
            case SENESCENCE -> ChatFormatting.GOLD;
        };
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
