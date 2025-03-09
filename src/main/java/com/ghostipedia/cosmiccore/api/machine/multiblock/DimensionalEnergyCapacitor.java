package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DimensionalEnergyCapacitor extends DimensionalEnergyCapacitorInterface{

    public static final int MAX_BATTERY_LAYER = 18;
    public static final int MIN_CASINGS = 14;


    public DimensionalEnergyCapacitor(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        List<IBatteryData> batteries = new ArrayList<>();
        for (Map.Entry<String, Object> battery : getMultiblockState().getMatchContext().entrySet()) {
            if (battery.getKey().startsWith(PowerSubstationMachine.PMC_BATTERY_HEADER)
                    && battery.getValue() instanceof PowerSubstationMachine.BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.getAmount(); i++) {
                    batteries.add(wrapper.getPartType());
                }
            }
        }

        if (batteries.isEmpty()) {
            onStructureInvalid();
            return;
        }

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getHolder().getOwner().getUUID();
            var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);


            var capacity = batteries.stream().mapToLong(IBatteryData::getCapacity)
                    .mapToObj(BigInteger::valueOf).reduce(BigInteger.ZERO, BigInteger::add);

            wirelessData.setCapacity(owner, capacity);
            wirelessData.setActive(owner, true);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getHolder().getOwner().getUUID();
            var wirelessData = WirelessEnergySavedData.getOrCreate(serverLevel);
            wirelessData.setActive(owner, false);
        }
    }

    @Getter
    public static class CosmicBatteryMatchWrapper extends PowerSubstationMachine.BatteryMatchWrapper {

        private final IBatteryData partType;
        private int amount;

        public CosmicBatteryMatchWrapper(IBatteryData partType) {
            super(partType);
            this.partType = partType;
        }

        public CosmicBatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }
}
