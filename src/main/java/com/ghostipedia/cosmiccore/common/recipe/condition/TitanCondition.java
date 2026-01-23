package com.ghostipedia.cosmiccore.common.recipe.condition;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.TitanFusionReactorMachine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class TitanCondition extends RecipeCondition<TitanCondition> {

    public int tier;

    public static final Codec<TitanCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.INT.fieldOf("titan_tier").forGetter(val -> val.tier))
                    .apply(instance, TitanCondition::new));

    public static RecipeConditionType<TitanCondition> TYPE;

    public TitanCondition(boolean isReverse, int tier) {
        this.isReverse = isReverse;
        this.tier = tier;
    }

    public TitanCondition(int tier) {
        this(false, tier);
    }

    public TitanCondition() {
        this.tier = 0;
    }

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("titan_condition",
                new RecipeConditionType<>(TitanCondition::new, TitanCondition.CODEC));
    }

    @Override
    public RecipeConditionType<TitanCondition> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("cosmiccore.recipe.condition.titan.tooltip", tier);
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!(recipeLogic.getMachine() instanceof TitanFusionReactorMachine titanReactor)) return false;
        return titanReactor.getReactorTier() >= tier;
    }

    @Override
    public TitanCondition createTemplate() {
        return new TitanCondition();
    }
}
