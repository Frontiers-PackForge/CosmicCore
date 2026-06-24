package com.ghostipedia.cosmiccore.mixin;

import com.ghostipedia.cosmiccore.api.block.IBlockPattern;

import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;

import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGrid;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds the {@code cosmiccore$autoBuild} hook (used by the Linked Terminal "build from ME network"
 * feature) onto GTCEu's {@link BlockPattern}.
 * <p>
 * TODO(8.0.0): the original implementation drove GTCEu's old pattern internals
 * (MultiblockState#getController/#update, SimplePredicate#limited/#common, LDLib BlockInfo), all of
 * which were rewritten in 8.0.0 to PatternState / BasePredicate / the new BlockPattern layout. The
 * body is shelved as a no-op for launch; reimplement against the new internals (or fold the feature
 * into the new Terminal behavior) before re-enabling auto-build.
 */
@Mixin(value = BlockPattern.class, remap = false)
public abstract class BlockPatternMixin implements IBlockPattern {

    @Override
    public void cosmiccore$autoBuild(Player player, PatternState worldState, IGrid grid) {
        // TODO(8.0.0): reimplement multiblock auto-build against the new BlockPattern API. Shelved.
    }
}
