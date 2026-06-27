package com.ghostipedia.cosmiccore.common.mob;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mob.DimensionMobScaling.ScalingConfig;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Handles applying dimension-based stat scaling to mobs when they spawn/load.
 */
@EventBusSubscriber(modid = CosmicCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class MobScalingHandler {

    private MobScalingHandler() {}

    private static final ResourceLocation HEALTH_MODIFIER_ID = CosmicCore.id("mob_scale_health");
    private static final ResourceLocation DAMAGE_MODIFIER_ID = CosmicCore.id("mob_scale_damage");
    private static final ResourceLocation ARMOR_MODIFIER_ID = CosmicCore.id("mob_scale_armor");
    private static final ResourceLocation SPEED_MODIFIER_ID = CosmicCore.id("mob_scale_speed");

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
        applyMultiplier(entity, Attributes.MAX_HEALTH, HEALTH_MODIFIER_ID, config.healthMultiplier);

        // Heal to full after increasing max health
        if (config.healthMultiplier > 1.0) {
            entity.setHealth(entity.getMaxHealth());
        }

        // Damage
        applyMultiplier(entity, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_ID, config.damageMultiplier);

        // Armor
        if (config.armorMultiplier != 1.0) {
            applyMultiplier(entity, Attributes.ARMOR, ARMOR_MODIFIER_ID, config.armorMultiplier);
        }

        // Speed
        if (config.speedMultiplier != 1.0) {
            applyMultiplier(entity, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_ID, config.speedMultiplier);
        }
    }

    /**
     * Apply a multiplicative attribute modifier.
     */
    private static void applyMultiplier(LivingEntity entity,
                                        Holder<Attribute> attribute,
                                        ResourceLocation id, double multiplier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;

        instance.removeModifier(id);

        double modifierValue = multiplier - 1.0;

        if (modifierValue != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(
                    id,
                    modifierValue,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
