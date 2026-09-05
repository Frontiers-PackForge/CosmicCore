package com.ghostipedia.cosmiccore.common.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.LeylineDeploymentFinishPacket;
import com.ghostipedia.cosmiccore.common.network.packet.LeylineDeploymentStartPacket;
import com.ghostipedia.cosmiccore.common.transmission.PowerTowerChain;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class LeylineDeploymentService {

    private static final Map<UUID, Deployment> ACTIVE = new HashMap<>();
    private static final String RESERVED_PACKAGE = "cosmiccore:leyline_reserved_package";
    private static final String RESERVED_COIL = "cosmiccore:leyline_reserved_coil";

    private LeylineDeploymentService() {}

    public static void begin(ServerPlayer player, InteractionHand hand, BlockHitResult requestedHit) {
        if (ACTIVE.containsKey(player.getUUID()) || !player.isAlive() || player.isSpectator()) return;
        ItemStack stack = player.getItemInHand(hand);
        if (!LeylineDeploymentBlueprints.isPackage(stack) || player.getCooldowns().isOnCooldown(stack.getItem()))
            return;
        player.getCooldowns().addCooldown(stack.getItem(), 6);
        BlockHitResult hit = LeylineDeploymentTarget.trace(player, 1);
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(requestedHit.getBlockPos()) ||
                hit.getDirection() != requestedHit.getDirection())
            return;
        ServerLevel level = player.serverLevel();
        Direction facing = player.getDirection().getOpposite();
        try {
            LeylinePrefab.migrate(stack, level);
            var blueprint = LeylineDeploymentBlueprints.forPackage(stack, facing, level);
            if (blueprint == null) {
                fail(player, stack, "invalid");
                return;
            }
            var plan = blueprint.planOnBase(LeylineDeploymentTarget.anchor(player, hit));
            if (!preflight(player, stack, hit.getDirection(), plan)) return;
            if (ACTIVE.values().stream().anyMatch(deployment -> deployment.level == level &&
                    deployment.animation.bounds().intersects(LeylineDeploymentAnimation.bounds(plan)))) {
                fail(player, stack, "busy");
                return;
            }
            var chain = PowerTowerChain.prepare(player, hand, plan);
            if (chain != null && !chain.validate(player, plan, facing)) return;
            Deployment deployment = new Deployment(player, stack, hit.getDirection(), plan,
                    LeylineDeploymentAnimation.resolve(level, plan), facing, chain);
            if (!player.getAbilities().instabuild) {
                refund(player);
                player.getPersistentData().put(RESERVED_PACKAGE, deployment.stack.save(player.registryAccess()));
                stack.shrink(1);
                if (chain != null) {
                    player.getPersistentData().put(RESERVED_COIL, chain.coil().save(player.registryAccess()));
                    chain.consume();
                }
            }
            ACTIVE.put(player.getUUID(), deployment);
            for (ServerPlayer observer : level.players()) {
                if (deployment.visibleTo(observer)) {
                    deployment.observe(observer);
                }
            }
            deployment.observe(player);
        } catch (RuntimeException exception) {
            cancel(player);
            refund(player);
            CosmicCore.LOGGER.error("Unable to start leyline deployment", exception);
            fail(player, stack, "invalid");
        }
    }

    private static boolean preflight(ServerPlayer player, ItemStack packageStack, Direction face,
                                     LeylineDeploymentPlan plan) {
        var preflight = LeyLineDeploymentCheck.validateFootprint(player.level(), plan);
        if (!preflight.valid()) {
            var failure = preflight.failures().getFirst();
            fail(player, packageStack, "preflight." + failure.kind().name().toLowerCase(Locale.ROOT),
                    failure.pos().toShortString());
            return false;
        }
        if (plan.worldPlacements().stream()
                .anyMatch(placement -> !player.level().mayInteract(player, placement.pos()) ||
                        !player.mayUseItemAt(placement.pos(), face, packageStack))) {
            fail(player, packageStack, "permission");
            return false;
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var iterator = ACTIVE.values().iterator();
        while (iterator.hasNext()) {
            Deployment deployment = iterator.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(deployment.owner);
            if (player == null || !player.isAlive() || player.serverLevel() != deployment.level) {
                deployment.finish(false, player);
                iterator.remove();
                continue;
            }
            if (deployment.level.getGameTime() < deployment.startTick + LeylineDeploymentAnimation.IMPACT_TICK) {
                if (deployment.level.getGameTime() % 10 == 0) {
                    for (ServerPlayer observer : deployment.level.players()) {
                        if (deployment.visibleTo(observer)) deployment.observe(observer);
                    }
                }
                continue;
            }
            iterator.remove();
            boolean committed = false;
            try {
                if (preflight(player, deployment.stack, deployment.face, deployment.plan) &&
                        (deployment.chain == null ||
                                deployment.chain.validate(player, deployment.plan, deployment.facing))) {
                    var result = LeylineDeploymentExecutor.deployAtomically(deployment.level, deployment.plan,
                            deployment.owner, (level, pos, existing, placing) -> level.mayInteract(player, pos) &&
                                    player.mayUseItemAt(pos, deployment.face, deployment.stack),
                            (level, plan) -> new GtmMultiblockFormationValidator().validate(level, plan) &&
                                    (deployment.chain == null || deployment.chain.connect(player, plan)),
                            player, deployment.face);
                    committed = result.committed();
                    if (committed) {
                        PowerTowerChain.placed(player, deployment.plan.controllerPos());
                        player.displayClientMessage(Component.translatable("cosmiccore.deployment.success",
                                LeylinePrefab.machineName(deployment.stack))
                                .withStyle(ChatFormatting.GREEN), true);
                    } else {
                        fail(player, deployment.stack, result.status().name().toLowerCase(Locale.ROOT));
                    }
                }
            } catch (RuntimeException exception) {
                CosmicCore.LOGGER.error("Unable to finish leyline deployment {}", deployment.id, exception);
                fail(player, deployment.stack, "invalid");
            }
            if (!committed && deployment.chain != null) deployment.chain.rollback(player);
            deployment.finish(committed, player);
        }
    }

    @SubscribeEvent
    public static void watch(ChunkWatchEvent.Sent event) {
        for (Deployment deployment : ACTIVE.values()) {
            if (deployment.level == event.getLevel() &&
                    deployment.visibleTo(event.getPlayer())) {
                deployment.observe(event.getPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        ACTIVE.clear();
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        for (Deployment deployment : ACTIVE.values()) {
            deployment.finish(false, event.getServer().getPlayerList().getPlayer(deployment.owner));
        }
        ACTIVE.clear();
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !ACTIVE.containsKey(player.getUUID())) refund(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cancel(player);
    }

    private static void cancel(ServerPlayer player) {
        Deployment deployment = ACTIVE.remove(player.getUUID());
        if (deployment != null) deployment.finish(false, player);
    }

    private static void refund(ServerPlayer player) {
        var data = player.getPersistentData();
        boolean returned = false;
        Component machine = LeylinePrefab.machineName(ItemStack.parseOptional(player.registryAccess(),
                data.getCompound(RESERVED_PACKAGE)));
        for (String key : new String[] { RESERVED_PACKAGE, RESERVED_COIL }) {
            if (!data.contains(key)) continue;
            var stack = ItemStack.parseOptional(player.registryAccess(), data.getCompound(key));
            if (stack.isEmpty()) continue;
            ItemHandlerHelper.giveItemToPlayer(player, stack);
            data.remove(key);
            returned = true;
        }
        if (!returned) return;
        player.displayClientMessage(Component.translatable("cosmiccore.deployment.refunded", machine)
                .withStyle(ChatFormatting.YELLOW), false);
    }

    private static void fail(ServerPlayer player, ItemStack stack, String reason, Object... arguments) {
        Object[] named = new Object[arguments.length + 1];
        named[0] = LeylinePrefab.machineName(stack);
        System.arraycopy(arguments, 0, named, 1, arguments.length);
        player.displayClientMessage(Component.translatable("cosmiccore.deployment.error." + reason,
                named).withStyle(ChatFormatting.RED), true);
    }

    private static final class Deployment {

        private final UUID id = UUID.randomUUID();
        private final UUID owner;
        private final ServerLevel level;
        private final ItemStack stack;
        private final Direction face;
        private final Direction facing;
        private final PowerTowerChain.Request chain;
        private final LeylineDeploymentPlan plan;
        private final LeylineDeploymentAnimation animation;
        private final long startTick;
        private final LeylineDeploymentStartPacket startPacket;
        private final Set<UUID> observers = new HashSet<>();

        private Deployment(ServerPlayer player, ItemStack stack, Direction face,
                           LeylineDeploymentPlan plan, LeylineDeploymentAnimation animation, Direction facing,
                           PowerTowerChain.Request chain) {
            this.owner = player.getUUID();
            this.level = player.serverLevel();
            this.stack = stack.copyWithCount(1);
            this.face = face;
            this.facing = facing;
            this.chain = chain;
            this.plan = plan;
            this.animation = animation;
            this.startTick = level.getGameTime() + LeylineDeploymentAnimation.START_DELAY_TICKS;
            this.startPacket = new LeylineDeploymentStartPacket(id, level.dimension().location(),
                    plan.blueprint().id(), plan.controllerPos(), facing, startTick, animation.openingY());
        }

        private void observe(ServerPlayer player) {
            if (observers.add(player.getUUID())) CCoreNetwork.sendToPlayer(player, startPacket);
        }

        private boolean visibleTo(ServerPlayer player) {
            int chunks = Math.min(player.requestedViewDistance(), level.getServer().getPlayerList().getViewDistance());
            return animation.bounds().inflate((Math.max(2, chunks) + 1) * 16).contains(player.position());
        }

        private void finish(boolean committed, ServerPlayer ownerPlayer) {
            if (ownerPlayer != null) {
                if (committed) {
                    ownerPlayer.getPersistentData().remove(RESERVED_PACKAGE);
                    ownerPlayer.getPersistentData().remove(RESERVED_COIL);
                } else refund(ownerPlayer);
            }
            for (UUID observer : observers) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(observer);
                if (player != null && player.serverLevel() == level) {
                    CCoreNetwork.sendToPlayer(player, new LeylineDeploymentFinishPacket(id, committed));
                }
            }
        }
    }
}
