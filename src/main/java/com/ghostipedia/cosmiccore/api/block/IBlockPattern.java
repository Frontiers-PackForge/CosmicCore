package com.ghostipedia.cosmiccore.api.block;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;

import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGrid;

public interface IBlockPattern {

    void cosmiccore$autoBuild(Player player, MultiblockState worldState, IGrid grid);
}
