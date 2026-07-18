package com.ghostipedia.cosmiccore.common.food;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FoodMemory(String dishName, Item dish, @Nullable Item side, @Nullable Item drink,
                         double heartBonus, double regenBonus, int quality, int sideQuality, int drinkQuality,
                         int sharedWith, long day, List<FoodDefinition.EffectSpec> effects) {

    public FoodMemory {
        quality = Math.clamp(quality, 0, 3);
        sideQuality = side == null ? 0 : Math.clamp(sideQuality, 0, 3);
        drinkQuality = drink == null ? 0 : Math.clamp(drinkQuality, 0, 3);
        effects = List.copyOf(effects);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", dishName);
        tag.putString("dish", BuiltInRegistries.ITEM.getKey(dish).toString());
        if (side != null) tag.putString("side", BuiltInRegistries.ITEM.getKey(side).toString());
        if (drink != null) tag.putString("drink", BuiltInRegistries.ITEM.getKey(drink).toString());
        tag.putDouble("hearts", heartBonus);
        tag.putDouble("regen", regenBonus);
        tag.putInt("quality", quality);
        tag.putInt("sideQuality", sideQuality);
        tag.putInt("drinkQuality", drinkQuality);
        tag.putInt("shared", sharedWith);
        tag.putLong("day", day);
        ListTag effectList = new ListTag();
        for (FoodDefinition.EffectSpec spec : effects) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("id", spec.effect().unwrapKey().map(k -> k.location().toString()).orElse(""));
            effectTag.putInt("amp", spec.amplifier());
            effectList.add(effectTag);
        }
        tag.put("effects", effectList);
        return tag;
    }

    @Nullable
    public static FoodMemory fromTag(CompoundTag tag) {
        if (!tag.contains("dish")) return null;
        Item dish = FoodNbt.item(tag.getString("dish"));
        if (dish == null) return null;
        Item side = tag.contains("side", Tag.TAG_STRING) ? FoodNbt.item(tag.getString("side")) : null;
        Item drink = tag.contains("drink", Tag.TAG_STRING) ? FoodNbt.item(tag.getString("drink")) : null;
        List<FoodDefinition.EffectSpec> effects = new ArrayList<>();
        for (Tag element : tag.getList("effects", Tag.TAG_COMPOUND)) {
            CompoundTag effectTag = (CompoundTag) element;
            ResourceLocation id = ResourceLocation.tryParse(effectTag.getString("id"));
            if (id == null) continue;
            BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, id))
                    .ifPresent(holder -> effects.add(
                            new FoodDefinition.EffectSpec(holder, effectTag.getInt("amp"))));
        }
        return new FoodMemory(tag.getString("name"), dish, side, drink, tag.getDouble("hearts"),
                tag.getDouble("regen"), tag.getInt("quality"), tag.getInt("sideQuality"),
                tag.getInt("drinkQuality"), tag.getInt("shared"), tag.getLong("day"), effects);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(dishName);
        buf.writeVarInt(BuiltInRegistries.ITEM.getId(dish));
        writeOptionalItem(buf, side);
        writeOptionalItem(buf, drink);
        buf.writeDouble(heartBonus);
        buf.writeDouble(regenBonus);
        buf.writeVarInt(quality);
        buf.writeVarInt(sideQuality);
        buf.writeVarInt(drinkQuality);
        buf.writeVarInt(sharedWith);
        buf.writeVarLong(day);
        buf.writeVarInt(effects.size());
        for (FoodDefinition.EffectSpec spec : effects) {
            buf.writeVarInt(BuiltInRegistries.MOB_EFFECT.getId(spec.effect().value()));
            buf.writeVarInt(spec.amplifier());
        }
    }

    public static FoodMemory read(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        Item dish = BuiltInRegistries.ITEM.byId(buf.readVarInt());
        Item side = readOptionalItem(buf);
        Item drink = readOptionalItem(buf);
        double hearts = buf.readDouble();
        double regen = buf.readDouble();
        int quality = buf.readVarInt();
        int sideQuality = buf.readVarInt();
        int drinkQuality = buf.readVarInt();
        int shared = buf.readVarInt();
        long day = buf.readVarLong();
        int count = buf.readVarInt();
        List<FoodDefinition.EffectSpec> effects = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.byId(buf.readVarInt());
            int amp = buf.readVarInt();
            if (effect != null) {
                Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
                effects.add(new FoodDefinition.EffectSpec(holder, amp));
            }
        }
        return new FoodMemory(name, dish, side, drink, hearts, regen, quality, sideQuality, drinkQuality,
                shared, day, effects);
    }

    private static void writeOptionalItem(FriendlyByteBuf buf, @Nullable Item item) {
        buf.writeBoolean(item != null);
        if (item != null) buf.writeVarInt(BuiltInRegistries.ITEM.getId(item));
    }

    @Nullable
    private static Item readOptionalItem(FriendlyByteBuf buf) {
        return buf.readBoolean() ? BuiltInRegistries.ITEM.byId(buf.readVarInt()) : null;
    }
}
