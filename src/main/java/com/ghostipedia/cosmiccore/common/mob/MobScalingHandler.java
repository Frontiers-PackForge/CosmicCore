package com.ghostipedia.cosmiccore.common.mob;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mob.DimensionMobScaling.ScalingConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Handles applying dimension-based stat scaling to mobs when they spawn/load.
 */
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MobScalingHandler {

    private MobScalingHandler() {}

    // Unique UUIDs for our attribute modifiers
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("c05a1c01-d1a3-5ca1-1a00-bea17bb005a1");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("c05a1c02-d1a3-5ca1-1a00-da4a3eb005a2");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("c05a1c03-d1a3-5ca1-1a00-a4a04b005a03");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("c05a1c04-d1a3-5ca1-1a00-5beedb005a04");

    private static final String MODIFIER_NAME = "Dimension Scaling";

    // NBT tag to mark entities as already scaled (prevents double-scaling on chunk reload)
    private static final String SCALED_TAG = "cosmiccore:dimension_scaled";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();

        // Only process on server side
        if (level.isClientSide()) return;

        // Only scale hostile/neutral mobs, not players or passive animals
        if (!shouldScale(entity)) return;

        LivingEntity living = (LivingEntity) entity;

        // Check if already scaled (prevents double-scaling)
        if (living.getPersistentData().getBoolean(SCALED_TAG)) return;

        // Get scaling config for this dimension
        ScalingConfig config = DimensionMobScaling.getScaling(level.dimension());

        // Skip if no scaling needed
        if (config == DimensionMobScaling.DEFAULT) return;

        // Apply scaling
        applyScaling(living, config);

        // Mark as scaled
        living.getPersistentData().putBoolean(SCALED_TAG, true);
    }

    /**
     * Determine if an entity should receive dimension scaling.
     */
    private static boolean shouldScale(Entity entity) {
        // Must be a living entity
        if (!(entity instanceof LivingEntity)) return false;

        // Don't scale players
        if (entity instanceof Player) return false;

        // Scale all monsters (hostile mobs)
        if (entity instanceof Monster) return true;

        // Scale neutral mobs (Mob class but not Monster)
        // This includes wolves, iron golems, piglins, etc.
        if (entity instanceof Mob) return true;

        return false;
    }

    /**
     * Apply scaling modifiers to a living entity.
     */
    private static void applyScaling(LivingEntity entity, ScalingConfig config) {
        // Health - use MULTIPLY_BASE so it multiplies the base value
        applyMultiplier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID, config.healthMultiplier);

        // Heal to full after increasing max health
        if (config.healthMultiplier > 1.0) {
            entity.setHealth(entity.getMaxHealth());
        }

        // Damage
        applyMultiplier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_UUID, config.damageMultiplier);

        // Armor
        if (config.armorMultiplier != 1.0) {
            applyMultiplier(entity, Attributes.ARMOR, ARMOR_MODIFIER_UUID, config.armorMultiplier);
        }

        // Speed
        if (config.speedMultiplier != 1.0) {
            applyMultiplier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, config.speedMultiplier);
        }
    }

    /**
     * Apply a multiplicative attribute modifier.
     */
    private static void applyMultiplier(LivingEntity entity,
                                        net.minecraft.world.entity.ai.attributes.Attribute attribute,
                                        UUID uuid, double multiplier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        // Remove existing modifier if present
        instance.removeModifier(uuid);

        // MULTIPLY_BASE means: final = base * (1 + modifier)
        // So for 2x, we need modifier = 1.0 (base * 2)
        // For 3x, we need modifier = 2.0 (base * 3)
        double modifierValue = multiplier - 1.0;

        if (modifierValue != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(
                    uuid,
                    MODIFIER_NAME,
                    modifierValue,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}
