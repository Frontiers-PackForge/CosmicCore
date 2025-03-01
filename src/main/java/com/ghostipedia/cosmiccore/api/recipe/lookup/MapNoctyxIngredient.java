package com.ghostipedia.cosmiccore.api.recipe.lookup;

import com.ghostipedia.cosmiccore.api.noctyx.NoctyxStack;

import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public class MapNoctyxIngredient extends AbstractMapIngredient {

    @Getter
    protected NoctyxStack[] stacks;

    @Override
    protected int hash() {
        return Arrays.hashCode(stacks);
    }

    public boolean isEmpty() {
        return stacks == null || stacks.length == 0;
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MapNoctyxIngredient that)) return false;
        if (!super.equals(object)) return false;

        return Arrays.equals(stacks, that.stacks);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("MapNoctyxIngredient{ NoctyxStack[] stacks=");
        for (int i = 0; i < stacks.length; i++) {
            builder.append(stacks[i].toString());
            if (i < stacks.length - 1) {
                builder.append(", ");
            }
        }
        builder.append('}');
        return builder.toString();
    }
}
