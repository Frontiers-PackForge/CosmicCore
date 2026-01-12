package com.ghostipedia.cosmiccore.common.recipe.condition;

import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Recipe condition that requires the machine to have linked partners.
 * <p>
 * Can be configured to require:
 * - A minimum number of linked partners
 * - At least one partner to be formed (structure valid)
 * - At least one partner to be actively working (running a recipe)
 */
public class LinkedPartnerCondition extends RecipeCondition {

    /** Minimum number of linked partners required */
    public int minPartners;
    /** If true, at least one partner must have a valid structure */
    public boolean requireFormed;
    /** If true, at least one partner must be actively running a recipe */
    public boolean requireWorking;

    public static final Codec<LinkedPartnerCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.INT.optionalFieldOf("min_partners", 1).forGetter(val -> val.minPartners))
                    .and(Codec.BOOL.optionalFieldOf("require_formed", false).forGetter(val -> val.requireFormed))
                    .and(Codec.BOOL.optionalFieldOf("require_working", false).forGetter(val -> val.requireWorking))
                    .apply(instance, LinkedPartnerCondition::new));

    public static RecipeConditionType<LinkedPartnerCondition> TYPE;

    public LinkedPartnerCondition(boolean isReverse, int minPartners, boolean requireFormed, boolean requireWorking) {
        this.isReverse = isReverse;
        this.minPartners = minPartners;
        this.requireFormed = requireFormed;
        this.requireWorking = requireWorking;
    }

    public LinkedPartnerCondition(int minPartners, boolean requireFormed, boolean requireWorking) {
        this(false, minPartners, requireFormed, requireWorking);
    }

    public LinkedPartnerCondition(int minPartners) {
        this(false, minPartners, false, false);
    }

    public LinkedPartnerCondition() {
        this(1);
    }

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("linked_partner",
                new RecipeConditionType<>(LinkedPartnerCondition::new, LinkedPartnerCondition.CODEC));
    }

    @Override
    public RecipeConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        if (requireWorking) {
            return Component.translatable("cosmiccore.recipe.condition.linked_partner.working", minPartners);
        } else if (requireFormed) {
            return Component.translatable("cosmiccore.recipe.condition.linked_partner.formed", minPartners);
        } else {
            return Component.translatable("cosmiccore.recipe.condition.linked_partner.tooltip", minPartners);
        }
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!(recipeLogic.getMachine() instanceof LinkedWorkableElectricMultiblockMachine linkedMachine)) {
            return false;
        }

        // Check minimum partner count
        int partnerCount = linkedMachine.getLinkedPartners().size();
        if (partnerCount < minPartners) {
            return false;
        }

        // Check if any partner needs to be formed
        if (requireFormed) {
            if (linkedMachine.countFormedPartners() < 1) {
                return false;
            }
        }

        // Check if any partner needs to be working
        if (requireWorking) {
            if (!linkedMachine.anyPartnerWorking()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new LinkedPartnerCondition();
    }
}
