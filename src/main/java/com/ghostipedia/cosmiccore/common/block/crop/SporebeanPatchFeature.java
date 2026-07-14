package com.ghostipedia.cosmiccore.common.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

final class SporebeanPatchFeature extends Feature<NoneFeatureConfiguration> {

    SporebeanPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placed = false;
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = origin.getX() + random.nextInt(15) - 7;
            int z = origin.getZ() + random.nextInt(15) - 7;
            int startY = origin.getY() - 3;
            for (int offsetY = 0; offsetY < 15; offsetY++) {
                boolean placedThisAttempt = false;
                BlockPos supportPos = new BlockPos(x, startY + offsetY, z);
                if (!level.getBlockState(supportPos).is(CropBlockTags.SPOREBEAN_SUPPORTS)) continue;
                Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                for (int side = 0; side < 4; side++) {
                    BlockPos cropPos = supportPos.relative(direction);
                    BlockState cropState = CosmicCrops.SPOREBEAN_CROP.getDefaultState()
                            .setValue(SporebeanCropBlock.FACING, direction)
                            .setValue(HarvestableCropBlock.AGE, random.nextInt(4));
                    if (level.isEmptyBlock(cropPos) && cropState.canSurvive(level, cropPos)) {
                        level.setBlock(cropPos, cropState, 2);
                        placed = true;
                        placedThisAttempt = true;
                        break;
                    }
                    direction = direction.getClockWise();
                }
                if (placedThisAttempt && random.nextBoolean()) break;
            }
        }
        return placed;
    }
}
