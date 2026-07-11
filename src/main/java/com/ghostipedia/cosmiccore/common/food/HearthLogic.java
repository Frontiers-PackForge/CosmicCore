package com.ghostipedia.cosmiccore.common.food;

import com.ghostipedia.cosmiccore.common.data.CosmicAttachmentTypes;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.SyncFoodDataPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HearthLogic {

    private HearthLogic() {}

    public static final double MEMORY_POWER_SHARE = 0.5;
    public static final double COMPLEMENT_SHARE = 0.15;
    public static final int HOME_RADIUS = 48;
    public static final double PALATE_PER_PAGE = 0.02;
    public static final int PALATE_PAGE_CAP = 20;
    public static final int SIGNATURE_DAYS = 7;
    public static final int MAX_SIGNATURES = 3;
    public static final double SIGNATURE_SHARE = 0.25;

    public static double palateMultiplier(CosmicFoodData data) {
        return 1.0 + Math.min(data.cookbook.size(), PALATE_PAGE_CAP) * PALATE_PER_PAGE;
    }

    public static boolean isAtHome(ServerPlayer player) {
        BlockPos respawn = player.getRespawnPosition();
        if (respawn == null) return false;
        if (player.getRespawnDimension() != player.level().dimension()) return false;
        return respawn.distSqr(player.blockPosition()) <= (double) HOME_RADIUS * HOME_RADIUS;
    }

    public static boolean applyHomeMeal(ServerPlayer player, ItemStack stack, int quality, int sharedWith) {
        return applyHomeMeal(player, stack, ItemStack.EMPTY, ItemStack.EMPTY, quality, sharedWith);
    }

    public static boolean applyHomeMeal(ServerPlayer player, ItemStack main, ItemStack side, ItemStack drink,
                                        int quality, int sharedWith) {
        if (main.isEmpty()) return false;
        if (!CosmicFoodRegistry.isConsumable(main)) return false;
        if (CosmicFoodRegistry.isVile(main.getItem())) return false;

        FoodDefinition def = CosmicFoodRegistry.get(main);
        double hearts = def.heartBonus() * MEMORY_POWER_SHARE;
        double regen = def.regenBonus() * MEMORY_POWER_SHARE;
        Map<Holder<MobEffect>, Integer> effectMerge = new LinkedHashMap<>();
        collectEffects(def, effectMerge);
        StringBuilder name = new StringBuilder(main.getHoverName().getString());
        if (!side.isEmpty() && CosmicFoodRegistry.isConsumable(side)) {
            FoodDefinition sideDef = CosmicFoodRegistry.get(side);
            hearts += sideDef.heartBonus() * COMPLEMENT_SHARE;
            regen += sideDef.regenBonus() * COMPLEMENT_SHARE;
            collectEffects(sideDef, effectMerge);
            name.append(" with ").append(side.getHoverName().getString());
        }
        if (!drink.isEmpty() && CosmicFoodRegistry.isConsumable(drink)) {
            FoodDefinition drinkDef = CosmicFoodRegistry.get(drink);
            hearts += drinkDef.heartBonus() * COMPLEMENT_SHARE;
            regen += drinkDef.regenBonus() * COMPLEMENT_SHARE;
            collectEffects(drinkDef, effectMerge);
            name.append(name.indexOf(" with ") >= 0 ? " and " : " with ").append(drink.getHoverName().getString());
        }

        List<FoodDefinition.EffectSpec> memoryEffects = new ArrayList<>();
        effectMerge.forEach((effect, amp) -> memoryEffects.add(new FoodDefinition.EffectSpec(effect, amp)));

        long day = player.serverLevel().getDayTime() / 24000L;
        CosmicFoodData data = player.getData(CosmicAttachmentTypes.FOOD_DATA);

        double palate = palateMultiplier(data);
        FoodMemory memory = new FoodMemory(name.toString(), main.getItem(), hearts * palate, regen * palate,
                quality, sharedWith, day, List.copyOf(memoryEffects));
        data.setMemory(memory);

        String pageKey = pageKey(main, side, drink);
        if (day > data.lastPageDay && !data.hasPage(pageKey)) {
            data.cookbook.add(new CookbookPage(pageKey, name.toString(), day));
            data.lastPageDay = day;
            String pageLangKey = data.cookbook.size() <= PALATE_PAGE_CAP ?
                    "cosmiccore.hearth.page_broadens" : "cosmiccore.hearth.page";
            player.sendSystemMessage(msg(pageLangKey, 0xB9A5E3));
        }

        data.lastMealKey = pageKey;
        Long lastMealDay = data.mealLastDay.get(pageKey);
        if (lastMealDay == null || day > lastMealDay) {
            data.mealLastDay.put(pageKey, day);
            int count = data.mealDays.merge(pageKey, 1, Integer::sum);
            if (count == SIGNATURE_DAYS && !data.hasSignature(pageKey)) {
                player.sendSystemMessage(msg("cosmiccore.hearth.taken_root", 0xC9AEF5, name.toString()));
            }
        }

        CCoreNetwork.sendToPlayer(player, new SyncFoodDataPacket(data));
        data.consumeDirty();
        player.sendSystemMessage(msg("cosmiccore.hearth.memory_settles", 0xE8C66A, memory.dishName()));
        return true;
    }

    public static boolean canInscribe(CosmicFoodData data) {
        return data.memory != null && !data.lastMealKey.isEmpty() &&
                !data.hasSignature(data.lastMealKey) &&
                data.mealDays.getOrDefault(data.lastMealKey, 0) >= SIGNATURE_DAYS;
    }

    public static Component inscribeCurrentMeal(ServerPlayer player) {
        CosmicFoodData data = player.getData(CosmicAttachmentTypes.FOOD_DATA);
        FoodMemory memory = data.memory;
        String key = data.lastMealKey;
        if (memory == null || key.isEmpty()) {
            return Component.translatable("cosmiccore.hearth.inscribe.no_memory");
        }
        if (data.hasSignature(key)) {
            return Component.translatable("cosmiccore.hearth.inscribe.already", memory.dishName());
        }
        if (data.mealDays.getOrDefault(key, 0) < SIGNATURE_DAYS) {
            return Component.translatable("cosmiccore.hearth.inscribe.not_rooted");
        }
        if (data.signatures.size() >= MAX_SIGNATURES) {
            return Component.translatable("cosmiccore.hearth.inscribe.full", MAX_SIGNATURES);
        }
        SignatureMeal signature = new SignatureMeal(key, memory.dishName(), memory.dish(),
                memory.heartBonus() * SIGNATURE_SHARE, memory.regenBonus() * SIGNATURE_SHARE, memory.day());
        data.signatures.add(signature);
        data.setMemory(data.memory);
        CCoreNetwork.sendToPlayer(player, new SyncFoodDataPacket(data));
        data.consumeDirty();
        return msg("cosmiccore.hearth.inscribe.done", 0xC9AEF5, signature.dishName());
    }

    private static void collectEffects(FoodDefinition def, Map<Holder<MobEffect>, Integer> into) {
        for (FoodDefinition.EffectSpec spec : def.effects()) {
            into.merge(spec.effect(), spec.amplifier(), Math::max);
        }
    }

    private static Component msg(String key, int color, Object... args) {
        return Component.translatable(key, args).withStyle(style -> style.withColor(color));
    }

    private static String pageKey(ItemStack main, ItemStack side, ItemStack drink) {
        List<String> parts = new ArrayList<>();
        parts.add(BuiltInRegistries.ITEM.getKey(main.getItem()).toString());
        if (!side.isEmpty()) parts.add(BuiltInRegistries.ITEM.getKey(side.getItem()).toString());
        if (!drink.isEmpty()) parts.add(BuiltInRegistries.ITEM.getKey(drink.getItem()).toString());
        Collections.sort(parts);
        return String.join("+", parts);
    }
}
