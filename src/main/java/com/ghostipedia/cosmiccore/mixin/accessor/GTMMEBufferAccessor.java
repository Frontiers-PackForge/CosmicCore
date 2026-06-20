package com.ghostipedia.cosmiccore.mixin.accessor;

import com.gregtechceu.gtceu.integration.ae2.machine.MEPatternBufferPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MEPatternBufferPartMachine.class)
public interface GTMMEBufferAccessor {

    @Accessor("MAX_PATTERN_COUNT")
    @Mutable
    static void setMaxPatternCount(int value) {
        throw new AssertionError();
    }
}
