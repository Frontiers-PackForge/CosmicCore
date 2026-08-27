package com.ghostipedia.cosmiccore.mixin.deployer;

import net.liukrast.deployer.lib.logistics.board.StockPanelBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StockPanelBehaviour.class)
public interface StockPanelBehaviourAccessor {

    @Accessor("filter")
    Object cosmiccore$getFilter();

    @Accessor("filter")
    void cosmiccore$setFilter(Object filter);
}
