package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Insulation.class, remap = false)
public interface InsulationConstructorInvoker {

    @Invoker("<init>")
    static Insulation cosmiccore$create(String enumName, int ordinal, String name, float thickness, int amperage,
                                        int lossMultiplier, TagPrefix tagPrefix, int insulationLevel) {
        throw new AssertionError();
    }
}
