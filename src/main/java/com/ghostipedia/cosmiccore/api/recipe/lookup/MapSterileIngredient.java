package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class MapSterileIngredient extends AbstractMapIngredient {

    public final FluidStack sterileFluid;

    public MapSterileIngredient(FluidStack fluid) {
        this.sterileFluid = fluid;
    }

    @Override
    protected int hash() {
        return sterileFluid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MapSterileIngredient other)) return false;
        return other.sterileFluid.equals(this.sterileFluid);
    }

    @Override
    public String toString() {
        return "MapSterileIngredient{" + "fluid=" + sterileFluid + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(FluidStack fluid) {
        return Collections.singletonList(new MapSterileIngredient(fluid));
    }
}
