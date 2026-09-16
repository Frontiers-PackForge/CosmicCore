package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.common.machine.trait.CleanroomProviderTrait;
import com.gregtechceu.gtceu.common.machine.trait.CleanroomReceiverTrait;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CleanroomReceiverTrait.class)
public interface CleanroomReceiverTraitAccessor {

    @Accessor("cleanroomProvider")
    CleanroomProviderTrait cosmiccore$getCleanroomProvider();
}
