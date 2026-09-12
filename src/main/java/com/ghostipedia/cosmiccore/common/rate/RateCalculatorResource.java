package com.ghostipedia.cosmiccore.common.rate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

record RateCalculatorResource(String kind, String id, Tag icon, long amount, int chance, int maxChance,
                              boolean perTick, boolean known, boolean expectedKnown, String chanceLogic,
                              List<Alternative> alternatives) {

    RateCalculatorResource {
        alternatives = List.copyOf(alternatives);
    }

    RateCalculatorResource add(long additional) {
        return new RateCalculatorResource(kind, id, icon, saturatedAdd(amount, additional), chance, maxChance,
                perTick, known, expectedKnown, chanceLogic, alternatives);
    }

    RateCalculatorResource uncertain() {
        return new RateCalculatorResource(kind, id, icon, amount, chance, maxChance, perTick, false, expectedKnown,
                chanceLogic, alternatives);
    }

    RateCalculatorResource resolved(Alternative alternative) {
        return new RateCalculatorResource(kind, alternative.id(), alternative.icon(), amount, chance, maxChance,
                perTick, true, expectedKnown, chanceLogic, List.of());
    }

    static RateCalculatorResource unknown(String kind, String key, long amount, int chance, int maxChance,
                                          boolean perTick, boolean expectedKnown, String chanceLogic,
                                          List<Alternative> alternatives) {
        return new RateCalculatorResource(kind, key, new CompoundTag(), amount, chance, maxChance, perTick, false,
                expectedKnown, chanceLogic, alternatives);
    }

    static RateCalculatorResource item(ItemStack stack, int chance, int maxChance, boolean perTick, boolean known,
                                       boolean expectedKnown, String chanceLogic, HolderLookup.Provider registries) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ItemStack icon = stack.copy();
        icon.setCount(1);
        return new RateCalculatorResource("item", id.toString(), icon.save(registries), stack.getCount(), chance,
                maxChance, perTick, known, expectedKnown, chanceLogic, List.of());
    }

    static RateCalculatorResource fluid(FluidStack stack, int chance, int maxChance, boolean perTick, boolean known,
                                        boolean expectedKnown, String chanceLogic, HolderLookup.Provider registries) {
        var id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        FluidStack icon = stack.copyWithAmount(1);
        return new RateCalculatorResource("fluid", id.toString(), icon.saveOptional(registries), stack.getAmount(),
                chance, maxChance, perTick, known, expectedKnown, chanceLogic, List.of());
    }

    static List<Alternative> itemAlternatives(ItemStack[] stacks, HolderLookup.Provider registries) {
        return Arrays.stream(stacks).filter(stack -> !stack.isEmpty()).map(stack -> {
            ItemStack icon = stack.copy();
            icon.setCount(1);
            return new Alternative(BuiltInRegistries.ITEM.getKey(icon.getItem()).toString(), icon.save(registries));
        }).distinct().sorted(Comparator.comparing(Alternative::id).thenComparing(value -> value.icon().toString()))
                .toList();
    }

    static List<Alternative> fluidAlternatives(FluidStack[] stacks, HolderLookup.Provider registries) {
        return Arrays.stream(stacks).filter(stack -> !stack.isEmpty()).map(stack -> {
            FluidStack icon = stack.copyWithAmount(1);
            return new Alternative(BuiltInRegistries.FLUID.getKey(icon.getFluid()).toString(),
                    icon.saveOptional(registries));
        }).distinct().sorted(Comparator.comparing(Alternative::id).thenComparing(value -> value.icon().toString()))
                .toList();
    }

    private static long saturatedAdd(long left, long right) {
        return right > 0 && left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind);
        tag.putString("id", id);
        tag.put("icon", icon.copy());
        tag.putLong("amount", amount);
        tag.putInt("chance", chance);
        tag.putInt("maxChance", maxChance);
        tag.putBoolean("perTick", perTick);
        tag.putBoolean("known", known);
        tag.putBoolean("expectedKnown", expectedKnown);
        tag.putString("chanceLogic", chanceLogic);
        ListTag choices = new ListTag();
        alternatives.forEach(alternative -> choices.add(alternative.toTag()));
        tag.put("alternatives", choices);
        return tag;
    }

    record Alternative(String id, Tag icon) {

        Alternative {
            icon = icon.copy();
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", id);
            tag.put("icon", icon.copy());
            return tag;
        }
    }
}
