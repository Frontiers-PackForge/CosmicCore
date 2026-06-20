package com.ghostipedia.cosmiccore.mixin.accessor;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(CubicSpline.Multipoint.class)
public interface CubicSplineMultipointAccessor<C, I extends ToFloatFunction<C>> {

    @Invoker("create")
    static <C, I extends ToFloatFunction<C>> CubicSpline.Multipoint<C, I> cosmiccore$create(
                                                                                            I coordinate,
                                                                                            float[] locations,
                                                                                            List<CubicSpline<C, I>> values,
                                                                                            float[] derivatives) {
        throw new AssertionError();
    }
}
