package com.ghostipedia.cosmiccore.integration.emi;

import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class TrackedStructureMap extends HashMap<BlockPos, BlockInfo> {

    private final Map<BasePredicate, PredicateCounts> counts = new IdentityHashMap<>();

    public TrackedStructureMap(Map<BlockPos, BlockInfo> initialEntries) {
        putAll(initialEntries);
    }

    public int countGlobal(BasePredicate predicate) {
        return counts.computeIfAbsent(predicate, this::createCounts).total;
    }

    public int countInLayer(BasePredicate predicate, Direction.Axis axis, int offset) {
        return counts.computeIfAbsent(predicate, this::createCounts).layers[axis.ordinal()].get(offset);
    }

    @Override
    public BlockInfo put(BlockPos key, BlockInfo value) {
        boolean contained = containsKey(key);
        BlockInfo previous = super.put(key, value);
        if (contained) {
            updateCounts(key, previous, -1);
        }
        updateCounts(key, value, 1);
        return previous;
    }

    @Override
    public void putAll(Map<? extends BlockPos, ? extends BlockInfo> map) {
        map.forEach(this::put);
    }

    @Override
    public BlockInfo remove(Object key) {
        if (!(key instanceof BlockPos pos) || !containsKey(key)) {
            return null;
        }
        BlockInfo previous = super.remove(key);
        updateCounts(pos, previous, -1);
        return previous;
    }

    @Override
    public boolean remove(Object key, Object value) {
        BlockInfo current = get(key);
        if (!containsKey(key) || !java.util.Objects.equals(current, value)) {
            return false;
        }
        remove(key);
        return true;
    }

    @Override
    public void clear() {
        super.clear();
        counts.clear();
    }

    private PredicateCounts createCounts(BasePredicate predicate) {
        PredicateCounts result = new PredicateCounts();
        forEach((pos, info) -> {
            if (predicate.getCandidates().contains(info)) {
                result.add(pos, 1);
            }
        });
        return result;
    }

    private void updateCounts(BlockPos pos, BlockInfo info, int amount) {
        counts.forEach((predicate, value) -> {
            if (predicate.getCandidates().contains(info)) {
                value.add(pos, amount);
            }
        });
    }

    private static final class PredicateCounts {

        private final Int2IntOpenHashMap[] layers = {
                new Int2IntOpenHashMap(),
                new Int2IntOpenHashMap(),
                new Int2IntOpenHashMap()
        };
        private int total;

        private void add(BlockPos pos, int amount) {
            total += amount;
            layers[Direction.Axis.X.ordinal()].addTo(pos.getX(), amount);
            layers[Direction.Axis.Y.ordinal()].addTo(pos.getY(), amount);
            layers[Direction.Axis.Z.ordinal()].addTo(pos.getZ(), amount);
        }
    }
}
