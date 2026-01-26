package com.ghostipedia.cosmiccore.api.recipe.ingredient;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public record SoulIngredient(SoulStack stack) implements Predicate<SoulStack> {

    public static final Codec<SoulIngredient> CODEC = SoulStack.CODEC.xmap(SoulIngredient::new, SoulIngredient::stack);

    public static SoulIngredient of(final SoulStack stack) {
        return new SoulIngredient(stack);
    }

    public static SoulIngredient of(SoulType soulType, int amount) {
        return new SoulIngredient(new SoulStack(soulType, amount));
    }

    @Override
    public boolean test(SoulStack soulStack) {
        return this.stack.type() == soulStack.type() && this.stack.amount() <= soulStack.amount();
    }

    @Override
    public @NotNull String toString() {
        return "SoulIngredient{stack=" + stack + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SoulIngredient other)) {
            return false;
        }
        return stack.equals(other.stack);
    }
}
