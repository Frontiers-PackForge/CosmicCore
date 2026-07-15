package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.data.savedData.UniqueMultiblockSavedData;
import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import com.lowdragmc.lowdraglib.utils.DummyWorld;

import net.minecraft.server.level.ServerLevel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO(8.0.0 MUI2): custom display text shelved; base default getWidgetsForDisplay UI used for now (original in git
// history).
public class DimensionalEnergyCapacitor extends DimensionalEnergyInterface {

    public static final int MAX_BATTERY_LAYER = 18;
    public static final int MIN_CASINGS = 14;

    // Passive Drain Constants
    // 1% capacity per 24 hours
    public static final long PASSIVE_DRAIN_DIVISOR = 20 * 60 * 60 * 24 * 100;
    // no more than 100kEU/t per storage block
    public static final long PASSIVE_DRAIN_MAX_PER_STORAGE = 100_000L;

    // Used to make sure you cannot have more than one of this multiblock per player / team
    @SaveField
    public boolean isDuplicate = false;

    @SaveField
    private long[] capacities;

    public DimensionalEnergyCapacitor(BlockEntityCreationInfo info) {
        super(info);
        this.localDisplay = false;
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        if (getLevel() instanceof DummyWorld) super.formStructure(substructureName);

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner == MachineOwner.EMPTY) {
                CosmicCore.LOGGER.warn("DimensionalEnergyCapcitor tried to form with null team.");
                return;
            }
            var multiblockId = getDefinition().getId().toString();
            var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);

            if (uniqueMultiblockMapping.hasData(owner, multiblockId, getDimension())) {
                this.isDuplicate = !uniqueMultiblockMapping.isUnique(owner, multiblockId, getDimension(),
                        getBlockPos());
                if (isDuplicate) {
                    recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                    return;
                }
            } else uniqueMultiblockMapping.addMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                    getBlockPos());

            List<IBatteryData> batteries = new ArrayList<>();
            // Re-derive batteries post-formation (match-context accumulator removed in 8.0.0);
            // mirrors GTCEu PowerSubstationMachine.formStructure.
            for (var entry : getDefaultPatternState().getCache().long2ObjectEntrySet()) {
                if (entry.getValue().getBlockState().getBlock() instanceof BatteryBlock batteryBlock &&
                        batteryBlock.getData().getCapacity() > 0) {
                    batteries.add(batteryBlock.getData());
                }
            }

            this.capacities = batteries.stream().mapToLong(IBatteryData::getCapacity).toArray();

            if (batteries.isEmpty()) {
                invalidateStructure(substructureName);
                return;
            }

            super.formStructure(substructureName); // This order is important do not move

            var capacity = batteries.stream().mapToLong(IBatteryData::getCapacity)
                    .mapToObj(BigInteger::valueOf).reduce(BigInteger.ZERO, BigInteger::add);

            wirelessData.setCapacity(owner, capacity);
            wirelessData.setActive(owner, true);
        }
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner != MachineOwner.EMPTY) {
                var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
                var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);
                wirelessData.setActive(owner, false);
                uniqueMultiblockMapping.removeMultiblock(owner, getDefinition().getId().toString(), getDimension(),
                        getBlockPos());
            }
        }
        this.capacities = null;
    }

    @Override
    public boolean isActive() {
        if (isDuplicate) return false;
        return super.isActive();
    }

    @Override
    public long getPassiveDrainPerTick() {
        long[] drains = Arrays.stream(capacities)
                .map(cap -> Math.min(PASSIVE_DRAIN_MAX_PER_STORAGE, cap / PASSIVE_DRAIN_DIVISOR)).toArray();
        return Arrays.stream(drains).sum();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getTeamUUID();
            if (owner != MachineOwner.EMPTY) {
                var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
                wirelessData.setActive(owner, isWorkingAllowed);
            }
        }
    }

    private String getDimension() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.dimension().location().toString();
        }
        return null;
    }
}
