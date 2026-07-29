package com.ghostipedia.cosmiccore.common.machine.multiblock.tier;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockMachine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TieredMultiblockTerminal {

    private TieredMultiblockTerminal() {}

    public static boolean build(Level level, MultiblockControllerMachine controller,
                                ITieredMultiblockMachine tiered, Map<BlockPos, BlockState> requestedPreferences) {
        IBlockPattern pattern = TieredMultiblockPatterns.pattern(controller.getDefinition(),
                tiered.getStructureTier());
        AbstractStructureHelper helper;
        if (pattern instanceof BlockPattern blockPattern) {
            Int2IntArrayMap repeats = new Int2IntArrayMap();
            for (int index = 0; index < blockPattern.getSlices().length; index++) {
                repeats.put(index, blockPattern.getSlices()[index].getMinRepeats());
            }
            helper = AbstractStructureHelper.blockPattern(repeats);
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            IntArrayList dimensions = new IntArrayList();
            expandablePattern.getBoundsConstraints().apply().stream()
                    .mapToInt(Pair::left)
                    .forEach(dimensions::add);
            helper = AbstractStructureHelper.expandable(dimensions);
        } else {
            return false;
        }

        Map<BlockPos, BlockInfo> canonicalStructure = new HashMap<>();
        try {
            helper.populate(canonicalStructure, pattern, new Long2ObjectOpenHashMap<>(), controller.getFrontFacing(),
                    controller.getUpwardsFacing(), controller.isFlipped());
        } catch (RuntimeException ignored) {
            return false;
        }
        if (!requestedPreferences.isEmpty() &&
                !canonicalStructure.keySet().equals(requestedPreferences.keySet())) {
            return false;
        }

        Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>();
        Long2ObjectOpenHashMap<BlockInfo> preferences = new Long2ObjectOpenHashMap<>();
        if (pattern instanceof BlockPattern) {
            for (var entry : requestedPreferences.entrySet()) {
                PatternPredicate predicate = helper.getPredicateFromPos(pattern, entry.getKey(),
                        controller.getFrontFacing(), controller.getUpwardsFacing(), controller.isFlipped());
                List<BlockInfo> candidates = predicate.subPredicates.stream()
                        .flatMap(basePredicate -> basePredicate.candidates.stream())
                        .distinct()
                        .toList();
                BlockInfo requested = BlockInfo.fromBlockState(entry.getValue());
                BlockInfo canonical = candidates.stream()
                        .filter(requested::equals)
                        .findFirst()
                        .orElse(null);
                if (canonical == null && entry.getValue().getBlock() instanceof MetaMachineBlock) {
                    List<BlockInfo> sameBlock = candidates.stream()
                            .filter(candidate -> candidate.getBlockState().getBlock() == entry.getValue().getBlock())
                            .toList();
                    if (sameBlock.size() == 1) canonical = sameBlock.getFirst();
                }
                if (canonical == null) return false;
                preferences.put(entry.getKey().asLong(), canonical);
            }
        }
        if (requestedPreferences.isEmpty()) {
            structureBlocks.putAll(canonicalStructure);
        } else {
            if (pattern instanceof BlockPattern blockPattern &&
                    !satisfiesGlobalMinimums(blockPattern, preferences)) {
                return false;
            }
            try {
                helper.populate(structureBlocks, pattern, preferences, controller.getFrontFacing(),
                        controller.getUpwardsFacing(), controller.isFlipped());
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        if (structureBlocks.isEmpty()) return false;

        BlockPos patternControllerPos = structureBlocks.entrySet().stream()
                .filter(entry -> entry.getValue().getBlockState().getBlock() == controller.getDefinition().getBlock())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (patternControllerPos == null) return false;
        BlockPos controllerOffset = controller.getBlockPos().offset(patternControllerPos.multiply(-1));
        structureBlocks.forEach((pos, info) -> level.setBlockAndUpdate(
                pos.offset(controllerOffset), info.getBlockState()));
        controller.getDefaultPatternState().getCache().clear();
        controller.getDefaultPatternState().setShouldUpdate(true);
        controller.getDefaultPatternState().setState(
                com.gregtechceu.gtceu.api.multiblock.pattern.PatternState.CheckState.UNINITIALIZED);
        controller.checkAndFormStructure();
        return true;
    }

    private static boolean satisfiesGlobalMinimums(BlockPattern pattern,
                                                   Long2ObjectOpenHashMap<BlockInfo> preferences) {
        Set<BasePredicate> checked = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (PatternPredicate predicate : pattern.getPredicates().values()) {
            for (BasePredicate basePredicate : predicate.subPredicates) {
                if (!checked.add(basePredicate) || basePredicate.minCount <= 0) continue;
                long count = preferences.values().stream()
                        .filter(basePredicate.candidates::contains)
                        .count();
                if (count < basePredicate.minCount) return false;
            }
        }
        return true;
    }
}
