package com.ghostipedia.cosmiccore.api.machine.feature;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;

/**
 * Interface for the Stellar Iris controller.
 * Modules query this to get processing parameters and stage information.
 */
public interface IStellarIrisProvider extends IMachineFeature {

    /**
     * @return the current stage of the stellar iris
     */
    IrisMultiblockMachine.Stage getStage();

    /**
     * @return whether the iris multiblock is formed
     */
    boolean isFormed();

    /**
     * @return maximum heat provided to modules (affects recipe availability)
     */
    int getMaxHeat();

    /**
     * @return speed bonus multiplier for module recipes
     */
    double getSpeedBonus();

    /**
     * @return energy discount multiplier for module recipes (1.0 = no discount)
     */
    double getEnergyDiscount();

    /**
     * @return maximum parallel recipes for modules
     */
    int getParallelLimit();

    /**
     * Check if the stage allows processing
     * 
     * @return true if the current stage can run module recipes
     */
    default boolean canProcess() {
        IrisMultiblockMachine.Stage stage = getStage();
        return stage == IrisMultiblockMachine.Stage.STAR ||
                stage == IrisMultiblockMachine.Stage.SUPERSTAR ||
                stage == IrisMultiblockMachine.Stage.BLACK_HOLE;
    }
}
