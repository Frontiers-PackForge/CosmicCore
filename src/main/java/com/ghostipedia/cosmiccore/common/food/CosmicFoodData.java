package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmicFoodData implements INBTSerializable<CompoundTag> {

    public static final int DEFAULT_FOOD_SLOTS = 3;
    public static final int DEFAULT_BREW_SLOTS = 2;

    public List<ActiveFood> foods = new ArrayList<>();
    public List<ActiveFood> brews = new ArrayList<>();
    public int maxFoods = DEFAULT_FOOD_SLOTS;
    public int maxBrews = DEFAULT_BREW_SLOTS;

    public transient int lastDamageTick = -10000;
    private transient boolean dirty = false;
    public transient Map<ResourceLocation, Holder<Attribute>> appliedAttrMods = new HashMap<>();

    public void eat(ItemStack stack) {
        dirty = true;
        FoodDefinition def = CosmicFoodRegistry.get(stack);
        List<ActiveFood> list = def.category() == FoodCategory.BREW ? brews : foods;
        int max = def.category() == FoodCategory.BREW ? maxBrews : maxFoods;
        Item item = stack.getItem();
        int base = def.durationTicks();

        for (ActiveFood af : list) {
            if (af.item == item) {
                af.ticksLeft = Math.min(af.ticksLeft + base, base * 2);
                return;
            }
        }

        if (list.size() < max) {
            list.add(new ActiveFood(item, base));
            return;
        }

        int weakest = 0;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).ticksLeft < list.get(weakest).ticksLeft) weakest = i;
        }
        list.set(weakest, new ActiveFood(item, base));
    }

    public void tick() {
        tickList(foods);
        tickList(brews);
    }

    private void tickList(List<ActiveFood> list) {
        for (ActiveFood af : list) af.ticksLeft--;
        if (list.removeIf(af -> af.ticksLeft <= 0)) dirty = true;
    }

    public double totalHeartBonus() {
        double sum = 0;
        for (ActiveFood af : foods) sum += af.def.heartBonus();
        for (ActiveFood af : brews) sum += af.def.heartBonus();
        return sum;
    }

    public double totalRegenBonus() {
        double sum = 0;
        for (ActiveFood af : foods) sum += af.def.regenBonus();
        for (ActiveFood af : brews) sum += af.def.regenBonus();
        return sum;
    }

    public List<AttributeSpec> allActiveAttributes() {
        List<AttributeSpec> out = new ArrayList<>();
        for (ActiveFood af : foods) out.addAll(af.def.attributes());
        for (ActiveFood af : brews) out.addAll(af.def.attributes());
        return out;
    }

    public void clearActive() {
        foods.clear();
        brews.clear();
        dirty = true;
    }

    public boolean hasActive() {
        return !foods.isEmpty() || !brews.isEmpty();
    }

    public boolean consumeDirty() {
        if (!dirty) return false;
        dirty = false;
        return true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxFoods", maxFoods);
        tag.putInt("maxBrews", maxBrews);
        tag.put("foods", saveList(foods));
        tag.put("brews", saveList(brews));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        maxFoods = nbt.contains("maxFoods") ? nbt.getInt("maxFoods") : DEFAULT_FOOD_SLOTS;
        maxBrews = nbt.contains("maxBrews") ? nbt.getInt("maxBrews") : DEFAULT_BREW_SLOTS;
        foods = loadList(nbt.getList("foods", Tag.TAG_COMPOUND));
        brews = loadList(nbt.getList("brews", Tag.TAG_COMPOUND));
    }

    private static ListTag saveList(List<ActiveFood> list) {
        ListTag out = new ListTag();
        for (ActiveFood af : list) {
            CompoundTag t = new CompoundTag();
            t.putString("id", BuiltInRegistries.ITEM.getKey(af.item).toString());
            t.putInt("ticks", af.ticksLeft);
            out.add(t);
        }
        return out;
    }

    private static List<ActiveFood> loadList(ListTag list) {
        List<ActiveFood> out = new ArrayList<>();
        for (Tag element : list) {
            CompoundTag t = (CompoundTag) element;
            ResourceLocation id = ResourceLocation.tryParse(t.getString("id"));
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) continue;
            out.add(new ActiveFood(BuiltInRegistries.ITEM.get(id), t.getInt("ticks")));
        }
        return out;
    }
}
