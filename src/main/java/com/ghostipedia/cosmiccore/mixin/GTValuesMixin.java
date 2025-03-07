package com.ghostipedia.cosmiccore.mixin;

import com.gregtechceu.gtceu.api.GTValues;

import org.spongepowered.asm.mixin.*;

@Debug
@Mixin(GTValues.class)
public class GTValuesMixin {

    @Final
    @Mutable
    @Shadow
    public static int[] VC;

    static {
        VC[2] = 0x03c2fc;
        VC[3] = 0x33ff5c;
        // 4 Doesn't need modification, it's already gray
        VC[5] = 0x6f42cf;
        VC[6] = 0xd5a5d6;
    }
}
