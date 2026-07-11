package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.ghostipedia.cosmiccore.common.data.worldgen.abyss.AbyssRegions;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class PressureRules {

    private PressureRules() {}

    public static final List<TagKey<Item>> RATED = List.of(
            CosmicItemTags.PRESSURE_RATED_1,
            CosmicItemTags.PRESSURE_RATED_2,
            CosmicItemTags.PRESSURE_RATED_3,
            CosmicItemTags.PRESSURE_RATED_4);

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public static int requiredRating(int y) {
        return AbyssRegions.layer(y);
    }

    public static int gearRating(LivingEntity entity) {
        int rating = Integer.MAX_VALUE;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            rating = Math.min(rating, pieceRating(entity.getItemBySlot(slot)));
            if (rating == 0) return 0;
        }
        return rating;
    }

    private static int pieceRating(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        for (int i = RATED.size() - 1; i >= 0; i--) {
            if (stack.is(RATED.get(i))) return i + 1;
        }
        return 0;
    }

    public static boolean crushing(Player player) {
        if (!MurkbloomServerLogic.inHollow(player.level(), player.getY())) return false;
        return requiredRating(player.getBlockY()) > gearRating(player);
    }
}
