package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockPreview;
import com.ghostipedia.cosmiccore.mixin.gtfix.emi.accessor.MultiblockSchemaInfoAccessor;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public final class MultiblockPreviewSchemaCache {

    private static final Map<MultiblockMachineDefinition, SoftReference<PreparedSchema>> CACHE = new ConcurrentHashMap<>();

    private MultiblockPreviewSchemaCache() {}

    public static void clear() {
        CACHE.clear();
    }

    public static void capture(MultiblockMachineDefinition definition, Map<BlockPos, BlockInfo> blocks) {
        Long2ReferenceMap<BlockState> states = new Long2ReferenceOpenHashMap<>(blocks.size());
        blocks.forEach((pos, info) -> states.put(pos.asLong(), info.getBlockState()));
        CACHE.put(definition, new SoftReference<>(new PreparedSchema(Long2ReferenceMaps.unmodifiable(states))));
    }

    public static boolean apply(MultiblockMachineDefinition definition, MultiblockSchemaInfo schemaInfo,
                                Direction frontFacing, Direction upFacing, boolean isFlipped) {
        if (schemaInfo instanceof ITieredMultiblockPreview preview && preview.cosmiccore$getPreviewTier() != 0) {
            return false;
        }
        if (schemaInfo.getMapSchema() != null || schemaInfo.getStructureHelper() != null ||
                !schemaInfo.getBlockCounts().isEmpty() || !schemaInfo.getStructureBlocks().isEmpty() ||
                !schemaInfo.getUserGlobalBlockPreferences().isEmpty() ||
                !schemaInfo.getUserSliceRepeats().isEmpty() || !schemaInfo.getUserDimensions().isEmpty() ||
                isFlipped || frontFacing != definition.getRotationState().defaultDirection ||
                upFacing != getDefaultUpFacing(definition)) {
            return false;
        }

        SoftReference<PreparedSchema> reference = CACHE.get(definition);
        PreparedSchema preparedSchema = reference == null ? null : reference.get();
        if (preparedSchema == null) {
            if (reference != null) {
                CACHE.remove(definition, reference);
            }
            return false;
        }

        IBlockPattern pattern = definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();
        AbstractStructureHelper structureHelper;
        if (pattern instanceof BlockPattern blockPattern) {
            for (int i = 0; i < blockPattern.getSlices().length; i++) {
                schemaInfo.getUserSliceRepeats().put(i, blockPattern.getSlices()[i].getMinRepeats());
            }
            structureHelper = AbstractStructureHelper.blockPattern(schemaInfo.getUserSliceRepeats());
        } else if (pattern instanceof ExpandablePattern expandablePattern) {
            expandablePattern.getBoundsConstraints().apply().stream()
                    .mapToInt(Pair::left)
                    .forEach(schemaInfo.getUserDimensions()::add);
            structureHelper = AbstractStructureHelper.expandable(schemaInfo.getUserDimensions());
        } else {
            return false;
        }

        Map<BlockPos, BlockInfo> structureBlocks = new HashMap<>(preparedSchema.states().size());
        preparedSchema.states().long2ReferenceEntrySet().forEach(entry -> {
            BlockState state = entry.getValue();
            structureBlocks.put(BlockPos.of(entry.getLongKey()), BlockInfo.fromBlockState(state));
            schemaInfo.getBlockCounts().mergeInt(state.getBlock(), 1, Integer::sum);
        });

        schemaInfo.setMapSchema(new MutableSchema(preparedSchema.states()));
        schemaInfo.getStructureBlocks().putAll(structureBlocks);
        ((MultiblockSchemaInfoAccessor) schemaInfo).cosmiccore$setStructureHelper(structureHelper);
        return true;
    }

    private static Direction getDefaultUpFacing(MultiblockMachineDefinition definition) {
        return switch (definition.getRotationState()) {
            case Y_AXIS -> Direction.NORTH;
            case ALL, NON_Y_AXIS, NONE -> Direction.UP;
        };
    }

    private record PreparedSchema(Long2ReferenceMap<BlockState> states) {}
}
