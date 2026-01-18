package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.armor.boots.ICosmicBoots;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class QuakeMovementHandler {

    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Integer> airTime = new HashMap<>();

    private static boolean clientHasQuakeMovement = false;

    private static final double AIR_ACCELERATE = 200.0;
    private static final double AIR_WISH_SPEED = 0.8;
    private static final double BHOP_BOOST = 1.25;
    private static final double MAX_SPEED = 1.2;
    private static final double MIN_BHOP_SPEED = 0.08;
    private static final double FRICTION_COUNTER = 0.85;

    private static final boolean DEBUG_MODE = false;
    private static final double VANILLA_SPRINT_SPEED = 0.28;
    private static final double VANILLA_SPRINT_JUMP_SPEED = 0.36;

    private static int lastBhopTick = 0;
    private static int lastStrafeTick = 0;
    private static double sessionMaxSpeed = 0;

    @OnlyIn(Dist.CLIENT)
    public static void setClientHasQuakeMovement(boolean has) {
        clientHasQuakeMovement = has;
        CosmicCore.LOGGER.info("Client quake movement set to: {}", has);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;

        if (!player.level().isClientSide()) return;
        if (!clientHasQuakeMovement) return;
        if (player != Minecraft.getInstance().player) return;

        if (player.isInWater() || player.isInLava() || player.isInFluidType((fluidType, height) -> height > 0.0))
            return;

        if (player.getAbilities().flying || player.isFallFlying())
            return;

        UUID uuid = player.getUUID();
        boolean onGround = player.onGround();
        boolean wasGrounded = wasOnGround.getOrDefault(uuid, true);

        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeed = getHorizontalSpeed(motion);

        if (!onGround) {
            airTime.merge(uuid, 1, Integer::sum);
        } else {
            airTime.put(uuid, 0);
        }

        boolean didBhop = false;
        if (onGround && !wasGrounded && horizontalSpeed > MIN_BHOP_SPEED) {
            double oldSpeed = horizontalSpeed;
            motion = applyBunnyHop(player, motion, horizontalSpeed);
            player.setDeltaMovement(motion);
            double newSpeed = getHorizontalSpeed(motion);
            horizontalSpeed = newSpeed;
            didBhop = newSpeed > oldSpeed;

            if (DEBUG_MODE && didBhop) {
                lastBhopTick = player.tickCount;
            }
        }

        boolean didStrafe = false;
        if (!onGround) {
            double oldSpeed = getHorizontalSpeed(motion);
            motion = applyAirAcceleration(player, motion);
            player.setDeltaMovement(motion);
            double newSpeed = getHorizontalSpeed(motion);
            didStrafe = newSpeed > oldSpeed + 0.005;

            if (DEBUG_MODE && didStrafe) {
                lastStrafeTick = player.tickCount;
            }
        }

        if (onGround && horizontalSpeed > MIN_BHOP_SPEED) {
            motion = reduceGroundFriction(player, motion, horizontalSpeed);
            player.setDeltaMovement(motion);
        }

        if (DEBUG_MODE) {
            double speed = getHorizontalSpeed(player.getDeltaMovement());
            if (speed > sessionMaxSpeed) {
                sessionMaxSpeed = speed;
            }

            StringBuilder status = new StringBuilder();

            ChatFormatting speedColor = getSpeedColor(speed);
            String speedMultiple = String.format("%.1fx", speed / VANILLA_SPRINT_SPEED);

            boolean recentBhop = (player.tickCount - lastBhopTick) < 20;
            boolean recentStrafe = (player.tickCount - lastStrafeTick) < 10;

            if (recentBhop) {
                status.append("§a[BHOP!] ");
            }
            if (recentStrafe && !onGround) {
                status.append("§b[STRAFE] ");
            }

            status.append(speedColor.toString());
            status.append(String.format("%.2f b/t (%s)", speed, speedMultiple));

            if (sessionMaxSpeed > VANILLA_SPRINT_JUMP_SPEED) {
                status.append(String.format(" §7| Max: §d%.2f", sessionMaxSpeed));
            }

            status.append(onGround ? " §7[G]" : " §e[AIR]");

            float fwd = player.zza;
            float strafe = player.xxa;
            if (Math.abs(fwd) > 0.01 || Math.abs(strafe) > 0.01) {
                status.append(String.format(" §8[%.1f/%.1f]", fwd, strafe));
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
        }

        wasOnGround.put(uuid, onGround);
    }

    public static boolean hasQuakeMovement(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId()))
                .orElse(false);
    }

    private static Vec3 applyBunnyHop(Player player, Vec3 motion, double currentSpeed) {
        double maxSpeed = getEffectiveMaxSpeed(player);
        double boostedSpeed = Math.min(currentSpeed * BHOP_BOOST, maxSpeed);

        if (boostedSpeed > currentSpeed && currentSpeed > 0) {
            double scale = boostedSpeed / currentSpeed;
            return new Vec3(motion.x * scale, motion.y, motion.z * scale);
        }

        return motion;
    }

    private static Vec3 applyAirAcceleration(Player player, Vec3 motion) {
        float forward = player.zza;
        float strafe = player.xxa;

        if (Math.abs(forward) < 0.01 && Math.abs(strafe) < 0.01) {
            return motion;
        }

        float yaw = player.getYRot() * ((float) Math.PI / 180f);

        double wishX = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
        double wishZ = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;

        double wishLength = Math.sqrt(wishX * wishX + wishZ * wishZ);
        if (wishLength > 0.01) {
            wishX /= wishLength;
            wishZ /= wishLength;
        } else {
            return motion;
        }

        double currentWishSpeed = motion.x * wishX + motion.z * wishZ;
        double addSpeed = AIR_WISH_SPEED - currentWishSpeed;

        if (addSpeed <= 0) {
            return motion;
        }

        double accelSpeed = AIR_ACCELERATE * 0.05 * AIR_WISH_SPEED;

        if (accelSpeed > addSpeed) {
            accelSpeed = addSpeed;
        }

        double newX = motion.x + accelSpeed * wishX;
        double newZ = motion.z + accelSpeed * wishZ;

        double maxSpeed = getEffectiveMaxSpeed(player);
        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (newSpeed > maxSpeed) {
            double scale = maxSpeed / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        return new Vec3(newX, motion.y, newZ);
    }

    private static Vec3 reduceGroundFriction(Player player, Vec3 motion, double currentSpeed) {
        double boost = 1.0 + FRICTION_COUNTER;

        double newX = motion.x * boost;
        double newZ = motion.z * boost;

        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);

        double maxSpeed = getEffectiveMaxSpeed(player);
        if (newSpeed > maxSpeed) {
            double scale = maxSpeed / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        if (newSpeed > currentSpeed * 1.01) {
            double scale = currentSpeed / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        return new Vec3(newX, motion.y, newZ);
    }

    private static double getHorizontalSpeed(Vec3 motion) {
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    private static double getEffectiveMaxSpeed(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!boots.isEmpty() && boots.getItem() instanceof ArmorComponentItem armorItem) {
            if (armorItem.getArmorLogic() instanceof ICosmicBoots cosmicBoots) {
                double bootSpeed = cosmicBoots.getEffectiveMaxSpeed(boots);
                if (bootSpeed <= 0) {
                    return MAX_SPEED;
                }
                return bootSpeed;
            }
        }
        return MAX_SPEED;
    }

    public static void removePlayer(UUID uuid) {
        wasOnGround.remove(uuid);
        airTime.remove(uuid);
    }

    @OnlyIn(Dist.CLIENT)
    private static void sendDebugMessage(Player player, ChatFormatting color, String format, Object... args) {
        if (!DEBUG_MODE) return;
        String msg = String.format(format, args);
        player.displayClientMessage(
                Component.literal("[QUAKE] ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(msg).withStyle(color)),
                true);
    }

    private static ChatFormatting getSpeedColor(double speed) {
        if (speed >= 1.0) return ChatFormatting.LIGHT_PURPLE;
        if (speed >= 0.6) return ChatFormatting.RED;
        if (speed >= VANILLA_SPRINT_JUMP_SPEED) return ChatFormatting.YELLOW;
        if (speed >= VANILLA_SPRINT_SPEED) return ChatFormatting.GREEN;
        return ChatFormatting.GRAY;
    }

    private static String getSpeedComparison(double speed) {
        double sprintMultiple = speed / VANILLA_SPRINT_SPEED;
        if (speed < 0.05) {
            return "(standing)";
        } else if (speed < VANILLA_SPRINT_SPEED) {
            return String.format("(%.0f%% of sprint)", sprintMultiple * 100);
        } else if (speed < VANILLA_SPRINT_JUMP_SPEED) {
            return String.format("(%.1fx sprint)", sprintMultiple);
        } else {
            return String.format("(%.1fx sprint, %.1fx sprint-jump)",
                    sprintMultiple, speed / VANILLA_SPRINT_JUMP_SPEED);
        }
    }
}
