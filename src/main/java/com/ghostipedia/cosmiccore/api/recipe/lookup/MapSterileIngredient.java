package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

public class MapSterileIngredient extends AbstractMapIngredient {

    public final FluidStack sterileFluid;

    public MapSterileIngredient(FluidStack fluid) {
        this.sterileFluid = fluid;
    }

    @Override
    protected int hash() {
        return MapSterileIngredient.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapSterileIngredient;
    }

    @Override
    public String toString() {
        return "MapSterileIngredient{" + "fluid=" + sterileFluid + '}';
    }

    public static List<AbstractMapIngredient> convertToMapIngredient(FluidStack fluid) {
        return Collections.singletonList(new MapSterileIngredient(fluid));
    }
}
