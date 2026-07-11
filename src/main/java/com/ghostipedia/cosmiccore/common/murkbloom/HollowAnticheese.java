package com.ghostipedia.cosmiccore.common.murkbloom;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.compat.hooked.HookedCompat;
import com.ghostipedia.cosmiccore.common.data.CosmicDamageTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class HollowAnticheese {

    private HollowAnticheese() {}

    public static final double SPEED_KILL_SQ = 1.0;
    public static final double NOISE_MOUNT = 6;
    public static final double NOISE_INTERACT = 2;
    public static final int FLOOD_FILL_CAP = 2048;
    public static final float AIR_KILL_DAMAGE = 100f;
    public static final double NOISE_HOOKSHOT = 22;
    public static final int HOOK_GRACE_TICKS = 100;

    private static final Map<UUID, Long> HOOK_GRACE = new HashMap<>();
    private static final Map<UUID, Boolean> HOOK_LIVE = new HashMap<>();

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        boolean fromHollow = MurkbloomServerLogic.inHollow(entity.level(), entity.getY());
        boolean toHollow = MurkbloomServerLogic.inHollow(entity.level(), event.getTargetY());
        if (!fromHollow && !toHollow) return;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        event.setCanceled(true);
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.no_teleport"), true);
        }
    }

    @SubscribeEvent
    public static void onDimensionTravel(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide || !MurkbloomServerLogic.inHollow(entity.level(), entity.getY())) return;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        event.setCanceled(true);
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("cosmiccore.abyss.no_teleport"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!MurkbloomServerLogic.inHollow(player)) return;

        if (player.isPassenger()) {
            Entity vehicle = player.getVehicle();
            player.stopRiding();
            returnVehicleToShelf(vehicle);
        }

        boolean hookLive = HookedCompat.hookActive(player);
        if (hookLive && !HOOK_LIVE.getOrDefault(player.getUUID(), false)) {
            grantHookGrace(player);
        }
        HOOK_LIVE.put(player.getUUID(), hookLive);

        if (player.getDeltaMovement().lengthSqr() > SPEED_KILL_SQ && !hookGraceActive(player)) {
            if (hookLive) {
                grantHookGrace(player);
            } else {
                player.hurt(CosmicDamageTypes.source(player.level(), CosmicDamageTypes.TOO_LOUD), Float.MAX_VALUE);
            }
        }

        if (!player.isUnderWater()) {
            player.hurt(CosmicDamageTypes.source(player.level(), CosmicDamageTypes.MURKBLOOM), AIR_KILL_DAMAGE);
        }
    }

    private static boolean hookGraceActive(ServerPlayer player) {
        Long until = HOOK_GRACE.get(player.getUUID());
        if (until == null) return false;
        if (player.level().getGameTime() > until) {
            HOOK_GRACE.remove(player.getUUID());
            return false;
        }
        return true;
    }

    private static void grantHookGrace(ServerPlayer player) {
        HOOK_GRACE.put(player.getUUID(), player.level().getGameTime() + HOOK_GRACE_TICKS);
        MurkbloomServerLogic.impulse(player, NOISE_HOOKSHOT, false, MurkbloomServerLogic.KIND_HIT);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        HOOK_GRACE.remove(event.getEntity().getUUID());
        HOOK_LIVE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos().immutable();
        if (!MurkbloomServerLogic.inHollow(level, pos.getY())) return;
        level.getServer().execute(() -> floodFill(level, pos));
    }

    private static void floodFill(ServerLevel level, BlockPos start) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(start);
        int placed = 0;
        while (!queue.isEmpty() && placed < FLOOD_FILL_CAP) {
            BlockPos pos = queue.poll();
            if (!seen.add(pos)) continue;
            if (pos.getY() > MurkbloomServerLogic.ENTRY_Y) continue;
            if (!level.getBlockState(pos).isAir()) continue;
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
            placed++;
            for (Direction direction : Direction.values()) {
                queue.add(pos.relative(direction));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (!MurkbloomServerLogic.inHollow(event.getLevel(), entity.getY())) return;

        if (entity instanceof AbstractMinecart || entity instanceof Boat) {
            returnVehicleToShelf(entity);
        } else if (entity instanceof LivingEntity) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isMounting()) return;
        if (!(event.getEntityMounting() instanceof ServerPlayer player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!MurkbloomServerLogic.inHollow(player)) return;

        event.setCanceled(true);
        MurkbloomServerLogic.impulse(player, NOISE_MOUNT, false, MurkbloomServerLogic.KIND_HIT);
        player.displayClientMessage(Component.translatable("cosmiccore.abyss.no_mount"), true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            MurkbloomServerLogic.impulse(player, NOISE_INTERACT, false, MurkbloomServerLogic.KIND_PLACE);
        }
    }

    private static void returnVehicleToShelf(Entity vehicle) {
        if (vehicle == null || vehicle instanceof Player) return;
        vehicle.setDeltaMovement(0, 0, 0);
        vehicle.resetFallDistance();
        vehicle.setPos(vehicle.getX(), MurkbloomServerLogic.ENTRY_Y + 1.0, vehicle.getZ());
    }
}
