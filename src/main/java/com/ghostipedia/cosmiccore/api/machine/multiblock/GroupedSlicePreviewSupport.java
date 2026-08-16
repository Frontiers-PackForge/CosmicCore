package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.BasicSliceGroupAccessor;
import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.BasicSliceStrategyAccessor;

import com.gregtechceu.gtceu.api.multiblock.pattern.BasicSliceStrategy;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternSlice;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.ArrayList;
import java.util.List;

public final class GroupedSlicePreviewSupport {

    private GroupedSlicePreviewSupport() {}

    public static int repeatKey(int groupIndex) {
        return -groupIndex - 1;
    }

    public static List<Group> variableGroups(BlockPattern pattern) {
        if (!(pattern.getSliceStrategy() instanceof BasicSliceStrategy strategy)) return List.of();
        List<?> groups = ((BasicSliceStrategyAccessor) strategy).cosmiccore$getMultiblockSlices();
        List<Group> result = new ArrayList<>();
        for (int index = 0; index < groups.size(); index++) {
            BasicSliceGroupAccessor group = (BasicSliceGroupAccessor) groups.get(index);
            if (group.cosmiccore$getMinRepeats() == group.cosmiccore$getMaxRepeats()) continue;
            result.add(new Group(index, group.cosmiccore$getMinRepeats(), group.cosmiccore$getMaxRepeats(),
                    group.cosmiccore$getStartInclusive(), group.cosmiccore$getEndExclusive()));
        }
        return result;
    }

    public static char[][][] flatten(BlockPattern pattern, Int2IntMap sliceRepeats) {
        if (!(pattern.getSliceStrategy() instanceof BasicSliceStrategy strategy)) return null;
        List<?> rawGroups = ((BasicSliceStrategyAccessor) strategy).cosmiccore$getMultiblockSlices();
        if (rawGroups.stream().map(BasicSliceGroupAccessor.class::cast)
                .noneMatch(group -> group.cosmiccore$getMinRepeats() != 1 || group.cosmiccore$getMaxRepeats() != 1)) {
            return null;
        }

        PatternSlice[] slices = pattern.getSlices();
        List<char[][]> flattened = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < rawGroups.size(); groupIndex++) {
            BasicSliceGroupAccessor group = (BasicSliceGroupAccessor) rawGroups.get(groupIndex);
            int groupRepeats = sliceRepeats.getOrDefault(repeatKey(groupIndex), group.cosmiccore$getMinRepeats());
            groupRepeats = Math.max(group.cosmiccore$getMinRepeats(),
                    Math.min(group.cosmiccore$getMaxRepeats(), groupRepeats));
            for (int groupRepeat = 0; groupRepeat < groupRepeats; groupRepeat++) {
                for (int sliceIndex = group.cosmiccore$getStartInclusive(); sliceIndex <
                        group.cosmiccore$getEndExclusive(); sliceIndex++) {
                    PatternSlice slice = slices[sliceIndex];
                    int repeats = sliceRepeats.getOrDefault(sliceIndex, slice.getMinRepeats());
                    repeats = Math.max(slice.getMinRepeats(), Math.min(slice.getMaxRepeats(), repeats));
                    for (int repeat = 0; repeat < repeats; repeat++) {
                        flattened.add(slice.getPattern());
                    }
                }
            }
        }
        return flattened.toArray(char[][][]::new);
    }

    public record Group(int index, int minRepeats, int maxRepeats, int startInclusive, int endExclusive) {}
}
