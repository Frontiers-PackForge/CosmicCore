package com.ghostipedia.cosmiccore.mixin.worldgen;

import com.ghostipedia.cosmiccore.mixin.accessor.CubicSplineMultipointAccessor;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Optimizes CubicSpline.Multipoint which is the single hottest worldgen code path.
 *
 * Three optimizations:
 * 1. Replace stream-based mapAll() with a plain array loop (eliminates Stream/Iterator/Spliterator allocation)
 * 2. Cache hashCode since Multipoint instances are used as HashMap keys in NoiseChunk and are immutable.
 * The record-generated hashCode recursively hashes all values (which are nested Multipoints),
 * making it O(tree_size) on every call. Caching makes subsequent calls O(1).
 * 3. Short-circuit equals() using cached hashCode - if hashes differ, objects differ.
 * Also adds reference equality check and avoids deep comparison when unnecessary.
 */
@Mixin(CubicSpline.Multipoint.class)
public abstract class CubicSplineMultipointMixin<C, I extends ToFloatFunction<C>> {

    @Shadow
    @Final
    private I coordinate;
    @Shadow
    @Final
    private float[] locations;
    @Shadow
    @Final
    private List<CubicSpline<C, I>> values;
    @Shadow
    @Final
    private float[] derivatives;
    @Shadow
    @Final
    private float minValue;
    @Shadow
    @Final
    private float maxValue;

    @Unique
    private int cosmiccore$cachedHashCode;
    @Unique
    private boolean cosmiccore$hashCached;

    /**
     * @author CosmicCore
     * @reason Replace stream-based mapAll with a plain loop to eliminate Stream/Iterator overhead.
     *         The original uses .stream().map().toList() which allocates Stream, Spliterator, and collector objects
     *         on every call. A plain ArrayList loop produces identical results with zero allocation overhead.
     */
    @Overwrite
    public CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> visitor) {
        I visited = visitor.visit(this.coordinate);
        List<CubicSpline<C, I>> mappedValues = new ArrayList<>(this.values.size());
        for (CubicSpline<C, I> value : this.values) {
            mappedValues.add(value.mapAll(visitor));
        }
        return CubicSplineMultipointAccessor.cosmiccore$create(visited, this.locations, mappedValues, this.derivatives);
    }

    /**
     * @author CosmicCore
     * @reason Cache the hashCode computation. The record-generated hashCode for Multipoint calls
     *         Objects.hashCode() on each component field. For the 'values' List field, this recursively
     *         hashes every nested CubicSpline (which may themselves be Multipoints), making each call
     *         O(tree_size). Since all fields are final, the result never changes - cache it.
     *
     *         We reproduce the exact record hashCode algorithm: for each component in declaration order,
     *         result = 31 * result + Objects.hashCode(component).
     *         For primitive float fields, Objects.hashCode autoboxes to Float.hashCode (Float.floatToIntBits).
     */
    @Overwrite
    public int hashCode() {
        if (!this.cosmiccore$hashCached) {
            // Record hashCode: for each component in order, result = 31 * result + hash(component)
            // Component order: coordinate, locations, values, derivatives, minValue, maxValue
            int result = Objects.hashCode(this.coordinate);
            result = 31 * result + Objects.hashCode(this.locations);
            result = 31 * result + Objects.hashCode(this.values);
            result = 31 * result + Objects.hashCode(this.derivatives);
            result = 31 * result + Float.hashCode(this.minValue);
            result = 31 * result + Float.hashCode(this.maxValue);
            this.cosmiccore$cachedHashCode = result;
            this.cosmiccore$hashCached = true;
        }
        return this.cosmiccore$cachedHashCode;
    }

    /**
     * @author CosmicCore
     * @reason Short-circuit equals using cached hashCode. The record-generated equals
     *         does deep structural comparison on all fields including nested Lists of
     *         CubicSplines. By checking hashCode first (O(1) when cached), we can
     *         reject most non-equal comparisons without any deep traversal.
     *         Also adds reference equality check as a fast path.
     */
    @Overwrite
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CubicSpline.Multipoint<?, ?> other)) return false;
        // Hash check: if cached hashes differ, objects are definitely not equal
        if (this.hashCode() != other.hashCode()) return false;
        // Full structural comparison (matching record equals behavior)
        return Objects.equals(this.coordinate, ((CubicSplineMultipointMixin<?, ?>) (Object) other).coordinate) &&
                Arrays.equals(this.locations, ((CubicSplineMultipointMixin<?, ?>) (Object) other).locations) &&
                Objects.equals(this.values, ((CubicSplineMultipointMixin<?, ?>) (Object) other).values) &&
                Arrays.equals(this.derivatives, ((CubicSplineMultipointMixin<?, ?>) (Object) other).derivatives) &&
                Float.compare(this.minValue, ((CubicSplineMultipointMixin<?, ?>) (Object) other).minValue) == 0 &&
                Float.compare(this.maxValue, ((CubicSplineMultipointMixin<?, ?>) (Object) other).maxValue) == 0;
    }
}
