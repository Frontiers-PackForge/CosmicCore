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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Recipe condition that requires a linked partner in a specific dimension
 * to have a specific item in its input hatches.
 * <p>
 * Use cases:
 * - "Requires partner in Sun Orbit with Solar Collector item"
 * - "Requires partner in Moon with Helium-3 canister"
 */
public class LinkedPartnerDimensionItemCondition extends RecipeCondition<LinkedPartnerDimensionItemCondition> {

    public ResourceLocation dimension;
    public ResourceLocation itemId;
    public int minCount;

    public static final Codec<LinkedPartnerDimensionItemCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(ResourceLocation.CODEC.fieldOf("dimension").forGetter(val -> val.dimension))
                    .and(ResourceLocation.CODEC.fieldOf("item").forGetter(val -> val.itemId))
                    .and(Codec.INT.optionalFieldOf("count", 1).forGetter(val -> val.minCount))
                    .apply(instance, LinkedPartnerDimensionItemCondition::new));

    public static RecipeConditionType<LinkedPartnerDimensionItemCondition> TYPE;

    public LinkedPartnerDimensionItemCondition(boolean isReverse, ResourceLocation dimension, ResourceLocation itemId,
                                               int minCount) {
        this.isReverse = isReverse;
        this.dimension = dimension;
        this.itemId = itemId;
        this.minCount = minCount;
    }

    public LinkedPartnerDimensionItemCondition(ResourceLocation dimension, ResourceLocation itemId, int minCount) {
        this(false, dimension, itemId, minCount);
    }

    public LinkedPartnerDimensionItemCondition(String dimension, Item item, int minCount) {
        this(false, ResourceLocation.parse(dimension), BuiltInRegistries.ITEM.getKey(item), minCount);
    }

    public LinkedPartnerDimensionItemCondition(String dimension, Item item) {
        this(dimension, item, 1);
    }

    public LinkedPartnerDimensionItemCondition() {
        this.dimension = ResourceLocation.parse("minecraft:overworld");
        this.itemId = ResourceLocation.parse("minecraft:stone");
        this.minCount = 1;
    }

    public static void register() {
        TYPE = GTRegistries.RECIPE_CONDITIONS.register("linked_partner_dimension_item",
                new RecipeConditionType<>(LinkedPartnerDimensionItemCondition::new,
                        LinkedPartnerDimensionItemCondition.CODEC));
    }

    @Override
    public RecipeConditionType<LinkedPartnerDimensionItemCondition> getType() {
        return TYPE;
    }

    @Override
    public Component getTooltips() {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        String itemName = item.getDescription().getString();
        return Component.translatable("cosmiccore.recipe.condition.linked_partner_dimension_item.tooltip",
                minCount, itemName, dimension.toString());
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
        Item targetItem = BuiltInRegistries.ITEM.get(itemId);

        // Check each partner in the required dimension
        for (GlobalPos partner : linkedMachine.getLinkedPartners()) {
            if (!partner.dimension().location().equals(dimension)) {
                continue;
            }

            // Check if this partner has the required item
            boolean hasItem = LinkedMultiblockHelper.partnerHasItem(
                    serverLevel.getServer(),
                    owner,
                    myPos,
                    partner,
                    (ItemStack stack) -> stack.is(targetItem) && stack.getCount() >= minCount);

            if (hasItem) {
                return true;
            }
        }

        return false;
    }

    @Override
    public LinkedPartnerDimensionItemCondition createTemplate() {
        return new LinkedPartnerDimensionItemCondition();
    }
}
