package com.ghostipedia.cosmiccore.common.reflection.bargain.impl;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.ghostipedia.cosmiccore.common.reflection.ReflectionCapability;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public class QuakeMovementHandler {

    private static final Map<UUID, Boolean> wasOnGround = new HashMap<>();
    private static final Map<UUID, Integer> airTime = new HashMap<>();
    private static final Map<UUID, Boolean> wasJumping = new HashMap<>();

    private static boolean clientHasQuakeMovement = false;

    // Movement constants
    private static final double GROUND_ACCELERATE = 15.0;
    private static final double AIR_ACCELERATE = 200.0;
    private static final double AIR_WISH_SPEED = 0.25;
    private static final double BHOP_BOOST = 1.12;
    private static final double HARD_CAP_SPEED = 1.25;    // 25 b/s
    private static final double BHOP_SOFT_CAP = 0.5;      // 10 b/s - pure bhop caps here
    private static final double SOFT_CAP_SPEED = 1.0;     // 20 b/s
    private static final double SOFT_CAP_DEGEN = 0.7;
    private static final double MIN_BHOP_SPEED = 0.10;
    private static final int MIN_BHOP_AIRTIME = 5;
    private static final double TRIMP_MULTIPLIER = 1.6;

    // Debug
    private static final boolean DEBUG_MODE = false;
    private static final double VANILLA_SPRINT_SPEED = 0.28;
    private static final double VANILLA_SPRINT_JUMP_SPEED = 0.36;

    private static int lastBhopTick = 0;
    private static int lastStrafeTick = 0;
    private static int lastTrimpTick = 0;
    private static double sessionMaxSpeed = 0;

    @OnlyIn(Dist.CLIENT)
    public static void setClientHasQuakeMovement(boolean has) {
        clientHasQuakeMovement = has;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean getClientHasQuakeMovement() {
        Player player = Minecraft.getInstance().player;
        return player != null && canUseGlobestriderMovement(player);
    }

    public static boolean canUseGlobestriderMovement(Player player) {
        return hasGlobestriderPalms(player);
    }

    public static boolean hasGlobestriderPalms(Player player) {
        Optional<ICuriosItemHandler> cap = CuriosApi.getCuriosInventory(player);
        if (cap.isEmpty()) return false;
        Optional<ICurioStacksHandler> handler = cap.get().getStacksHandler("hands");
        if (handler.isEmpty()) return false;
        IDynamicStackHandler stacks = handler.get().getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            if (stacks.getStackInSlot(i).is(CosmicItems.PALMS_OF_THE_GLOBESTRIDER.asItem())) {
                return true;
            }
        }
        return false;
    }

    public static double getHardCapSpeed() {
        return HARD_CAP_SPEED;
    }

    public static double getBhopSoftCap() {
        return BHOP_SOFT_CAP;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;
        if (player != Minecraft.getInstance().player) return;
        if (!canUseGlobestriderMovement(player)) return;

        CelesteDashHandler.clientTick(player);

        if (player.isInWater() || player.isInLava() || player.isInFluidType((fluidType, height) -> height > 0.0))
            return;
        if (player.getAbilities().flying || player.isFallFlying())
            return;
        if (CelesteDashHandler.isDashing(player))
            return;

        UUID uuid = player.getUUID();
        boolean onGround = QuakeGrounding.isMovementGrounded(player);
        boolean wasGrounded = wasOnGround.getOrDefault(uuid, true);

        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeed = getHorizontalSpeed(motion);
        int airborneTicks = airTime.getOrDefault(uuid, 0);

        if (!onGround) {
            airborneTicks++;
            airTime.put(uuid, airborneTicks);
        }

        boolean justJumped = !onGround && wasGrounded && motion.y > 0;
        if (justJumped) {
            wasJumping.put(uuid, true);

            if (player.isCrouching() && horizontalSpeed > VANILLA_SPRINT_SPEED) {
                motion = applyTrimp(player, motion, horizontalSpeed);
                player.setDeltaMovement(motion);
                horizontalSpeed = getHorizontalSpeed(motion);
                if (DEBUG_MODE) lastTrimpTick = player.tickCount;
            }
        }

        if (onGround && !wasGrounded) {
            wasJumping.put(uuid, false);
        }

        // Bhop on landing (requires minimum airtime to filter stair/step spam)
        if (onGround && !wasGrounded && horizontalSpeed > MIN_BHOP_SPEED) {
            if (airborneTicks >= MIN_BHOP_AIRTIME) {
                double oldSpeed = horizontalSpeed;
                motion = applyBunnyHop(player, motion, horizontalSpeed);
                player.setDeltaMovement(motion);
                double newSpeed = getHorizontalSpeed(motion);
                horizontalSpeed = newSpeed;

                if (newSpeed > oldSpeed) {
                    spawnBhopParticles(player, 4);
                    if (DEBUG_MODE) lastBhopTick = player.tickCount;
                }
            }
        }
        if (onGround) {
            airTime.put(uuid, 0);
        }

        // Air strafe
        if (!onGround) {
            double oldSpeed = horizontalSpeed;
            motion = applyAirAcceleration(player, motion);
            player.setDeltaMovement(motion);
            double newSpeed = getHorizontalSpeed(motion);

            if (DEBUG_MODE && newSpeed > oldSpeed + 0.002) {
                lastStrafeTick = player.tickCount;
            }
        }

        // Hard cap
        motion = player.getDeltaMovement();
        horizontalSpeed = getHorizontalSpeed(motion);
        if (horizontalSpeed > HARD_CAP_SPEED) {
            motion = applyHardCap(motion, horizontalSpeed);
            player.setDeltaMovement(motion);
        }

        // Debug HUD
        if (DEBUG_MODE) {
            displayDebugHud(player, onGround);
        }

        wasOnGround.put(uuid, onGround);
    }

    public static boolean hasQuakeMovement(Player player) {
        return ReflectionCapability.get(player)
                .map(reflection -> reflection.hasBargain(QuakeMovementBargain.INSTANCE.getId()))
                .orElse(false);
    }

    private static Vec3 applyBunnyHop(Player player, Vec3 motion, double currentSpeed) {
        double targetSpeed = currentSpeed;

        if (currentSpeed < SOFT_CAP_SPEED) {
            double speedRatio = Math.min(currentSpeed / BHOP_SOFT_CAP, 1.0);
            double dynamicMultiplier = 1.0 + (BHOP_BOOST - 1.0) * (1.0 - speedRatio * speedRatio);
            double boostedSpeed = currentSpeed * dynamicMultiplier;
            if (boostedSpeed > SOFT_CAP_SPEED) {
                double excess = boostedSpeed - SOFT_CAP_SPEED;
                boostedSpeed = SOFT_CAP_SPEED + excess * SOFT_CAP_DEGEN;
            }
            targetSpeed = boostedSpeed;
        }

        targetSpeed = Math.min(targetSpeed, HARD_CAP_SPEED);
        targetSpeed = Math.max(targetSpeed, Math.min(currentSpeed, HARD_CAP_SPEED));

        if (Math.abs(targetSpeed - currentSpeed) > 0.001 && currentSpeed > 0) {
            double scale = targetSpeed / currentSpeed;
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
        accelSpeed = Math.min(accelSpeed, addSpeed);

        double currentSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (currentSpeed > SOFT_CAP_SPEED) {
            accelSpeed *= SOFT_CAP_DEGEN;
        }

        double newX = motion.x + accelSpeed * wishX;
        double newZ = motion.z + accelSpeed * wishZ;

        double newSpeed = Math.sqrt(newX * newX + newZ * newZ);
        if (newSpeed > HARD_CAP_SPEED) {
            double scale = HARD_CAP_SPEED / newSpeed;
            newX *= scale;
            newZ *= scale;
        }

        return new Vec3(newX, motion.y, newZ);
    }

    private static Vec3 applyTrimp(Player player, Vec3 motion, double currentSpeed) {
        if (currentSpeed <= VANILLA_SPRINT_SPEED) {
            return motion;
        }

        double speedBonus = Math.min((currentSpeed / VANILLA_SPRINT_SPEED - 1.0) * 0.5, 1.0);
        double horizontalReduction = 1.0 / TRIMP_MULTIPLIER;
        double verticalBoost = speedBonus * currentSpeed * TRIMP_MULTIPLIER;

        spawnBhopParticles(player, 8);

        return new Vec3(
                motion.x * horizontalReduction,
                motion.y + verticalBoost,
                motion.z * horizontalReduction);
    }

    private static Vec3 applyHardCap(Vec3 motion, double currentSpeed) {
        double scale = HARD_CAP_SPEED / currentSpeed;
        return new Vec3(motion.x * scale, motion.y, motion.z * scale);
    }

    private static double getHorizontalSpeed(Vec3 motion) {
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnBhopParticles(Player player, int count) {
        if (count < 1) return;

        int x = Mth.floor(player.getX());
        int y = Mth.floor(player.getY() - 0.2);
        int z = Mth.floor(player.getZ());

        var blockState = player.level().getBlockState(new BlockPos(x, y, z));
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < count; i++) {
                double px = player.getX() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth();
                double pz = player.getZ() + (player.getRandom().nextFloat() - 0.5) * player.getBbWidth();
                double py = player.getBoundingBox().minY + 0.1;

                Vec3 vel = player.getDeltaMovement();
                player.level().addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                        px, py, pz, -vel.x * 4.0, 1.5, -vel.z);
            }
        }
    }

    private static void displayDebugHud(Player player, boolean onGround) {
        double speed = getHorizontalSpeed(player.getDeltaMovement());
        if (speed > sessionMaxSpeed) {
            sessionMaxSpeed = speed;
        }

        StringBuilder status = new StringBuilder();
        ChatFormatting speedColor = getSpeedColor(speed);
        String speedMultiple = String.format("%.1fx", speed / VANILLA_SPRINT_SPEED);

        boolean recentBhop = (player.tickCount - lastBhopTick) < 20;
        boolean recentStrafe = (player.tickCount - lastStrafeTick) < 10;
        boolean recentTrimp = (player.tickCount - lastTrimpTick) < 20;

        if (recentTrimp) status.append("§d[TRIMP!] ");
        if (recentBhop) status.append("§a[BHOP!] ");
        if (recentStrafe && !onGround) status.append("§b[STRAFE] ");

        if (CelesteDashHandler.isDashing(player)) {
            status.append("§c[DASHING] ");
        } else if (CelesteDashHandler.canDashNow(player)) {
            status.append("§e[DASH OK] ");
        } else {
            status.append("§7[DASH CD] ");
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

    public static void removePlayer(UUID uuid) {
        wasOnGround.remove(uuid);
        airTime.remove(uuid);
        wasJumping.remove(uuid);
        CelesteDashHandler.removePlayer(uuid);
    }

    private static ChatFormatting getSpeedColor(double speed) {
        if (speed >= 1.5) return ChatFormatting.LIGHT_PURPLE;
        if (speed >= 1.0) return ChatFormatting.RED;
        if (speed >= 0.6) return ChatFormatting.YELLOW;
        if (speed >= VANILLA_SPRINT_SPEED) return ChatFormatting.GREEN;
        return ChatFormatting.GRAY;
    }
}
