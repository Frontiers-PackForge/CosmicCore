package com.ghostipedia.cosmiccore.common.recipe.condition;

import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Recipe condition that requires a linked partner to be in a specific dimension.
 * <p>
 * Use cases:
 * - "Requires partner in Sun Orbit" for solar plasma collection
 * - "Requires partner in The Deep Below" for mining operations
 * - "Requires partner in Moon" for low-gravity processing
 */
public class LinkedPartnerDimensionCondition extends RecipeCondition {

    /** The dimension the partner must be in */
    public ResourceLocation dimension;

    public static final Codec<LinkedPartnerDimensionCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("dimension").forGetter(val -> val.dimension))
                    .apply(instance, LinkedPartnerDimensionCondition::new));

    public static RecipeConditionType<LinkedPartnerDimensionCondition> TYPE;

    public LinkedPartnerDimensionCondition(boolean isReverse, ResourceLocation dimension) {
        this.isReverse = isReverse;
        this.dimension = dimension;
    }

    public LinkedPartnerDimensionCondition(ResourceLocation dimension) {
        this(false, dimension);
    }

    public LinkedPartnerDimensionCondition(String dimension) {
        this(false, new ResourceLocation(dimension));
    }

    public LinkedPartnerDimensionCondition() {
        this.dimension = new ResourceLocation("minecraft:overworld");
    }

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("linked_partner_dimension",
                new RecipeConditionType<>(LinkedPartnerDimensionCondition::new, LinkedPartnerDimensionCondition.CODEC));
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("cosmiccore.recipe.condition.linked_partner_dimension.tooltip",
                dimension.toString());
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!(recipeLogic.getMachine() instanceof LinkedWorkableElectricMultiblockMachine linkedMachine)) {
            return false;
        }

        // Check if any linked partner is in the required dimension
        for (GlobalPos partner : linkedMachine.getLinkedPartners()) {
            if (partner.dimension().location().equals(dimension)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new LinkedPartnerDimensionCondition();
    }
}
