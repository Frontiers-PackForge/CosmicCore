package com.ghostipedia.cosmiccore.common.item.armor.boots;

import com.ghostipedia.cosmiccore.utils.ItemData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface ICosmicBoots {

    String TAG_SPEED_MODIFIER = "cosmicboots:speed_mod";
    String TAG_JUMP_MODIFIER = "cosmicboots:jump_mod";
    String TAG_STEP_ASSIST = "cosmicboots:step_assist";
    String TAG_INERTIA_CANCEL = "cosmicboots:inertia_cancel";

    double MODIFIER_INCREMENT = 0.25;

    double getMaxSpeed();

    double getGroundAcceleration();

    double getGroundDeceleration();

    double getAirControl();

    double getJumpPower();

    default boolean negatesFallDamage() {
        return true;
    }

    default boolean hasStepAssist() {
        return true;
    }

    static double getSpeedModifier(ItemStack stack) {
        if (stack.isEmpty()) return 1.0;
        CompoundTag tag = ItemData.readTag(stack);
        if (tag == null || !tag.contains(TAG_SPEED_MODIFIER)) return 1.0;
        return tag.getDouble(TAG_SPEED_MODIFIER);
    }

    static double getJumpModifier(ItemStack stack) {
        if (stack.isEmpty()) return 1.0;
        CompoundTag tag = ItemData.readTag(stack);
        if (tag == null || !tag.contains(TAG_JUMP_MODIFIER)) return 1.0;
        return tag.getDouble(TAG_JUMP_MODIFIER);
    }

    static boolean isStepAssistEnabled(ItemStack stack) {
        if (stack.isEmpty()) return true;
        CompoundTag tag = ItemData.readTag(stack);
        if (tag == null || !tag.contains(TAG_STEP_ASSIST)) return true;
        return tag.getBoolean(TAG_STEP_ASSIST);
    }

    static boolean isInertiaCancelEnabled(ItemStack stack) {
        if (stack.isEmpty()) return true;
        CompoundTag tag = ItemData.readTag(stack);
        if (tag == null || !tag.contains(TAG_INERTIA_CANCEL)) return true;
        return tag.getBoolean(TAG_INERTIA_CANCEL);
    }

    static double changeSpeedModifier(ItemStack stack, double delta) {
        double current = getSpeedModifier(stack);
        double newValue = Math.max(0.0, Math.min(1.0, current + delta));
        newValue = Math.round(newValue / MODIFIER_INCREMENT) * MODIFIER_INCREMENT;
        double finalValue = newValue;
        ItemData.mutateTag(stack, tag -> tag.putDouble(TAG_SPEED_MODIFIER, finalValue));
        return newValue;
    }

    static double changeJumpModifier(ItemStack stack, double delta) {
        double current = getJumpModifier(stack);
        double newValue = Math.max(0.0, Math.min(1.0, current + delta));
        newValue = Math.round(newValue / MODIFIER_INCREMENT) * MODIFIER_INCREMENT;
        double finalValue = newValue;
        ItemData.mutateTag(stack, tag -> tag.putDouble(TAG_JUMP_MODIFIER, finalValue));
        return newValue;
    }

    static boolean toggleStepAssist(ItemStack stack) {
        boolean current = isStepAssistEnabled(stack);
        ItemData.mutateTag(stack, tag -> tag.putBoolean(TAG_STEP_ASSIST, !current));
        return !current;
    }

    static boolean toggleInertiaCancel(ItemStack stack) {
        boolean current = isInertiaCancelEnabled(stack);
        ItemData.mutateTag(stack, tag -> tag.putBoolean(TAG_INERTIA_CANCEL, !current));
        return !current;
    }

    default double getEffectiveMaxSpeed(ItemStack stack) {
        return getMaxSpeed() * getSpeedModifier(stack);
    }

    default double getEffectiveJumpPower(ItemStack stack) {
        return getJumpPower() * getJumpModifier(stack);
    }
}
