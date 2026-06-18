package com.ghostipedia.cosmiccore.common.recipe.condition;

import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.LinkedMultiblockHelper;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Recipe condition that requires a linked partner in a specific dimension
 * to have a specific fluid in its input hatches.
 * <p>
 * Use cases:
 * - "Requires partner in Sun Orbit with Solar Plasma"
 * - "Requires partner in Deep Below with Molten Core fluid"
 */
public class LinkedPartnerDimensionFluidCondition extends RecipeCondition<LinkedPartnerDimensionFluidCondition> {

    public ResourceLocation dimension;
    public ResourceLocation fluidId;
    public int minAmount;

    public static final Codec<LinkedPartnerDimensionFluidCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("dimension").forGetter(val -> val.dimension))
                    .and(ResourceLocation.CODEC.fieldOf("fluid").forGetter(val -> val.fluidId))
                    .and(Codec.INT.optionalFieldOf("amount", 1000).forGetter(val -> val.minAmount))
                    .apply(instance, LinkedPartnerDimensionFluidCondition::new));

    public static RecipeConditionType<LinkedPartnerDimensionFluidCondition> TYPE;

    public LinkedPartnerDimensionFluidCondition(boolean isReverse, ResourceLocation dimension, ResourceLocation fluidId,
                                                int minAmount) {
        this.isReverse = isReverse;
        this.dimension = dimension;
        this.fluidId = fluidId;
        this.minAmount = minAmount;
    }

    public LinkedPartnerDimensionFluidCondition(ResourceLocation dimension, ResourceLocation fluidId, int minAmount) {
        this(false, dimension, fluidId, minAmount);
    }

    public LinkedPartnerDimensionFluidCondition(String dimension, Fluid fluid, int minAmount) {
        this(false, ResourceLocation.fromNamespaceAndPath(dimension), BuiltInRegistries.FLUID.getKey(fluid), minAmount);
    }

    public LinkedPartnerDimensionFluidCondition(String dimension, Fluid fluid) {
        this(dimension, fluid, 1000);
    }

    public LinkedPartnerDimensionFluidCondition() {
        this.dimension = ResourceLocation.parse("minecraft:overworld");
        this.fluidId = ResourceLocation.parse("minecraft:water");
        this.minAmount = 1000;
    }

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("linked_partner_dimension_fluid",
                new RecipeConditionType<>(LinkedPartnerDimensionFluidCondition::new,
                        LinkedPartnerDimensionFluidCondition.CODEC));
    }

    @Override
    public RecipeConditionType<LinkedPartnerDimensionFluidCondition> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
        String fluidName = new FluidStack(fluid, 1000).getDisplayName().getString();
        return Component.translatable("cosmiccore.recipe.condition.linked_partner_dimension_fluid.tooltip",
                minAmount, fluidName, dimension.toString());
    }

    @Override
    protected boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (!(recipeLogic.getMachine() instanceof LinkedWorkableElectricMultiblockMachine linkedMachine)) {
            return false;
        }

        if (!(linkedMachine.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        UUID owner = linkedMachine.getTeamUUID();
        if (owner == null) return false;

        GlobalPos myPos = linkedMachine.getGlobalPos();
        Fluid targetFluid = BuiltInRegistries.FLUID.get(fluidId);

        // Check each partner in the required dimension
        for (GlobalPos partner : linkedMachine.getLinkedPartners()) {
            if (!partner.dimension().location().equals(dimension)) {
                continue;
            }

            // Check if this partner has the required fluid
            boolean hasFluid = LinkedMultiblockHelper.partnerHasFluid(
                    serverLevel.getServer(),
                    owner,
                    myPos,
                    partner,
                    (FluidStack stack) -> stack.getFluid().isSame(targetFluid) && stack.getAmount() >= minAmount);

            if (hasFluid) {
                return true;
            }
        }

        return false;
    }

    @Override
    public LinkedPartnerDimensionFluidCondition createTemplate() {
        return new LinkedPartnerDimensionFluidCondition();
    }
}
