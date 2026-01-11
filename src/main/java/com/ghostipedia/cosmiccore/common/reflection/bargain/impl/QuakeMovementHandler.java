package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Quake-style movement for players with the Quake Movement bargain.
 *
 * This implements proper Quake/Source-engine style air strafing:
 * - Air acceleration allows gaining speed by looking perpendicular to movement and strafing
 * - Bunny hopping preserves and builds momentum
 * - Ground friction is reduced to maintain speed
 *
 * The key insight of Quake movement is that air acceleration is applied in the
 * WISH direction (where player wants to go) but only adds speed if current
 * velocity in that direction is below a threshold. This creates the characteristic
 * "strafe jumping" behavior.
 */
@Mod.EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class QuakeMovementHandler {

    // Track if player was on ground last tick (for bhop detection)
    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Integer> airTime = new HashMap<>();

    // Client-side state - synced from server
    private static boolean clientHasQuakeMovement = false;

    // === TUNING PARAMETERS ===
    // Cranked up for testing

    /** Air acceleration rate - higher = faster speed gain while strafing */
    private static final double AIR_ACCELERATE = 200.0;

    /** Max speed you can accelerate to in air (per-axis wish speed) */
    private static final double AIR_WISH_SPEED = 0.8;

    /** Speed boost when bunny hopping (multiplier) */
    private static final double BHOP_BOOST = 1.25;

    /** Maximum horizontal speed cap (blocks/tick). ~4x sprint speed */
    private static final double MAX_SPEED = 1.2;

    /** Minimum speed to trigger bhop mechanics */
    private static final double MIN_BHOP_SPEED = 0.08;

    /** How much to counteract ground friction (1.0 = no friction, 0.0 = normal friction) */
    private static final double FRICTION_COUNTER = 0.85;

    // === DEBUG MODE ===
    private static final boolean DEBUG_MODE = false;

    /** Vanilla sprint speed for comparison (blocks/tick) */
    private static final double VANILLA_SPRINT_SPEED = 0.28;
    private static final double VANILLA_SPRINT_JUMP_SPEED = 0.36;

    // Debug state tracking
    private static int lastBhopTick = 0;
    private static int lastStrafeTick = 0;
    private static double sessionMaxSpeed = 0;

    /**
     * Called from network packet to set client-side state.
     */
    @OnlyIn(Dist.CLIENT)
    public static void setClientHasQuakeMovement(boolean has) {
        clientHasQuakeMovement = has;
        CosmicCore.LOGGER.info("Client quake movement set to: {}", has);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Run at START of tick so our velocity changes happen before Minecraft processes movement
        if (event.phase != TickEvent.Phase.START) return;

        Player player = event.player;

        // Only run on client side - server movement causes rubber-banding
        if (!player.level().isClientSide()) return;

        // Check client-side state
        if (!clientHasQuakeMovement) return;

        // Only apply to the local player
        if (player != Minecraft.getInstance().player) return;

        // Don't apply in fluids (water, lava) - prevents mach 11 swimming
        if (player.isInWater() || player.isInLava() || player.isInFluidType((fluidType, height) -> height > 0.0))
            return;

        UUID uuid = player.getUUID();
        boolean onGround = player.onGround();
        boolean wasGrounded = wasOnGround.getOrDefault(uuid, true);

        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeed = getHorizontalSpeed(motion);

        // Track air time
        if (!onGround) {
            airTime.merge(uuid, 1, Integer::sum);
        } else {
            airTime.put(uuid, 0);
        }

        // === BUNNY HOP: Just landed while moving fast ===
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

        // === AIR STRAFING: In the air with movement input ===
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

        // === GROUND MOVEMENT: Reduce friction to preserve speed ===
        if (onGround && horizontalSpeed > MIN_BHOP_SPEED) {
            motion = reduceGroundFriction(player, motion, horizontalSpeed);
            player.setDeltaMovement(motion);
        }

        // === DEBUG: Unified display ===
        if (DEBUG_MODE) {
            double speed = getHorizontalSpeed(player.getDeltaMovement());
            if (speed > sessionMaxSpeed) {
                sessionMaxSpeed = speed;
            }

            // Build status line
            StringBuilder status = new StringBuilder();

            // Show speed with color
            ChatFormatting speedColor = getSpeedColor(speed);
            String speedMultiple = String.format("%.1fx", speed / VANILLA_SPRINT_SPEED);

            // Recent events (within last 20 ticks)
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

            // Show max speed achieved
            if (sessionMaxSpeed > VANILLA_SPRINT_JUMP_SPEED) {
                status.append(String.format(" §7| Max: §d%.2f", sessionMaxSpeed));
            }

            // Ground state indicator
            status.append(onGround ? " §7[G]" : " §e[AIR]");

            // Input indicator
            float fwd = player.zza;
            float strafe = player.xxa;
            if (Math.abs(fwd) > 0.01 || Math.abs(strafe) > 0.01) {
                status.append(String.format(" §8[%.1f/%.1f]", fwd, strafe));
            }

            player.displayClientMessage(Component.literal(status.toString()), true);
        }

        // Update ground state for next tick
        wasOnGround.put(uuid, onGround);
    }

    /**
     * Check if the player has the Quake Movement bargain active (server-side check).
     */
    public static boolean hasQuakeMovement(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId()))
                .orElse(false);
    }

    /**
     * Apply bunny hop boost when landing.
     * This is the key to maintaining speed - boost slightly on each landing.
     */
    private static Vec3 applyBunnyHop(Player player, Vec3 motion, double currentSpeed) {
        // Boost horizontal velocity
        double boostedSpeed = Math.min(currentSpeed * BHOP_BOOST, MAX_SPEED);

        if (boostedSpeed > currentSpeed && currentSpeed > 0) {
            double scale = boostedSpeed / currentSpeed;
            return new Vec3(motion.x * scale, motion.y, motion.z * scale);
        }

        return motion;
    }

    /**
     * Apply Quake-style air acceleration.
     * This is where the magic happens - allows gaining speed by strafing.
     */
    private static Vec3 applyAirAcceleration(Player player, Vec3 motion) {
        // Get player's input direction
        float forward = player.zza; // Forward/backward input (-1 to 1)
        float strafe = player.xxa;  // Left/right input (-1 to 1)

        // No input = no acceleration
        if (Math.abs(forward) < 0.01 && Math.abs(strafe) < 0.01) {
            return motion;
        }

        // Get player's facing direction (horizontal only)
        float yaw = player.getYRot() * ((float) Math.PI / 180f);

        // Calculate wish direction based on input
        // Forward: -sin(yaw), cos(yaw)
        // Right (strafe positive = right): cos(yaw), sin(yaw)
        double wishX = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
        double wishZ = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;

        // Normalize wish direction
        double wishLength = Math.sqrt(wishX * wishX + wishZ * wishZ);
        if (wishLength > 0.01) {
            wishX /= wishLength;
            wishZ /= wishLength;
        } else {
            return motion;
        }

        // Current velocity in wish direction (dot product)
        double currentWishSpeed = motion.x * wishX + motion.z * wishZ;

        // Calculate how much we can add
        double addSpeed = AIR_WISH_SPEED - currentWishSpeed;

        if (addSpeed <= 0) {
            return motion; // Already going fast enough in wish direction
        }

        // Calculate acceleration (scaled by tick time ~0.05)
        double accelSpeed = AIR_ACCELERATE * 0.05 * AIR_WISH_SPEED;

        // Cap the acceleration
        if (accelSpeed > addSpeed) {
            accelSpeed = addSpeed;
        }

        // Apply acceleration in wish direction
        double newX = motion.x + accelSpeed * wishX;
        double newZ = motion.z + accelSpeed * wishZ;

        // Cap max speed
        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (newSpeed > MAX_SPEED) {
            double scale = MAX_SPEED / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        return new Vec3(newX, motion.y, newZ);
    }

    /**
     * Reduce ground friction to help maintain speed.
     * Minecraft's default friction is very high - we counteract it.
     */
    private static Vec3 reduceGroundFriction(Player player, Vec3 motion, double currentSpeed) {
        // Minecraft applies friction as: velocity *= 0.91 * slipperiness (roughly 0.6 on grass)
        // So velocity gets multiplied by ~0.546 each tick on ground
        // We want to counteract most of that

        // Calculate boost to counteract friction
        // Normal friction: v *= 0.546
        // We want: v *= 0.546 * boost = ~0.9 or higher
        double boost = 1.0 + FRICTION_COUNTER;

        double newX = motion.x * boost;
        double newZ = motion.z * boost;

        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);

        // Cap at max speed
        if (newSpeed > MAX_SPEED) {
            double scale = MAX_SPEED / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        // Don't go faster than we were (unless bhop just happened)
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

    /**
     * Clean up player data when they log out.
     */
    public static void removePlayer(UUID uuid) {
        wasOnGround.remove(uuid);
        airTime.remove(uuid);
    }

    // === DEBUG HELPERS ===

    @OnlyIn(Dist.CLIENT)
    private static void sendDebugMessage(Player player, ChatFormatting color, String format, Object... args) {
        if (!DEBUG_MODE) return;
        String msg = String.format(format, args);
        player.displayClientMessage(
                Component.literal("[QUAKE] ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(msg).withStyle(color)),
                true // Action bar
        );
    }

    private static ChatFormatting getSpeedColor(double speed) {
        if (speed >= 1.0) return ChatFormatting.LIGHT_PURPLE; // Insane
        if (speed >= 0.6) return ChatFormatting.RED;          // Very fast
        if (speed >= VANILLA_SPRINT_JUMP_SPEED) return ChatFormatting.YELLOW; // Above sprint-jump
        if (speed >= VANILLA_SPRINT_SPEED) return ChatFormatting.GREEN;       // Above sprint
        return ChatFormatting.GRAY;                                            // Normal
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
