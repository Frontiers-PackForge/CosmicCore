package com.ghostipedia.cosmiccore.gtbridge.machine.logic;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;

public interface MachineCasingType {
    /**
     * @return the Tier of the Machine Casing
     */
    int getTier();

    /**
     * @return the Machine Casing Block
     */
    BlockEntry<Block> getMachineCasing();

    String getEnergyHatchLevel();
}
