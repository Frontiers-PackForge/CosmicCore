package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.function.TriFunction;

import java.util.function.Supplier;

public final class PatternMappedPartAppearance
                                               implements
                                               TriFunction<MultiblockControllerMachine, MultiblockPartMachine, Direction, BlockState> {

    private final Supplier<BlockState> fallback;

    private PatternMappedPartAppearance(Supplier<BlockState> fallback) {
        this.fallback = fallback;
    }

    public static PatternMappedPartAppearance of(Supplier<BlockState> fallback) {
        return new PatternMappedPartAppearance(fallback);
    }

    @Override
    public BlockState apply(MultiblockControllerMachine controller, MultiblockPartMachine part, Direction side) {
        if (!(controller.getDefaultStructurePattern() instanceof BlockPattern pattern)) return fallback.get();
        for (var slice : pattern.getSlices()) {
            if (slice.getMinRepeats() != slice.getMaxRepeats()) return fallback.get();
        }

        Direction front = controller.getFrontFacing();
        Direction up = controller.getUpwardsFacing();
        boolean flipped = controller.isFlipped();
        BlockPos.MutableBlockPos start = controller.getBlockPos().mutable();
        pattern.getOffset().apply(start, front, up, flipped);
        BlockPos delta = part.getBlockPos().subtract(start);
        int sliceIndex = project(delta, pattern.getDirections()[0].getRelativeFacing(front, up, flipped));
        int stringIndex = project(delta, pattern.getDirections()[1].getRelativeFacing(front, up, flipped));
        int charIndex = project(delta, pattern.getDirections()[2].getRelativeFacing(front, up, flipped));
        int[] dimensions = pattern.getDimensions();
        if (sliceIndex < 0 || sliceIndex >= dimensions[0] ||
                stringIndex < 0 || stringIndex >= dimensions[1] ||
                charIndex < 0 || charIndex >= dimensions[2]) {
            return fallback.get();
        }

        char symbol = pattern.getSlices()[sliceIndex].charAt(stringIndex, charIndex);
        var predicate = pattern.getPredicates().get(symbol);
        if (predicate == null) return fallback.get();
        for (var subPredicate : predicate.subPredicates) {
            for (BlockInfo candidate : subPredicate.getCandidates()) {
                BlockState state = candidate.getBlockState();
                if (!(state.getBlock() instanceof MetaMachineBlock) && !state.is(Blocks.AIR)) return state;
            }
        }
        return fallback.get();
    }

    private static int project(BlockPos pos, Direction direction) {
        return pos.getX() * direction.getStepX() +
                pos.getY() * direction.getStepY() +
                pos.getZ() * direction.getStepZ();
    }
}
