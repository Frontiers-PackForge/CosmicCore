package com.ghostipedia.cosmiccore.common.transmission.me;

import net.minecraft.core.BlockPos;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public final class PowerTowerMEConnections {

    private final Map<BlockPos, Bridge> bridges = new HashMap<>();
    private final BiFunction<IGridNode, IGridNode, IGridConnection> connect;

    public PowerTowerMEConnections() {
        this(GridHelper::createConnection);
    }

    PowerTowerMEConnections(BiFunction<IGridNode, IGridNode, IGridConnection> connect) {
        this.connect = connect;
    }

    public void reconcile(Map<BlockPos, Endpoints> desired) {
        removeIf(entry -> !entry.getValue().endpoints.equals(desired.get(entry.getKey())) ||
                !entry.getValue().isLive());
        desired.forEach((pos, endpoints) -> {
            if (bridges.containsKey(pos) || endpoints.existingConnection() != null) return;
            bridges.put(pos, new Bridge(endpoints, connect.apply(endpoints.input, endpoints.output)));
        });
    }

    public void disconnect(@Nullable IGridNode node) {
        if (node != null) removeIf(entry -> entry.getValue().endpoints.contains(node));
    }

    public @Nullable IGridConnection connection(BlockPos output) {
        var bridge = bridges.get(output);
        return bridge == null ? null : bridge.connection;
    }

    private void removeIf(Predicate<Map.Entry<BlockPos, Bridge>> predicate) {
        var removed = new ArrayList<Bridge>();
        bridges.entrySet().removeIf(entry -> {
            if (!predicate.test(entry)) return false;
            removed.add(entry.getValue());
            return true;
        });
        for (var bridge : removed) {
            if (bridge.isLive()) bridge.connection.destroy();
        }
    }

    public record Endpoints(IGridNode input, IGridNode output) {

        public Endpoints {
            if (input == null || output == null || input == output)
                throw new IllegalArgumentException("A tower bridge requires two distinct live nodes");
        }

        boolean contains(IGridNode node) {
            return input == node || output == node;
        }

        public @Nullable IGridConnection existingConnection() {
            for (var connection : input.getConnections()) {
                if (connection.getOtherSide(input) == output) return connection;
            }
            return null;
        }
    }

    private record Bridge(Endpoints endpoints, IGridConnection connection) {

        boolean isLive() {
            return endpoints.input.getConnections().contains(connection) &&
                    endpoints.output.getConnections().contains(connection);
        }
    }
}
