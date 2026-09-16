package com.ghostipedia.cosmiccore.common.network.packet;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.client.rate.ProductionStatisticsScreen;
import com.ghostipedia.cosmiccore.common.production.ProductionStatisticsData;
import com.ghostipedia.cosmiccore.common.production.ProductionStatisticsService;
import com.ghostipedia.cosmiccore.common.rate.RateCalculatorReportTransport;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ProductionStatisticsPackets {

    private ProductionStatisticsPackets() {}

    public record Request(CompoundTag query) implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(CosmicCore.id("production_statistics_request"));
        public static final StreamCodec<FriendlyByteBuf, Request> CODEC = StreamCodec.composite(
                RateCalculatorReportTransport.CODEC, Request::query, Request::new);

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> {
                if (!(context.player() instanceof ServerPlayer player)) return;
                int window = Math.max(-1,
                        Math.min(ProductionStatisticsData.WINDOWS.length - 1, query.getInt("window")));
                int page = Math.max(0, Math.min(100_000, query.getInt("page")));
                int pageSize = query.contains("pageSize", Tag.TAG_INT) ?
                        Math.max(1, Math.min(ProductionStatisticsData.MAX_PAGE_SIZE, query.getInt("pageSize"))) :
                        ProductionStatisticsData.MAX_PAGE_SIZE;
                String kind = bounded(query.getString("kind"), 32);
                String dimension = bounded(query.getString("dimension"), 512);
                String search = bounded(query.getString("search"), 256).toLowerCase(java.util.Locale.ROOT);
                String sortMode = ProductionStatisticsData.normalizeSortMode(bounded(query.getString("sort"), 32));
                boolean reverse = query.getBoolean("reverse");
                List<String> selected = new ArrayList<>();
                ListTag requested = query.getList("selected", StringTag.TAG_STRING);
                for (int i = 0; i < Math.min(8, requested.size()); i++) {
                    String key = bounded(requested.getString(i), 16_384);
                    if (!key.isEmpty()) selected.add(key);
                }
                var pool = ProductionStatisticsService.viewerPool(player);
                ProductionStatisticsData data = ProductionStatisticsData.get(player.getServer());
                Response.send(player,
                        data.query(pool, dimension, kind, search, window, page, selected, sortMode, reverse, pageSize),
                        data.dimensions(pool), window, page, kind, dimension, search, selected, false);
            });
        }

        private static String bounded(String value, int max) {
            return value.length() <= max ? value : "";
        }

        @Override
        public @NotNull Type<Request> type() {
            return TYPE;
        }
    }

    public record Response(CompoundTag report) implements CustomPacketPayload {

        public static final Type<Response> TYPE = new Type<>(CosmicCore.id("production_statistics_response"));
        public static final StreamCodec<FriendlyByteBuf, Response> CODEC = StreamCodec.composite(
                RateCalculatorReportTransport.CODEC, Response::report, Response::new);

        static void send(ServerPlayer player, ProductionStatisticsData.Query query, Iterable<String> dimensions,
                         int window, int page, String kind, String dimension, String search, List<String> selected,
                         boolean open) {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("open", open);
            tag.putInt("window", window);
            tag.putInt("page", page);
            tag.putString("kind", kind);
            tag.putString("dimension", dimension);
            tag.putLong("clock", query.clock());
            tag.putString("search", search);
            tag.putString("sort", query.sortMode());
            tag.putBoolean("reverse", query.reverse());
            tag.putLong("covered", query.coveredTicks());
            tag.putLong("lifetime", query.lifetimeTicks());
            tag.putLong("energyStarted", query.energyStarted());
            tag.putLong("energyCovered", query.energyCoveredTicks());
            tag.putBoolean("more", query.more());
            tag.putBoolean("partialInput", query.partialInput());
            tag.putBoolean("partialOutput", query.partialOutput());
            ListTag dims = new ListTag();
            dimensions.forEach(value -> dims.add(StringTag.valueOf(value)));
            tag.put("dimensions", dims);
            ListTag selection = new ListTag();
            selected.forEach(value -> selection.add(StringTag.valueOf(value)));
            tag.put("selected", selection);
            ListTag rows = new ListTag();
            for (var row : query.rows()) {
                CompoundTag entry = row.resource().toTag();
                entry.putString("key", row.resource().key());
                entry.putString("input", row.input());
                entry.putString("output", row.output());
                rows.add(entry);
            }
            tag.put("rows", rows);
            ListTag graph = new ListTag();
            for (var point : query.graph()) {
                CompoundTag entry = new CompoundTag();
                entry.putString("resource", point.resource());
                entry.putLong("start", point.start());
                entry.putLong("end", point.end());
                entry.putString("input", point.input());
                entry.putString("output", point.output());
                graph.add(entry);
            }
            tag.put("graph", graph);
            com.ghostipedia.cosmiccore.common.network.CCoreNetwork.sendToPlayer(player, new Response(tag));
        }

        public static void open(ServerPlayer player) {
            var pool = ProductionStatisticsService.viewerPool(player);
            ProductionStatisticsData data = ProductionStatisticsData.get(player.getServer());
            send(player, data.query(pool, "", "", -1, 0, List.of()), data.dimensions(pool), -1, 0, "", "", "",
                    List.of(), true);
        }

        public void execute(IPayloadContext context) {
            context.enqueueWork(() -> ProductionStatisticsScreen.receive(report));
        }

        @Override
        public @NotNull Type<Response> type() {
            return TYPE;
        }
    }
}
