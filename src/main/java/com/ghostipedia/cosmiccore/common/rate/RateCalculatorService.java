package com.ghostipedia.cosmiccore.common.rate;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.RateCalculatorItem;
import com.ghostipedia.cosmiccore.common.machine.trait.activity.MachineActivityRuntime;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.RateCalculatorPackets;
import com.ghostipedia.cosmiccore.utils.ItemData;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class RateCalculatorService {

    private static final String DATA = "rateCalculator";
    private static final String FIRST = RateCalculatorSelection.FIRST;
    private static final String SECOND = RateCalculatorSelection.SECOND;
    private static final String DIRECT = RateCalculatorSelection.DIRECT;
    private static final String REGIONS = RateCalculatorSelection.REGIONS;
    private static final String TOOL = "tool";
    private static final String DIMENSION = "dimension";
    private static final int MAX_AXIS = 128;
    private static final int MAX_CHUNKS = 81;
    private static final int MAX_MACHINES = 256;
    private static final int MAX_REGIONS = 32;
    private static final long TTL = 20L * 30L;
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Long> RESET_COOLDOWNS = new HashMap<>();

    private RateCalculatorService() {}

    public static void select(ServerPlayer player, ItemStack stack, BlockPos position) {
        close(player, null);
        ItemData.mutateElement(stack, DATA, tag -> {
            prepare(tag, player);
            CompoundTag candidate = tag.copy();
            Set<BlockPos> clickedMachines = candidate.contains(FIRST) ? Set.of() :
                    machine(player.serverLevel(), position);
            if (candidate.contains(FIRST)) {
                if (!RateCalculatorSelection.completeRegion(candidate, position.asLong(), MAX_REGIONS)) {
                    limit(player);
                    return;
                }
            } else if (clickedMachines.isEmpty()) {
                candidate.putLong(FIRST, position.asLong());
            } else {
                for (BlockPos controller : clickedMachines) {
                    if (!RateCalculatorSelection.addDirect(candidate, controller.asLong(), MAX_MACHINES)) {
                        limit(player);
                        return;
                    }
                }
            }
            candidate.putUUID(TOOL, tag.hasUUID(TOOL) ? tag.getUUID(TOOL) : UUID.randomUUID());
            candidate.putString(DIMENSION, player.level().dimension().location().toString());
            if (resolve(player, candidate) == null) {
                limit(player);
                return;
            }
            new HashSet<>(tag.getAllKeys()).forEach(tag::remove);
            tag.merge(candidate);
        });
    }

    public static void open(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof RateCalculatorItem)) return;
        ItemData.mutateElement(stack, DATA, tag -> prepare(tag, player));
        CompoundTag data = ItemData.readElement(stack, DATA);
        if (data.contains(FIRST)) {
            player.displayClientMessage(Component.translatable("gui.cosmiccore.rate_calculator.finish_corner"), true);
            return;
        }
        if (!data.getString(DIMENSION).equals(player.level().dimension().location().toString()) ||
                (data.getLongArray(DIRECT).length == 0 && data.getList(REGIONS, Tag.TAG_COMPOUND).isEmpty())) {
            player.displayClientMessage(Component.translatable("gui.cosmiccore.rate_calculator.select_corners"), true);
            return;
        }
        UUID tool = data.hasUUID(TOOL) ? data.getUUID(TOOL) : UUID.randomUUID();
        ItemData.mutateElement(stack, DATA, tag -> tag.putUUID(TOOL, tool));
        close(player, null);
        CompoundTag report = collect(player, data, tool);
        if (report == null) {
            player.displayClientMessage(Component.translatable("gui.cosmiccore.rate_calculator.selection_limit"), true);
            return;
        }
        CCoreNetwork.sendToPlayer(player, new RateCalculatorPackets.Open(tool, report));
    }

    private static void limit(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("gui.cosmiccore.rate_calculator.selection_limit"), true);
    }

    private static void prepare(CompoundTag tag, ServerPlayer player) {
        String dimension = player.level().dimension().location().toString();
        if (tag.contains(DIMENSION) && !tag.getString(DIMENSION).equals(dimension)) {
            tag.remove(FIRST);
            tag.remove(SECOND);
            tag.remove(DIRECT);
            tag.remove(REGIONS);
            tag.remove(DIMENSION);
            return;
        }
        RateCalculatorSelection.migrate(tag);
        tag.remove("report");
        tag.remove("savedAt");
        if (!tag.contains(DIMENSION)) tag.putString(DIMENSION, dimension);
    }

    public static void refresh(ServerPlayer player, UUID tool) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.tool.equals(tool) || session.expires < player.serverLevel().getGameTime() ||
                !session.dimension.equals(player.level().dimension()) || !holds(player, tool) ||
                player.distanceToSqr(session.center.getCenter()) > 65_536.0) {
            close(player, tool);
            return;
        }
        long now = player.serverLevel().getGameTime();
        if (now < session.nextRefresh) return;
        session.nextRefresh = now + 20;
        CompoundTag report = snapshots(player.serverLevel(), session);
        session.expires = player.serverLevel().getGameTime() + TTL;
        CCoreNetwork.sendToPlayer(player, new RateCalculatorPackets.Update(tool, report));
    }

    public static void close(ServerPlayer player, UUID tool) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || tool != null && !session.tool.equals(tool)) return;
        SESSIONS.remove(player.getUUID());
        RateCalculatorTracker.clearSubscription(session.subscription);
    }

    public static void tick(ServerPlayer player) {
        Session session = SESSIONS.get(player.getUUID());
        if (session != null && (session.expires < player.serverLevel().getGameTime() ||
                !session.dimension.equals(player.level().dimension()) || !holds(player, session.tool))) {
            close(player, null);
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (SESSIONS.isEmpty() || event.getServer().getTickCount() % 20 != 0) return;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) tick(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            close(player, null);
            RESET_COOLDOWNS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) close(player, null);
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) close(player, null);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        for (Session session : SESSIONS.values()) RateCalculatorTracker.clearSubscription(session.subscription);
        SESSIONS.clear();
        RESET_COOLDOWNS.clear();
    }

    private static boolean holds(ServerPlayer player, UUID tool) {
        return matches(player.getMainHandItem(), tool) || matches(player.getOffhandItem(), tool);
    }

    private static boolean matches(ItemStack stack, UUID tool) {
        if (!(stack.getItem() instanceof RateCalculatorItem)) return false;
        CompoundTag tag = ItemData.readElement(stack, DATA);
        return tag.hasUUID(TOOL) && tag.getUUID(TOOL).equals(tool);
    }

    public static void reset(ServerPlayer player, UUID tool) {
        Session session = SESSIONS.get(player.getUUID());
        if (!holds(player, tool) || session != null && (!session.tool.equals(tool) ||
                !session.dimension.equals(player.level().dimension())))
            return;
        long now = player.serverLevel().getGameTime();
        if (now < RESET_COOLDOWNS.getOrDefault(player.getUUID(), 0L)) return;
        RESET_COOLDOWNS.put(player.getUUID(), now + 20);
        if (session != null) session.nextReset = now + 20;
        if (session != null) {
            SESSIONS.remove(player.getUUID());
            RateCalculatorTracker.clearSubscription(session.subscription);
        }
        for (ItemStack stack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (!matches(stack, tool)) continue;
            ItemData.mutateElement(stack, DATA, RateCalculatorSelection::clear);
            CCoreNetwork.sendToPlayer(player, new RateCalculatorPackets.Clear(tool));
            return;
        }
    }

    private static CompoundTag collect(ServerPlayer player, CompoundTag data, UUID tool) {
        ResolvedSelection resolved = resolve(player, data);
        if (resolved == null) return null;
        ServerLevel level = player.serverLevel();
        UUID subscription = UUID.randomUUID();
        Session session = new Session(tool, subscription, resolved.positions, level.dimension(), player.blockPosition(),
                level.getGameTime() + TTL, level.getGameTime(), resolved.unloadedChunks);
        SESSIONS.put(player.getUUID(), session);
        CompoundTag report = snapshots(level, session);
        return report;
    }

    private static ResolvedSelection resolve(ServerPlayer player, CompoundTag data) {
        ServerLevel level = player.serverLevel();
        Set<BlockPos> positions = new HashSet<>();
        int unloadedChunks = 0;
        for (long direct : data.getLongArray(DIRECT)) {
            BlockPos position = BlockPos.of(direct);
            if (player.distanceToSqr(position.getCenter()) > 65_536.0) return null;
            if (level.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4) == null) {
                unloadedChunks++;
                continue;
            }
            positions.addAll(machine(level, position));
            if (positions.size() > MAX_MACHINES) return null;
        }
        ListTag regions = data.getList(REGIONS, Tag.TAG_COMPOUND);
        if (regions.size() > MAX_REGIONS) return null;
        for (Tag value : regions) {
            CompoundTag region = (CompoundTag) value;
            ResolvedSelection resolved = resolveRegion(player, BlockPos.of(region.getLong(FIRST)),
                    BlockPos.of(region.getLong(SECOND)));
            if (resolved == null) return null;
            positions.addAll(resolved.positions);
            unloadedChunks += resolved.unloadedChunks;
            if (positions.size() > MAX_MACHINES) return null;
        }
        return new ResolvedSelection(positions, unloadedChunks);
    }

    private static ResolvedSelection resolveRegion(ServerPlayer player, BlockPos first, BlockPos second) {
        ServerLevel level = player.serverLevel();
        int minX = Math.min(first.getX(), second.getX()), maxX = Math.max(first.getX(), second.getX());
        int minY = Math.min(first.getY(), second.getY()), maxY = Math.max(first.getY(), second.getY());
        int minZ = Math.min(first.getZ(), second.getZ()), maxZ = Math.max(first.getZ(), second.getZ());
        if (maxX - minX > MAX_AXIS || maxY - minY > MAX_AXIS || maxZ - minZ > MAX_AXIS) return null;
        BlockPos center = new BlockPos((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
        if (player.distanceToSqr(center.getCenter()) > 65_536.0) return null;
        int minChunkX = minX >> 4, maxChunkX = maxX >> 4, minChunkZ = minZ >> 4, maxChunkZ = maxZ >> 4;
        if ((maxChunkX - minChunkX + 1L) * (maxChunkZ - minChunkZ + 1L) > MAX_CHUNKS) return null;
        Set<BlockPos> positions = new HashSet<>();
        int unloadedChunks = 0;
        for (int x = minChunkX; x <= maxChunkX; x++) for (int z = minChunkZ; z <= maxChunkZ; z++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
            if (chunk == null) {
                unloadedChunks++;
                continue;
            }
            for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                if (pos.getY() < minY || pos.getY() > maxY || pos.getX() < minX || pos.getX() > maxX ||
                        pos.getZ() < minZ || pos.getZ() > maxZ)
                    continue;
                positions.addAll(machine(level, pos));
                if (positions.size() > MAX_MACHINES) return null;
            }
        }
        return new ResolvedSelection(positions, unloadedChunks);
    }

    private static Set<BlockPos> machine(ServerLevel level, BlockPos position) {
        MetaMachine machine = MetaMachine.getMachine(level, position);
        if (machine instanceof MultiblockPartMachine part) {
            Set<BlockPos> controllers = new HashSet<>();
            for (MultiblockControllerMachine controller : part.getControllers())
                controllers.add(controller.getBlockPos());
            return controllers;
        }
        return machine instanceof IRecipeLogicMachine ? Set.of(position) : Set.of();
    }

    private record ResolvedSelection(Set<BlockPos> positions, int unloadedChunks) {}

    private static CompoundTag snapshots(ServerLevel level, Session session) {
        ListTag machines = new ListTag();
        int unloaded = session.initialUnloaded;
        for (BlockPos position : session.positions) {
            if (level.getChunkSource().getChunkNow(position.getX() >> 4, position.getZ() >> 4) == null) {
                unloaded++;
                session.partial = true;
                continue;
            }
            RateCalculatorMachineSnapshot snapshot = RateCalculatorTracker.watch(session.subscription, level, position);
            if (snapshot != null) {
                CompoundTag tag = snapshot.toTag(level.registryAccess());
                MetaMachine machine = MetaMachine.getMachine(level, position);
                boolean hasActivity = false;
                if (machine != null) {
                    var activity = MachineActivityRuntime.activity(machine);
                    if (activity != null) {
                        tag.put("activity", activity.snapshot(level.registryAccess()));
                        hasActivity = true;
                    }
                }
                if (!hasActivity) {
                    CompoundTag baseline = session.baselines.get(position.asLong());
                    if (baseline == null) session.baselines.put(position.asLong(), tag.copy());
                    else if (!baseline.getString("machine").equals(tag.getString("machine"))) {
                        session.baselines.put(position.asLong(), tag.copy());
                        session.partial = true;
                    }
                }
                machines.add(tag);
            }
        }
        return RateCalculatorReport.create(level.dimension().location().toString(), machines, session.baselines,
                level.getGameTime() - session.started, session.positions.size(), unloaded, session.partial);
    }

    private static final class Session {

        private final UUID tool;
        private final UUID subscription;
        private final Set<BlockPos> positions;
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final BlockPos center;
        private final Map<Long, CompoundTag> baselines = new HashMap<>();
        private final int initialUnloaded;
        private long started;
        private long expires;
        private long nextRefresh;
        private long nextReset;
        private boolean partial;

        private Session(UUID tool, UUID subscription, Set<BlockPos> positions,
                        net.minecraft.resources.ResourceKey<Level> dimension, BlockPos center, long expires,
                        long started,
                        int initialUnloaded) {
            this.tool = tool;
            this.subscription = subscription;
            this.positions = positions;
            this.dimension = dimension;
            this.center = center;
            this.expires = expires;
            this.started = started;
            this.initialUnloaded = initialUnloaded;
        }
    }
}
