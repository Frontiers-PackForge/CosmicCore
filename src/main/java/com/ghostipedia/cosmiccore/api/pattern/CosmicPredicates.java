package com.ghostipedia.cosmiccore.api.pattern;

import com.ghostipedia.cosmiccore.api.CosmicCoreAPI;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
import com.ghostipedia.cosmiccore.common.block.MagnetBlock;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;

public class CosmicPredicates {
    public static TraceabilityPredicate magnetCoils() {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (Map.Entry<IMagnetType, Supplier<MagnetBlock>> entry : CosmicCoreAPI.MAGNET_COILS.entrySet()) {
                if (blockState.is(entry.getValue().get())) {
                    var stats = entry.getKey();
                    Object currentMagnet = blockWorldState.getMatchContext().getOrPut("MagnetType", stats);
                    if (!currentMagnet.equals(stats)) {
                        blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.coils"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> CosmicCoreAPI.MAGNET_COILS.entrySet().stream()
                // sort to make autogenerated jei previews not pick random coils each game load
                .sorted(Comparator.comparingInt(value -> value.getKey().getMagnetFieldCapacity()))
                .map(coil -> BlockInfo.fromBlockState(coil.getValue().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }
    public static void init() {
    }
}