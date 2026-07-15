package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

final class DriftweedPatchFeature extends Feature<NoneFeatureConfiguration> {

    private static final int PLACEMENT_ATTEMPTS = 10;
    private static final int HORIZONTAL_SPREAD = 5;

    DriftweedPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placed = false;
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(HORIZONTAL_SPREAD + 1) -
                    random.nextInt(HORIZONTAL_SPREAD + 1);
            int z = origin.getZ() + random.nextInt(HORIZONTAL_SPREAD + 1) -
                    random.nextInt(HORIZONTAL_SPREAD + 1);
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
            BlockPos rootPos = new BlockPos(x, y, z);
            if (!level.getFluidState(rootPos).isSourceOfType(Fluids.WATER)) continue;
            if (DriftweedRootBlock.placeColumnToSurface(level, rootPos, 2)) placed = true;
        }
        return placed;
    }
}
