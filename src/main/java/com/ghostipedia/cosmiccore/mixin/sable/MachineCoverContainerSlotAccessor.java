package com.ghostipedia.cosmiccore.mixin.sable;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MachineCoverContainer.class, remap = false)
public interface MachineCoverContainerSlotAccessor {

    @Accessor("up")
    void cosmiccore$setUp(@Nullable CoverBehavior cover);

    @Accessor("down")
    void cosmiccore$setDown(@Nullable CoverBehavior cover);

    @Accessor("north")
    void cosmiccore$setNorth(@Nullable CoverBehavior cover);

    @Accessor("south")
    void cosmiccore$setSouth(@Nullable CoverBehavior cover);

    @Accessor("west")
    void cosmiccore$setWest(@Nullable CoverBehavior cover);

    @Accessor("east")
    void cosmiccore$setEast(@Nullable CoverBehavior cover);
}
