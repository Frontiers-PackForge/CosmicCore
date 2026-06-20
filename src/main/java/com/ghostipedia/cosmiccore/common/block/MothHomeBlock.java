package com.ghostipedia.cosmiccore.common.block;

import net.minecraft.world.level.block.Block;

import lombok.Getter;

/**
 * Moth Home block - provides moths for the Cargo Moth system.
 * Different tiers provide faster cycles and more moths per home.
 */
public class MothHomeBlock extends Block {

    @Getter
    private final int tier;

    public MothHomeBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }
}
