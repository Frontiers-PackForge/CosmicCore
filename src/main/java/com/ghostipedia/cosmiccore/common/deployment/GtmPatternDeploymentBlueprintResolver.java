package com.ghostipedia.cosmiccore.common.deployment;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.HashMap;
import java.util.Map;

public final class GtmPatternDeploymentBlueprintResolver {

    private GtmPatternDeploymentBlueprintResolver() {}

    public static LeylineDeploymentBlueprint resolve(ResourceLocation blueprintId,
                                                     MultiblockMachineDefinition definition,
                                                     Direction frontFacing, Direction upFacing, boolean flipped) {
        IBlockPattern pattern = definition.getStructurePatterns().get(MultiblockControllerMachine.DEFAULT_STRUCTURE)
                .get();
        AbstractStructureHelper structureHelper = createStructureHelper(pattern);
        Map<BlockPos, BlockInfo> populated = new HashMap<>();
        // GTM Will always default to min repeatable counts, for the sake of Leylines we can just assume min works
        // Unlikely to ever stick a expandable multi like this in a leyline package, but uh, fuck whatever ig.
        structureHelper.populate(populated, pattern, null, frontFacing, upFacing, flipped);
        BlockPos controller = populated.entrySet().stream()
                .filter(entry -> entry.getValue().getBlockState().getBlock() == definition.getBlock())
                .map(Map.Entry::getKey)
                .reduce((first, second) -> {
                    throw new IllegalStateException("Blueprint contains multiple controllers");
                })
                .orElseThrow(() -> new IllegalStateException("Blueprint has no controller"));
        Map<BlockPos, BlockState> states = new HashMap<>();
        populated.forEach((pos, info) -> states.put(pos, info.getBlockState()));
        return LeylineDeploymentBlueprint.fromPopulated(blueprintId, controller, states);
    }

    private static AbstractStructureHelper createStructureHelper(IBlockPattern pattern) {
        if (pattern instanceof BlockPattern blockPattern) {
            Int2IntArrayMap repeats = new Int2IntArrayMap();
            for (int index = 0; index < blockPattern.getSlices().length; index++) {
                repeats.put(index, blockPattern.getSlices()[index].getMinRepeats());
            }
            return AbstractStructureHelper.blockPattern(repeats);
        }
        if (pattern instanceof ExpandablePattern expandablePattern) {
            IntArrayList dimensions = new IntArrayList();
            if (expandablePattern.getBoundsConstraints() != null) {
                expandablePattern.getBoundsConstraints().apply().stream().mapToInt(Pair::left)
                        .forEach(dimensions::add);
            }
            return AbstractStructureHelper.expandable(dimensions);
        }
        throw new IllegalArgumentException("Unsupported multiblock pattern type");
    }
}
