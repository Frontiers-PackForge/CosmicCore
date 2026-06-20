package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

public class MagnetWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    private IMagnetType magnetType = MagnetBlock.MagnetType.HIGH_POWERED;

    public MagnetWorkableElectricMultiblockMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    public IMagnetType getMagnetType() {
        return magnetType;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get("MagnetType");
        if (type instanceof IMagnetType magnet) {
            this.magnetType = magnet;
        }
    }

    public int getMagnetStrength() {
        return magnetType.getMagnetFieldCapacity();
    }

    public int getMagnetRegen() {
        return magnetType.getMagnetRegenRate();
    }

    public int getEnergyCost() {
        return magnetType.energyConsumption();
    }
}
