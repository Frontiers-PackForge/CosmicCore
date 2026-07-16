package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;

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

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class CosmicFoodData implements INBTSerializable<CompoundTag> {

    public static final int DEFAULT_FOOD_SLOTS = 3;
    public static final int DEFAULT_BREW_SLOTS = 2;

    public List<ActiveFood> foods = new ArrayList<>();
    public List<ActiveFood> brews = new ArrayList<>();
    public int maxFoods = DEFAULT_FOOD_SLOTS;
    public int maxBrews = DEFAULT_BREW_SLOTS;
    @Nullable
    public FoodMemory memory;
    public List<CookbookPage> cookbook = new ArrayList<>();
    public long lastPageDay = -1;
    public long lastPageTick = -1;
    public List<SignatureMeal> signatures = new ArrayList<>();
    public Map<String, Integer> mealDays = new HashMap<>();
    public Map<String, Long> mealLastDay = new HashMap<>();
    public String lastMealKey = "";
    public boolean sickened = false;

    public static final int SICKNESS_DECAY = 120;

    public boolean hasPage(String key) {
        for (var page : cookbook) {
            if (page.key().equals(key)) return true;
        }
        return false;
    }

    public boolean hasSignature(String key) {
        for (var signature : signatures) {
            if (signature.key().equals(key)) return true;
        }
        return false;
    }

    public transient int lastDamageTick = -10000;
    private transient boolean dirty = false;
    public transient Map<ResourceLocation, Holder<Attribute>> appliedAttrMods = new HashMap<>();

    public void eat(ItemStack stack) {
        dirty = true;
        FoodDefinition def = CosmicFoodRegistry.get(stack);
        List<ActiveFood> list = def.category() == FoodCategory.BREW ? brews : foods;
        int max = def.category() == FoodCategory.BREW ? maxBrews : maxFoods;
        Item item = stack.getItem();
        int quality = QualityFoodCompat.level(stack);

        for (ActiveFood af : list) {
            if (af.item == item) {
                af.addServing(quality);
                return;
            }
        }

        ActiveFood active = new ActiveFood(item);
        active.addServing(quality);
        if (list.size() < max) {
            list.add(active);
            return;
        }

        int weakest = 0;
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).ticksLeft() < list.get(weakest).ticksLeft()) weakest = i;
        }
        list.set(weakest, active);
    }

    public void tick() {
        int step = sickened ? SICKNESS_DECAY : 1;
        tickList(foods, step);
        tickList(brews, step);
        if (sickened && !hasActive()) {
            sickened = false;
            dirty = true;
        }
    }

    private void tickList(List<ActiveFood> list, int step) {
        for (ActiveFood af : list) {
            if (af.tick(step)) dirty = true;
        }
        if (list.removeIf(ActiveFood::isExpired)) dirty = true;
    }

    public static final double[] CHANNEL_WEIGHTS = { 1.0, 0.5, 0.25 };

    public double totalHeartBonus() {
        double total = softCapTotal(FoodDefinition::heartBonus) + (memory != null ? memory.heartBonus() : 0);
        for (var signature : signatures) total += signature.heartBonus();
        return total;
    }

    public double totalRegenBonus() {
        double total = softCapTotal(FoodDefinition::regenBonus) + (memory != null ? memory.regenBonus() : 0);
        for (var signature : signatures) total += signature.regenBonus();
        return total;
    }

    public void setMemory(@Nullable FoodMemory newMemory) {
        memory = newMemory;
        dirty = true;
    }

    private double softCapTotal(ToDoubleFunction<FoodDefinition> extractor) {
        int count = foods.size() + brews.size();
        if (count == 0) return 0;
        double[] values = new double[count];
        int filled = 0;
        for (ActiveFood af : foods) {
            values[filled++] = extractor.applyAsDouble(af.def) * QualityFoodCompat.multiplier(af.quality());
        }
        for (ActiveFood af : brews) {
            values[filled++] = extractor.applyAsDouble(af.def) * QualityFoodCompat.multiplier(af.quality());
        }
        Arrays.sort(values);
        double total = 0;
        for (int rank = 0; rank < count; rank++) {
            double weight = CHANNEL_WEIGHTS[Math.min(rank, CHANNEL_WEIGHTS.length - 1)];
            total += values[count - 1 - rank] * weight;
        }
        return total;
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
        if (memory != null) tag.put("memory", memory.toTag());
        ListTag pages = new ListTag();
        for (var page : cookbook) pages.add(page.toTag());
        tag.put("cookbook", pages);
        tag.putLong("lastPageDay", lastPageDay);
        tag.putLong("lastPageTick", lastPageTick);
        ListTag sigs = new ListTag();
        for (var signature : signatures) sigs.add(signature.toTag());
        tag.put("signatures", sigs);
        CompoundTag progress = new CompoundTag();
        mealDays.forEach(progress::putInt);
        tag.put("mealDays", progress);
        CompoundTag lastDays = new CompoundTag();
        mealLastDay.forEach(lastDays::putLong);
        tag.put("mealLastDay", lastDays);
        tag.putString("lastMealKey", lastMealKey);
        tag.putBoolean("sickened", sickened);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        maxFoods = nbt.contains("maxFoods") ? nbt.getInt("maxFoods") : DEFAULT_FOOD_SLOTS;
        maxBrews = nbt.contains("maxBrews") ? nbt.getInt("maxBrews") : DEFAULT_BREW_SLOTS;
        foods = loadList(nbt.getList("foods", Tag.TAG_COMPOUND));
        brews = loadList(nbt.getList("brews", Tag.TAG_COMPOUND));
        memory = nbt.contains("memory") ? FoodMemory.fromTag(nbt.getCompound("memory")) : null;
        cookbook = new ArrayList<>();
        for (Tag element : nbt.getList("cookbook", Tag.TAG_COMPOUND)) {
            cookbook.add(CookbookPage.fromTag((CompoundTag) element));
        }
        lastPageDay = nbt.contains("lastPageDay") ? nbt.getLong("lastPageDay") : -1;
        lastPageTick = nbt.contains("lastPageTick") ? nbt.getLong("lastPageTick") : -1;
        signatures = new ArrayList<>();
        for (Tag element : nbt.getList("signatures", Tag.TAG_COMPOUND)) {
            var signature = SignatureMeal.fromTag((CompoundTag) element);
            if (signature != null) signatures.add(signature);
        }
        mealDays = new HashMap<>();
        CompoundTag progress = nbt.getCompound("mealDays");
        for (String key : progress.getAllKeys()) mealDays.put(key, progress.getInt(key));
        mealLastDay = new HashMap<>();
        CompoundTag lastDays = nbt.getCompound("mealLastDay");
        for (String key : lastDays.getAllKeys()) mealLastDay.put(key, lastDays.getLong(key));
        lastMealKey = nbt.getString("lastMealKey");
        sickened = nbt.getBoolean("sickened");
    }

    private static ListTag saveList(List<ActiveFood> list) {
        ListTag out = new ListTag();
        for (ActiveFood af : list) {
            CompoundTag t = new CompoundTag();
            t.putString("id", BuiltInRegistries.ITEM.getKey(af.item).toString());
            ListTag reserves = new ListTag();
            double[] qualityReserves = af.qualityReserves();
            for (int quality = 0; quality < qualityReserves.length; quality++) {
                if (qualityReserves[quality] <= 0.0) continue;
                CompoundTag reserve = new CompoundTag();
                reserve.putInt("quality", quality);
                reserve.putDouble("servings", qualityReserves[quality]);
                reserves.add(reserve);
            }
            t.put("qualityReserves", reserves);
            out.add(t);
        }
        return out;
    }

    private static List<ActiveFood> loadList(ListTag list) {
        List<ActiveFood> out = new ArrayList<>();
        for (Tag element : list) {
            CompoundTag t = (CompoundTag) element;
            Item item = FoodNbt.item(t.getString("id"));
            if (item == null) continue;
            ActiveFood active;
            if (t.contains("qualityReserves", Tag.TAG_LIST)) {
                double[] reserves = new double[4];
                for (Tag reserveTag : t.getList("qualityReserves", Tag.TAG_COMPOUND)) {
                    CompoundTag reserve = (CompoundTag) reserveTag;
                    int quality = Math.clamp(reserve.getInt("quality"), 0, 3);
                    reserves[quality] += reserve.getDouble("servings");
                }
                active = new ActiveFood(item);
                active.restoreQualityReserves(reserves);
            } else {
                int quality = t.contains("quality") ? t.getInt("quality") : 0;
                active = new ActiveFood(item, t.getInt("ticks"), quality);
            }
            if (!active.isExpired()) out.add(active);
        }
        return out;
    }
}
