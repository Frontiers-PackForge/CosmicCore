package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CelesteDashHandler {

    // Constants
    private static final double DASH_SPEED = 0.6;
    private static final int DASH_DURATION = 4;
    private static final int DASH_COOLDOWN = 10;
    private static final int MAX_CHARGES = 1;
    private static final int COYOTE_TIME = 6;
    private static final int INPUT_BUFFER = 6;
    private static final double WAVEDASH_SPEED_BOOST = 0.25;
    private static final double SUPER_DASH_BONUS = 0.15;
    private static final int SUPER_DASH_WINDOW = 15;
    private static final double MIN_PRESERVE_SPEED = 0.20;
    private static final double MAX_VERTICAL_RATIO = 0.7;
    private static final double HARD_CAP = 1.25;

    private static final Map<UUID, DashState> playerStates = new HashMap<>();

    public static class DashState {

        public int dashCharges = MAX_CHARGES;
        public int dashCooldown = 0;
        public int dashDuration = 0;
        public Vec3 dashDirection = Vec3.ZERO;
        public boolean isDashing = false;

        public int ticksSinceGrounded = 0;
        public boolean wasGrounded = true;
        public int groundedTicks = 0;

        public int bufferedDashTicks = 0;
        public float bufferedXRot = 0;
        public float bufferedYRot = 0;
        public float bufferedForward = 0;
        public float bufferedStrafe = 0;

        public Vec3 preDashVelocity = Vec3.ZERO;
        public double preDashSpeed = 0;
        public int ticksSinceDashEnd = 999;

        public boolean canSuperDash = false;
        public int superDashWindow = 0;

        public boolean canWavedash = false;
        public int wavedashWindow = 0;
        public double wavedashStoredSpeed = 0;

        public int groundDashStreak = 0;
        public boolean jumpedSinceLastDash = false;
        public int groundedWithoutJumpTicks = 0;
    }

    public static DashState getState(Player player) {
        return playerStates.computeIfAbsent(player.getUUID(), k -> new DashState());
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean tryDash(Player player) {
        DashState state = getState(player);

        if (canDash(player, state)) {
            performDash(player, state, player.getXRot(), player.getYRot(), player.zza, player.xxa);
            return true;
        }

        if (state.dashCooldown > 0 && state.dashCooldown <= INPUT_BUFFER) {
            state.bufferedDashTicks = INPUT_BUFFER;
            state.bufferedXRot = player.getXRot();
            state.bufferedYRot = player.getYRot();
            state.bufferedForward = player.zza;
            state.bufferedStrafe = player.xxa;
        }

        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Player player) {
        DashState state = getState(player);

        boolean justJumped = !player.onGround() && state.wasGrounded && player.getDeltaMovement().y > 0.1;

        if (player.onGround()) {
            state.ticksSinceGrounded = 0;
            state.wasGrounded = true;
            state.groundedWithoutJumpTicks++;

            if (state.dashCharges < MAX_CHARGES && !state.isDashing) {
                state.dashCharges = MAX_CHARGES;
            }
            state.groundedTicks++;
        } else {
            if (state.wasGrounded && player.getDeltaMovement().y > 0.1) {
                state.jumpedSinceLastDash = true;
                state.groundDashStreak = 0;
                state.groundedWithoutJumpTicks = 0;
            }
            if (state.wasGrounded) {
                state.wasGrounded = false;
            }
            state.ticksSinceGrounded++;
            state.groundedTicks = 0;
            state.groundedWithoutJumpTicks = 0;
        }

        if (state.dashCooldown > 0) state.dashCooldown--;

        if (state.dashDuration > 0) {
            state.dashDuration--;
            if (state.dashDuration == 0) {
                onDashEnd(player, state);
            }
        }

        if (state.superDashWindow > 0) {
            state.superDashWindow--;
            if (state.superDashWindow == 0) {
                state.canSuperDash = false;
            }
        }

        if (state.wavedashWindow > 0) {
            state.wavedashWindow--;
            if (justJumped) {
                triggerWavedash(player, state);
            }
            if (state.wavedashWindow == 0) {
                state.canWavedash = false;
            }
        }

        if (!state.isDashing) state.ticksSinceDashEnd++;

        if (state.bufferedDashTicks > 0) {
            if (canDash(player, state)) {
                performDash(player, state, state.bufferedXRot, state.bufferedYRot,
                        state.bufferedForward, state.bufferedStrafe);
                state.bufferedDashTicks = 0;
            } else {
                state.bufferedDashTicks--;
            }
        }

        if (state.isDashing && state.dashDuration > 0) {
            applyDashMovement(player, state);
        }
    }

    public static void executeDashServer(ServerPlayer player, float xRot, float yRot, float forwardInput,
                                         float strafeInput) {
        if (player.isInWater() || player.isInLava()) return;
        if (player.getAbilities().flying || player.isFallFlying()) return;

        Vec3 dashDir = calculateDashDirection(player, xRot, yRot);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.5f, 1.8f);

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double progress = i / 8.0;
                double x = player.getX() - dashDir.x * progress * 2;
                double y = player.getY() + 0.5 - dashDir.y * progress * 2;
                double z = player.getZ() - dashDir.z * progress * 2;
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    private static boolean canDash(Player player, DashState state) {
        if (state.isDashing) return false;
        if (state.dashCharges <= 0) return false;
        if (state.dashCooldown > 0) return false;
        if (player.isInWater() || player.isInLava()) return false;
        if (player.getAbilities().flying || player.isFallFlying()) return false;
        return true;
    }

    private static void performDash(Player player, DashState state, float xRot, float yRot, float forwardInput,
                                    float strafeInput) {
        state.preDashVelocity = player.getDeltaMovement();
        state.preDashSpeed = Math.sqrt(state.preDashVelocity.x * state.preDashVelocity.x +
                state.preDashVelocity.z * state.preDashVelocity.z);

        Vec3 dashDir = calculateDashDirection(player, xRot, yRot);
        boolean isSuperDash = state.canSuperDash && state.preDashSpeed > MIN_PRESERVE_SPEED;

        // Ground spam penalty
        boolean isGroundSpam = player.onGround() && !state.jumpedSinceLastDash;
        boolean prolongedGrounding = state.groundedWithoutJumpTicks > 10;
        double groundPenalty = 1.0;

        if (isGroundSpam || prolongedGrounding) {
            state.groundDashStreak++;
            if (state.groundDashStreak == 1) {
                groundPenalty = 0.8;
            } else if (state.groundDashStreak == 2) {
                groundPenalty = 0.5;
            } else {
                groundPenalty = 0.3;
            }
        } else if (state.jumpedSinceLastDash) {
            state.groundDashStreak = 0;
        }

        state.jumpedSinceLastDash = false;
        state.groundedWithoutJumpTicks = 0;

        double speed = DASH_SPEED;
        if (isSuperDash) {
            speed += SUPER_DASH_BONUS;
        }
        speed *= groundPenalty;
        speed = Math.min(speed, DASH_SPEED + SUPER_DASH_BONUS);

        Vec3 dashVelocity = dashDir.scale(speed);

        double yVel = dashVelocity.y;
        if (state.preDashVelocity.y > 0 && dashDir.y >= -0.1) {
            yVel = Math.max(yVel, state.preDashVelocity.y * 0.6);
        }

        player.setDeltaMovement(dashVelocity.x, yVel, dashVelocity.z);

        state.dashDirection = dashDir;
        state.isDashing = true;
        state.dashDuration = DASH_DURATION;
        state.dashCooldown = DASH_COOLDOWN;
        state.dashCharges--;
        state.canSuperDash = false;
        state.ticksSinceDashEnd = 0;

        player.resetFallDistance();
    }

    private static Vec3 calculateDashDirection(Player player, float xRot, float yRot) {
        double yawRad = Math.toRadians(yRot);
        double pitchRad = Math.toRadians(xRot);

        double sinYaw = Math.sin(yawRad);
        double cosYaw = Math.cos(yawRad);
        double sinPitch = Math.sin(pitchRad);
        double cosPitch = Math.cos(pitchRad);

        double lookX = -sinYaw * cosPitch;
        double lookY = -sinPitch;
        double lookZ = cosYaw * cosPitch;

        if (Math.abs(lookY) > MAX_VERTICAL_RATIO) {
            double sign = lookY > 0 ? 1 : -1;
            lookY = sign * MAX_VERTICAL_RATIO;
            double horizontalScale = Math.sqrt(1 - lookY * lookY) /
                    Math.sqrt(lookX * lookX + lookZ * lookZ + 0.0001);
            lookX *= horizontalScale;
            lookZ *= horizontalScale;
        }

        Vec3 dir = new Vec3(lookX, lookY, lookZ);
        double length = dir.length();
        if (length > 0.01) {
            return dir.scale(1.0 / length);
        }
        return new Vec3(-sinYaw, 0, cosYaw).normalize();
    }

    private static void applyDashMovement(Player player, DashState state) {
        Vec3 current = player.getDeltaMovement();
        Vec3 dashVel = state.dashDirection.scale(DASH_SPEED);

        double blend = 0.8;
        double newX = current.x * (1 - blend) + dashVel.x * blend;
        double newZ = current.z * (1 - blend) + dashVel.z * blend;

        double newY = current.y;
        if (state.dashDirection.y != 0) {
            newY = current.y * 0.5 + dashVel.y * 0.5;
        }

        player.setDeltaMovement(newX, newY, newZ);
    }

    private static void onDashEnd(Player player, DashState state) {
        state.isDashing = false;

        Vec3 velocity = player.getDeltaMovement();
        double currentHorizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        boolean onGroundNow = player.onGround();
        boolean dashedDownward = state.dashDirection.y < -0.1;
        boolean wasAirborne = state.preDashVelocity.y < -0.05 || state.preDashVelocity.y > 0.1;

        if (onGroundNow && (dashedDownward || wasAirborne) && currentHorizontalSpeed > 0.2) {
            state.canWavedash = true;
            state.wavedashWindow = 8;
            state.wavedashStoredSpeed = currentHorizontalSpeed;
        } else {
            double preserveSpeed = Math.max(currentHorizontalSpeed, state.preDashSpeed * 0.8);

            if (preserveSpeed > currentHorizontalSpeed && preserveSpeed > MIN_PRESERVE_SPEED) {
                Vec3 dir = velocity.lengthSqr() > 0.01 ? velocity.normalize() : state.dashDirection;
                Vec3 horizontalDir = new Vec3(dir.x, 0, dir.z);
                if (horizontalDir.lengthSqr() > 0.01) {
                    horizontalDir = horizontalDir.normalize();
                    player.setDeltaMovement(
                            horizontalDir.x * preserveSpeed,
                            velocity.y,
                            horizontalDir.z * preserveSpeed);
                }
            }
        }
    }

    private static void triggerWavedash(Player player, DashState state) {
        state.canWavedash = false;
        state.wavedashWindow = 0;

        Vec3 velocity = player.getDeltaMovement();
        double currentHorizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        double baseSpeed = Math.max(currentHorizontalSpeed, state.wavedashStoredSpeed);
        double wavedashSpeed = baseSpeed + WAVEDASH_SPEED_BOOST;

        if (state.preDashSpeed > MIN_PRESERVE_SPEED) {
            double chainBonus = state.preDashSpeed + WAVEDASH_SPEED_BOOST * 0.7;
            wavedashSpeed = Math.max(wavedashSpeed, chainBonus);
        }

        wavedashSpeed = Math.min(wavedashSpeed, HARD_CAP);

        Vec3 horizontalDir = new Vec3(velocity.x, 0, velocity.z);
        if (horizontalDir.lengthSqr() > 0.01) {
            horizontalDir = horizontalDir.normalize();
            player.setDeltaMovement(
                    horizontalDir.x * wavedashSpeed,
                    velocity.y,
                    horizontalDir.z * wavedashSpeed);
        }

        state.canSuperDash = true;
        state.superDashWindow = SUPER_DASH_WINDOW;
    }

    public static void removePlayer(UUID uuid) {
        playerStates.remove(uuid);
    }

    public static boolean isDashing(Player player) {
        return getState(player).isDashing;
    }

    public static int getDashCharges(Player player) {
        return getState(player).dashCharges;
    }

    public static boolean canDashNow(Player player) {
        return canDash(player, getState(player));
    }
}
