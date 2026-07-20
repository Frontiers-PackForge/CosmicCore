package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.gregtechceu.gtceu.api.block.OreBlock;

import net.minecraft.world.level.block.state.BlockState;

public final class OreFieldBlockRules {

    private OreFieldBlockRules() {}

    public static boolean isFieldOre(BlockState state) {
        return state.getBlock() instanceof OreBlock oreBlock &&
                OreFieldPlacement.bundles().contains(oreBlock.material);
    }
}
