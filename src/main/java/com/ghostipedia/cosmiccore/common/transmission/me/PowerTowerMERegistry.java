package com.ghostipedia.cosmiccore.common.transmission.me;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMEHatch;
import com.ghostipedia.cosmiccore.common.transmission.graph.PowerTowerSavedData;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import appeng.api.networking.pathing.ControllerState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class PowerTowerMERegistry {

    private static final Set<PowerTowerMERegistry> PENDING = new HashSet<>();
    private final PowerTowerSavedData data;
    private final Map<UUID, BlockPos> inputIdentities = new HashMap<>();
    private final Map<BlockPos, PowerTowerMEBinding> bindings = new HashMap<>();
    private final Map<UUID, NavigableSet<BlockPos>> destinations = new HashMap<>();
    private final Map<BlockPos, PowerTowerMEHatch> loaded = new HashMap<>();
    private final PowerTowerMEConnections connections = new PowerTowerMEConnections();

    public PowerTowerMERegistry(PowerTowerSavedData data, CompoundTag tag) {
        this.data = data;
        for (Tag value : tag.getList("Inputs", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) value;
            if (entry.hasUUID("Circuit")) inputIdentities.put(entry.getUUID("Circuit"),
                    BlockPos.of(entry.getLong("Position")));
        }
        for (Tag value : tag.getList("Outputs", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) value;
            var binding = PowerTowerMEBinding.load(entry);
            if (binding != null) putBinding(BlockPos.of(entry.getLong("Position")), binding);
        }
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        var inputs = new ListTag();
        inputIdentities.forEach((id, pos) -> {
            var entry = new CompoundTag();
            entry.putUUID("Circuit", id);
            entry.putLong("Position", pos.asLong());
            inputs.add(entry);
        });
        var outputs = new ListTag();
        bindings.forEach((pos, binding) -> {
            var entry = binding.save();
            entry.putLong("Position", pos.asLong());
            outputs.add(entry);
        });
        tag.put("Inputs", inputs);
        tag.put("Outputs", outputs);
        return tag;
    }

    public UUID claimInput(@Nullable UUID requested, BlockPos pos) {
        UUID id = requested;
        if (id == null || inputIdentities.containsKey(id) && !inputIdentities.get(id).equals(pos)) {
            do {
                id = UUID.randomUUID();
            } while (inputIdentities.containsKey(id));
        }
        if (!pos.equals(inputIdentities.put(id, pos.immutable()))) data.setDirty();
        return id;
    }

    public void register(PowerTowerMEHatch hatch) {
        var previous = loaded.get(hatch.getBlockPos());
        if (previous != null && previous != hatch) unregister(previous);
        loaded.put(hatch.getBlockPos(), hatch);
        updateBinding(hatch);
    }

    public void updateBinding(PowerTowerMEHatch hatch) {
        var binding = hatch.isInput() ? null : hatch.binding();
        if (!Objects.equals(bindings.get(hatch.getBlockPos()), binding)) {
            putBinding(hatch.getBlockPos(), binding);
            data.setDirty();
        }
        changed();
    }

    private void putBinding(BlockPos pos, @Nullable PowerTowerMEBinding binding) {
        var old = bindings.remove(pos);
        if (old != null) {
            var set = destinations.get(old.circuit());
            if (set != null && set.remove(pos) && set.isEmpty()) destinations.remove(old.circuit());
        }
        if (binding != null) {
            bindings.put(pos.immutable(), binding);
            destinations.computeIfAbsent(binding.circuit(), ignored -> new TreeSet<>()).add(pos.immutable());
        }
    }

    public void unregister(PowerTowerMEHatch hatch) {
        if (loaded.remove(hatch.getBlockPos(), hatch)) disconnect(hatch);
        changed();
    }

    public void remove(PowerTowerMEHatch hatch) {
        unregister(hatch);
        if (bindings.containsKey(hatch.getBlockPos())) {
            putBinding(hatch.getBlockPos(), null);
            data.setDirty();
        }
    }

    public void disconnect(PowerTowerMEHatch hatch) {
        connections.disconnect(hatch.getMainNode().getNode());
        changed();
    }

    public void changed() {
        PENDING.add(this);
    }

    public @Nullable PowerTowerMEHatch source(@Nullable PowerTowerMEBinding binding) {
        if (binding == null) return null;
        var source = loaded.get(binding.input());
        return source != null && source.isInput() && binding.equals(source.identity()) ? source : null;
    }

    public List<BlockPos> destinations(UUID circuit) {
        var set = destinations.get(circuit);
        return set == null ? List.of() : List.copyOf(set);
    }

    public @Nullable PowerTowerMEHatch loadedAt(BlockPos pos) {
        return loaded.get(pos);
    }

    public int usedChannels(PowerTowerMEHatch hatch) {
        var node = hatch.getMainNode().getNode();
        if (node == null || !node.hasGridBooted() || !node.isOnline()) return -1;
        if (hatch.isInput()) return node.getUsedChannels();
        var connection = connections.connection(hatch.getBlockPos());
        return connection == null ? -1 : connection.getUsedChannels();
    }

    public int capacity(PowerTowerMEHatch hatch) {
        var source = hatch.isInput() ? hatch : source(hatch.binding());
        if (source == null || !source.towerReady()) return -1;
        var node = source.getMainNode().getNode();
        if (node == null) return -1;
        int capacity = node.getMaxChannels();
        for (var connection : node.getConnections()) {
            if (connection.isInWorld()) capacity = Math.min(capacity,
                    connection.getOtherSide(node).getMaxChannels());
        }
        return capacity;
    }

    public String status(PowerTowerMEHatch hatch) {
        if (!hatch.towerReady()) return "unformed";
        if (!hatch.isInput()) {
            String unavailable = unavailable(hatch);
            if (unavailable != null) return unavailable;
            if (connections.connection(hatch.getBlockPos()) == null) {
                var source = source(hatch.binding());
                var endpoints = new PowerTowerMEConnections.Endpoints(source.getMainNode().getNode(),
                        hatch.getMainNode().getNode());
                return endpoints.existingConnection() == null ? "connecting" : "external_connection";
            }
        }
        var node = hatch.getMainNode().getNode();
        if (node == null) return "connecting";
        if (node.getGrid().getPathingService().getControllerState() == ControllerState.CONTROLLER_CONFLICT)
            return "controller_conflict";
        if (!node.hasGridBooted()) return "booting";
        if (!node.isOnline()) return "offline";
        int rating = capacity(hatch);
        var source = hatch.isInput() ? hatch : source(hatch.binding());
        if (rating >= 0 && source != null && usedChannels(source) > rating) return "overloaded";
        return "linked";
    }

    private @Nullable String unavailable(PowerTowerMEHatch output) {
        if (output.binding() == null) return "unbound";
        var input = source(output.binding());
        if (input == null) return "input_unavailable";
        if (!input.towerReady()) return "input_unformed";
        UUID first = input.towerId();
        UUID second = output.towerId();
        if (first == null || second == null || data.graph().node(first) == null ||
                !data.graph().reachableNodeIds(first).contains(second))
            return "route_broken";
        if (input.getMainNode().getNode() == null || output.getMainNode().getNode() == null) return "connecting";
        return null;
    }

    public void reconcile() {
        var desired = new HashMap<BlockPos, PowerTowerMEConnections.Endpoints>();
        for (var output : loaded.values()) {
            if (!output.isInput() && output.towerReady() && unavailable(output) == null) {
                var input = source(output.binding());
                desired.put(output.getBlockPos(), new PowerTowerMEConnections.Endpoints(
                        input.getMainNode().getNode(), output.getMainNode().getNode()));
            }
        }
        connections.reconcile(desired);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var pending = List.copyOf(PENDING);
        PENDING.clear();
        pending.forEach(PowerTowerMERegistry::reconcile);
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
