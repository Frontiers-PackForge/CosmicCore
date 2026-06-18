package com.ghostipedia.cosmiccore.common.item.armor.boots;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CosmicBootsArmorLogic extends ArmorLogicSuite implements ICosmicBoots {

    protected static final ResourceLocation STEP_HEIGHT_ID = CosmicCore.id("cosmic_boots_step_height");
    protected static final ResourceLocation SWIM_SPEED_ID = CosmicCore.id("cosmic_boots_swim_speed");

    protected final double maxSpeed;
    protected final double groundAcceleration;
    protected final double groundDeceleration;
    protected final double airControl;
    protected final double jumpPower;
    protected final boolean fallNegation;
    protected final String texturePath;

    private static final double SPRINT_BOOST = 2.5;

    public CosmicBootsArmorLogic(int energyPerUse, long maxCapacity, int tier,
                                 double maxSpeed, double groundAcceleration,
                                 double groundDeceleration, double airControl,
                                 double jumpPower, boolean fallNegation,
                                 String texturePath) {
        super(energyPerUse, maxCapacity, tier, ArmorItem.Type.BOOTS);
        this.maxSpeed = maxSpeed;
        this.groundAcceleration = groundAcceleration;
        this.groundDeceleration = groundDeceleration;
        this.airControl = airControl;
        this.jumpPower = jumpPower;
        this.fallNegation = fallNegation;
        this.texturePath = texturePath;
    }

    @Override
    public double getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public double getGroundAcceleration() {
        return groundAcceleration;
    }

    @Override
    public double getGroundDeceleration() {
        return groundDeceleration;
    }

    @Override
    public double getAirControl() {
        return airControl;
    }

    @Override
    public double getJumpPower() {
        return jumpPower;
    }

    @Override
    public boolean negatesFallDamage() {
        return fallNegation;
    }

    @Override
    public boolean hasStepAssist() {
        return true;
    }

    @Override
    public void onArmorTick(Level level, Player player, @NotNull ItemStack boots) {
        IElectricItem electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric == null) return;

        boolean hasEnergy = electric.getCharge() > 0;

        if (hasEnergy) {
            boolean canApplyMovement = !player.getAbilities().flying && !player.isFallFlying();

            if (player.isInWater() || player.isInLava()) {
                applySwimSpeed(player, boots);
            } else {
                removeSwimSpeed(player);
                if (canApplyMovement) {
                    applyMovementBonus(player, boots);
                }
            }

            if (ICosmicBoots.isStepAssistEnabled(boots)) {
                applyStepHeight(player);
            } else {
                removeStepHeight(player);
            }

            if (!level.isClientSide()) {
                Vec3 motion = player.getDeltaMovement();
                double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
                if (horizontalSpeed > 0.4) {
                    spawnCloudTrail(level, player, horizontalSpeed);
                }
                if (horizontalSpeed > 0.2) {
                    electric.discharge(energyPerUse / 100L, tier, true, false, false);
                }
            }
        } else {
            removeStepHeight(player);
            removeSwimSpeed(player);
        }
    }

    protected void applyStepHeight(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute != null && attribute.getModifier(STEP_HEIGHT_ID) == null) {
            attribute.addPermanentModifier(new AttributeModifier(
                    STEP_HEIGHT_ID,
                    1.0,
                    AttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void removeStepHeight(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attribute != null) {
            attribute.removeModifier(STEP_HEIGHT_ID);
        }
    }

    protected void applySwimSpeed(Player player, ItemStack boots) {
        double speedMod = ICosmicBoots.getSpeedModifier(boots);
        if (speedMod <= 0) {
            removeSwimSpeed(player);
            return;
        }

        double swimBoost = groundAcceleration * speedMod * 0.5;

        AttributeInstance attribute = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SWIM_SPEED_ID);
            attribute.addTransientModifier(new AttributeModifier(
                    SWIM_SPEED_ID,
                    swimBoost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    protected void removeSwimSpeed(Player player) {
        AttributeInstance attribute = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (attribute != null) {
            attribute.removeModifier(SWIM_SPEED_ID);
        }
    }

    protected void applyMovementBonus(Player player, ItemStack boots) {
        double speedMod = ICosmicBoots.getSpeedModifier(boots);
        if (speedMod <= 0) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();

        float forward = player.zza;
        float strafe = player.xxa;
        boolean hasInput = forward != 0 || strafe != 0;
        boolean isSprinting = player.isSprinting();

        double effectiveMaxSpeed = getEffectiveMaxSpeed(boots);
        double effectiveAccel = groundAcceleration * speedMod;

        if (isSprinting) {
            effectiveAccel *= SPRINT_BOOST;
            effectiveMaxSpeed *= SPRINT_BOOST;
        }

        double newX = motion.x;
        double newZ = motion.z;

        if (hasInput) {
            float yaw = player.getYRot() * ((float) Math.PI / 180F);
            double sinYaw = Mth.sin(yaw);
            double cosYaw = Mth.cos(yaw);

            double wishX = (strafe * cosYaw - forward * sinYaw);
            double wishZ = (forward * cosYaw + strafe * sinYaw);

            double wishLen = Math.sqrt(wishX * wishX + wishZ * wishZ);
            if (wishLen > 0.01) {
                wishX /= wishLen;
                wishZ /= wishLen;
            }

            double boostAmount;
            if (player.onGround()) {
                boostAmount = effectiveAccel * 0.08;
            } else {
                boostAmount = airControl * effectiveAccel * 0.07;
            }

            newX += wishX * boostAmount;
            newZ += wishZ * boostAmount;

        } else if (ICosmicBoots.isInertiaCancelEnabled(boots) && player.onGround()) {
            double decelAmount = groundDeceleration;

            if (Math.abs(newX) > 0.001) {
                newX *= (1.0 - decelAmount);
            } else {
                newX = 0;
            }
            if (Math.abs(newZ) > 0.001) {
                newZ *= (1.0 - decelAmount);
            } else {
                newZ = 0;
            }
        }

        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (newSpeed > effectiveMaxSpeed) {
            double scale = effectiveMaxSpeed / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        player.setDeltaMovement(newX, motion.y, newZ);
    }

    protected void spawnCloudTrail(Level level, Player player, double speed) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 pos = player.position();
        Vec3 motion = player.getDeltaMovement();

        int particleCount = (int) Math.min(4, speed * 3);

        double offsetX = -motion.x * 1.5;
        double offsetZ = -motion.z * 1.5;

        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                pos.x + offsetX + (level.random.nextDouble() - 0.5) * 0.5,
                pos.y + 0.15,
                pos.z + offsetZ + (level.random.nextDouble() - 0.5) * 0.5,
                particleCount,
                0.12, 0.06, 0.12,
                0.02);
    }

    @Override
    public int damageArmor(LivingEntity entity, ItemStack boots, DamageSource source, int damage,
                           EquipmentSlot slot) {
        IElectricItem electric = GTCapabilityHelper.getElectricItem(boots);
        if (electric != null) {
            electric.discharge((long) energyPerUse * damage, tier, true, false, false);
        }
        return 0;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return CosmicCore.id(texturePath);
    }

    @Override
    public void addInfo(ItemStack stack, List<Component> lines) {
        super.addInfo(stack, lines);

        double speedMod = ICosmicBoots.getSpeedModifier(stack);
        double jumpMod = ICosmicBoots.getJumpModifier(stack);
        boolean stepEnabled = ICosmicBoots.isStepAssistEnabled(stack);
        boolean inertiaEnabled = ICosmicBoots.isInertiaCancelEnabled(stack);

        lines.add(Component.translatable("cosmiccore.boots.speed_modifier",
                String.format("%.0f%%", speedMod * 100)));
        lines.add(Component.translatable("cosmiccore.boots.jump_modifier",
                String.format("%.0f%%", jumpMod * 100)));

        lines.add(Component.translatable("cosmiccore.boots.step_assist",
                stepEnabled ? "\u00a7aON" : "\u00a7cOFF"));

        lines.add(Component.translatable("cosmiccore.boots.inertia_cancel",
                inertiaEnabled ? "\u00a7aON" : "\u00a7cOFF"));

        lines.add(Component.translatable("cosmiccore.boots.max_speed",
                String.format("%.1f", getEffectiveMaxSpeed(stack) * 20) + " b/s"));
    }
}
