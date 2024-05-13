package com.ghostipedia.cosmiccore.api;

import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CosmicCoreAPI {
    public static final Map<IMagnetType, Supplier<MagnetBlock>> MAGNET_COILS = new HashMap<>();
}
