package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.common.data.CosmicEffects;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.item.IGTTool;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

public final class StealthCoating {

    private StealthCoating() {}

    private static final String TAG_ROOT = "CosmicStealth";
    private static final String TAG_TIER = "Tier";
    public static final int MAX_TIER = 3;
    private static final double[] TOOL_MULT = { 1.0, 0.25, 0.15, 0.05 };
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public static int tier(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int tier = ItemData.readElement(stack, TAG_ROOT).getInt(TAG_TIER);
        return Math.max(0, Math.min(MAX_TIER, tier));
    }

    public static void setTier(ItemStack stack, int tier) {
        int clamped = Math.max(0, Math.min(MAX_TIER, tier));
        ItemData.mutateElement(stack, TAG_ROOT, tag -> tag.putInt(TAG_TIER, clamped));
    }

    public static boolean canCoat(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof ArmorItem || item instanceof DiggerItem || item instanceof IGTTool ||
                stack.is(Tags.Items.TOOLS);
    }

    public static double toolMultiplier(ItemStack stack) {
        return TOOL_MULT[tier(stack)];
    }

    public static int armorTierSum(Player player) {
        int sum = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            sum += tier(player.getItemBySlot(slot));
        }
        return sum;
    }

    public static double armorMultiplier(Player player) {
        return Math.pow(5, -armorTierSum(player) / 4.0);
    }

    public static double effectMultiplier(LivingEntity entity) {
        MobEffectInstance instance = entity.getEffect(CosmicEffects.STEALTH);
        if (instance == null) return 1.0;
        return Math.pow(5, -(instance.getAmplifier() + 1) / 4.0);
    }

    public static String numeral(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(tier);
        };
    }
}
