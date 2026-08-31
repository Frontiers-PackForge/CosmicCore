package com.ghostipedia.cosmiccore.common.recipe.ingredient;

import com.ghostipedia.cosmiccore.common.vitae.EnderIOSpawnerResolver;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public record SoulEntityIngredient(ResourceLocation entity) implements ICustomIngredient {

    public static final MapCodec<SoulEntityIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(ResourceLocation.CODEC.fieldOf("entity").forGetter(SoulEntityIngredient::entity))
            .apply(instance, SoulEntityIngredient::new));

    public static SizedIngredient of(String entity) {
        return new SizedIngredient(new SoulEntityIngredient(ResourceLocation.parse(entity)).toVanilla(), 1);
    }

    @Override
    public boolean test(ItemStack stack) {
        return EnderIOSpawnerResolver.resolveSoulVial(stack).filter(entity::equals).isPresent();
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(EnderIOSpawnerResolver.createFilledSoulVial(entity));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return CosmicIngredientTypes.SOUL_ENTITY.get();
    }
}
