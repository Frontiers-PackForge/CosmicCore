package com.ghostipedia.cosmiccore.common.machine.multiblock.multi;

import com.ghostipedia.cosmiccore.common.abyss.IAbyssTimer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class AbyssWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine implements IAbyssTimer {
    public AbyssWorkableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public long getRemainingTicks(ResourceKey<Level> dimension) {
        return 0;
    }

    @Override
    public void setRemainingTicks(ResourceKey<Level> dimension, long ticks) {

    }

    @Override
    public boolean isDecaying(ResourceKey<Level> dimension) {
        return false;
    }

    @Override
    public void setDecaying(ResourceKey<Level> dimension, boolean decaying) {

    }

    @Override
    public double getCleanse(ResourceKey<Level> dimension) {
        return 0;
    }

    @Override
    public void setCleanse(ResourceKey<Level> dimension, double amount) {

    }
}
