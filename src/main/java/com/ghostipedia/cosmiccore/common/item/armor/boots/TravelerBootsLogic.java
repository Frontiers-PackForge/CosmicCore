package com.ghostipedia.cosmiccore.common.item.armor.boots;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class TravelerBootsLogic {

    private static final ResourceLocation STEP_HEIGHT_ID = CosmicCore.id("travelers_boots_step_height");
    private static final ResourceLocation LEGACY_STEP_HEIGHT_ID = CosmicCore.id("cosmic_boots_step_height");
    private static final ResourceLocation LEGACY_SWIM_SPEED_ID = CosmicCore.id("cosmic_boots_swim_speed");
    private static final AttributeModifier STEP_HEIGHT = new AttributeModifier(
            STEP_HEIGHT_ID, 0.5, AttributeModifier.Operation.ADD_VALUE);

    private TravelerBootsLogic() {}

    public static void updateStepAssist(Player player) {
        clearLegacyModifiers(player);
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight == null) return;

        if (isWearing(player) && !player.isCrouching()) {
            stepHeight.addOrUpdateTransientModifier(STEP_HEIGHT);
        } else {
            stepHeight.removeModifier(STEP_HEIGHT_ID);
        }
    }

    public static void clearStepAssist(Player player) {
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.removeModifier(STEP_HEIGHT_ID);
        }
        clearLegacyModifiers(player);
    }

    public static boolean isWearing(Player player) {
        return player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof TravelerBootsItem;
    }

    private static void clearLegacyModifiers(Player player) {
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.removeModifier(LEGACY_STEP_HEIGHT_ID);
        }
        AttributeInstance swimSpeed = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (swimSpeed != null) {
            swimSpeed.removeModifier(LEGACY_SWIM_SPEED_ID);
        }
    }
}
