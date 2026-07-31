package com.ghostipedia.cosmiccore.integration.emi;

import com.ghostipedia.cosmiccore.api.machine.multiblock.ITieredMultiblockPreview;
import com.ghostipedia.cosmiccore.mixin.CosmicCoreMixinPlugin;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine.DEFAULT_STRUCTURE;

public final class MultiblockPreviewSchemaCache {

    private static final int AERONAUTICS_GUARD_LIMIT = 2_048;
    private static final int ABSOLUTE_GUARD_LIMIT = 32_768;
    private static final Map<MultiblockMachineDefinition, SoftReference<PreparedSchema>> CACHE = new ConcurrentHashMap<>();
    private static final Map<MultiblockMachineDefinition, GuardedSchema> GUARDED_CACHE = new ConcurrentHashMap<>();

    private MultiblockPreviewSchemaCache() {}

    public static void clear() {
        CACHE.clear();
        GUARDED_CACHE.clear();
    }

    public static void capture(MultiblockMachineDefinition definition, Map<BlockPos, BlockInfo> blocks) {
        if (shouldGuard(blocks.size())) {
            Reference2IntMap<Block> blockCounts = new Reference2IntOpenHashMap<>();
            blocks.forEach((pos, info) -> blockCounts.mergeInt(info.getBlockState().getBlock(), 1, Integer::sum));
            GUARDED_CACHE.put(definition, new GuardedSchema(Reference2IntMaps.unmodifiable(blockCounts)));
            CACHE.remove(definition);
            return;
        }

        GUARDED_CACHE.remove(definition);
        Long2ReferenceMap<BlockState> states = new Long2ReferenceOpenHashMap<>(blocks.size());
        blocks.forEach((pos, info) -> states.put(pos.asLong(), info.getBlockState()));
        CACHE.put(definition, new SoftReference<>(new PreparedSchema(Long2ReferenceMaps.unmodifiable(states))));
    }

    public static boolean apply(MultiblockMachineDefinition definition, MultiblockSchemaInfo schemaInfo,
                                Direction frontFacing, Direction upFacing, boolean isFlipped) {
        GuardedSchema guardedSchema = GUARDED_CACHE.get(definition);
        if (guardedSchema != null) {
            schemaInfo.getBlockCounts().clear();
            schemaInfo.getBlockCounts().putAll(guardedSchema.blockCounts());
            schemaInfo.getStructureBlocks().clear();
            schemaInfo.setMapSchema(new MutableSchema());
            AbstractStructureHelper structureHelper = createStructureHelper(definition, schemaInfo);
            if (structureHelper != null) {
                ((MultiblockSchemaInfoAccessor) schemaInfo).cosmiccore$setStructureHelper(structureHelper);
            }
            return true;
        }

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

        AbstractStructureHelper structureHelper = createStructureHelper(definition, schemaInfo);
        if (structureHelper == null) {
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

    private static boolean shouldGuard(int blockCount) {
        return blockCount > ABSOLUTE_GUARD_LIMIT ||
                blockCount > AERONAUTICS_GUARD_LIMIT && ModList.get().isLoaded("aeronautics") &&
                        !CosmicCoreMixinPlugin.isAeronauticsSchemaBypassAvailable();
    }

    private static AbstractStructureHelper createStructureHelper(MultiblockMachineDefinition definition,
                                                                 MultiblockSchemaInfo schemaInfo) {
        IBlockPattern pattern = definition.getStructurePatterns().get(DEFAULT_STRUCTURE).get();
        if (pattern instanceof BlockPattern blockPattern) {
            for (int i = 0; i < blockPattern.getSlices().length; i++) {
                if (!schemaInfo.getUserSliceRepeats().containsKey(i)) {
                    schemaInfo.getUserSliceRepeats().put(i, blockPattern.getSlices()[i].getMinRepeats());
                }
            }
            return AbstractStructureHelper.blockPattern(schemaInfo.getUserSliceRepeats());
        }
        if (pattern instanceof ExpandablePattern expandablePattern) {
            if (schemaInfo.getUserDimensions().isEmpty()) {
                expandablePattern.getBoundsConstraints().apply().stream()
                        .mapToInt(Pair::left)
                        .forEach(schemaInfo.getUserDimensions()::add);
            }
            return AbstractStructureHelper.expandable(schemaInfo.getUserDimensions());
        }
        return null;
    }

    private static Direction getDefaultUpFacing(MultiblockMachineDefinition definition) {
        return switch (definition.getRotationState()) {
            case Y_AXIS -> Direction.NORTH;
            case ALL, NON_Y_AXIS, NONE -> Direction.UP;
        };
    }

    private record PreparedSchema(Long2ReferenceMap<BlockState> states) {}

    private record GuardedSchema(Reference2IntMap<Block> blockCounts) {}
}
