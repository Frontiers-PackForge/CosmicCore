package com.ghostipedia.cosmiccore.api.capability.recipe;

import com.ghostipedia.cosmiccore.api.capability.IHeatInfoProvider;

import net.minecraft.core.Direction;

public interface IHeatContainer extends IHeatInfoProvider {

    /*
     * Basically just changeHeat(long) but should also handle overloads and capacity.
     * This is what I probably want to use between blocks...
     * fuck if I know
     */
    long acceptHeatFromNetwork(Direction side);

    // Returns: if this container can accept heat from this side.
    boolean inputsHeat(Direction side);

    // Returns: if this container can eject heat from this side.
    default boolean outputsHeat(Direction side) {
        return false;
    };

    /*
     * The Magic Sauce.
     * for handling logic within machine running behaviors, DO NOT. Use this for the eventual pipenet
     * update this desc to properly reflect interface methods once they are adapted.
     */
    long changeHeat(long heatDifference);

    /*
     * Adds a set amount of heat to this heat container
     * Params : heatToAdd - amount of heat to add.
     * Returns : amount of heat added.
     */
    default long addHeat(long heatToAdd) {
        return changeHeat(heatToAdd);
    }

    /*
     * Removes a set amount of heat to this heat container
     * Params : heatToRemove - amount of heat to remove.
     * Returns : amount of heat removed.
     */
    default long removeHeat(long heatToRemove) {
        return -changeHeat(-heatToRemove);
    }

    // Heat Containers Do not have an insertion limit. Thus we melt the block if they overload.
    // TODO : The Math that actually makes this behave less psychotic. And actually function.
    default boolean getHeatCanBeOverloaded() {
        if (getOverloadLimit() > getHeatStorage()) {
            return getHeatInfo().overload();
        }
        return false;
    }

    // Reports the Current Thermal Maximum a container can withstand
    long getOverloadLimit();

    // Reports the Current Temperature.
    long getHeatStorage();

    @Override
    default HeatInfo getHeatInfo() {
        return new HeatInfo(getHeatStorage(), getHeatStorage(), getHeatCanBeOverloaded());
    };

    // Params needs to build the container.
    // This Abomination - Allows Going below Absolute Zero
    @Override
    default boolean supportsImpossibleHeatValues() {
        return false;
    };

    // Max amount of heat that can be output per tick
    default long getEjectLimit() {
        return 0L;
    };

    // Max amount of heat that can be accepted per tick
    default long getAcceptLimit() {
        return 0L;
    }

    // Input per second
    default long getHeatInputPerSec() {
        return 0L;
    }

    // Output per second
    default long getHeatOutputPerSec() {
        return 0L;
    }

    IHeatContainer DEFAULT = new IHeatContainer() {

        @Override
        public long acceptHeatFromNetwork(Direction side) {
            return 0;
        }

        @Override
        public boolean inputsHeat(Direction side) {
            return false;
        }

        @Override
        public long changeHeat(long heatDifference) {
            return 0;
        }

        @Override
        public long getOverloadLimit() {
            return 0;
        }

        @Override
        public long getHeatStorage() {
            return 0;
        }
    };
}
