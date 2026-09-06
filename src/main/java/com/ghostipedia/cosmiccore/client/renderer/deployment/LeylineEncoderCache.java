package com.ghostipedia.cosmiccore.client.renderer.deployment;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.*;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class LeylineEncoderCache {

    private static final int MAX_SCHEMAS = 8;
    private static final int MAX_BLOCKS = 65_536;
    private static final Map<Key, MultiblockSchemaInfo> SCHEMAS = new LinkedHashMap<>();
    private static List<MachineRow> machines;
    private static int cachedBlocks;
    private static int generation;

    private LeylineEncoderCache() {}

    public static synchronized void clear() {
        SCHEMAS.clear();
        machines = null;
        cachedBlocks = 0;
        generation++;
    }

    static synchronized Lease acquire(MultiblockMachineDefinition definition, UUID design) {
        var key = new Key(definition, design);
        var schema = SCHEMAS.remove(key);
        if (schema != null) cachedBlocks -= schema.getStructureBlocks().size();
        return new Lease(key, schema == null ? new MultiblockSchemaInfo() : schema, generation);
    }

    static synchronized List<MachineRow> machines() {
        if (machines == null) {
            var font = Minecraft.getInstance().font;
            machines = GTRegistries.MACHINES.stream().filter(MultiblockMachineDefinition.class::isInstance)
                    .map(MultiblockMachineDefinition.class::cast)
                    .sorted(Comparator.comparing(definition -> definition.getId().toString()))
                    .map(definition -> {
                        Component title = new ItemStack(definition.getItem()).getHoverName();
                        return new MachineRow(definition, title, normalize(title.getString()),
                                normalize(definition.getId().toString()),
                                Math.max(26, font.split(title, 115).size() * font.lineHeight + 8));
                    }).toList();
        }
        return machines;
    }

    static String normalize(String text) {
        return Objects.requireNonNullElse(ChatFormatting.stripFormatting(text), "").strip().toLowerCase(Locale.ROOT);
    }

    private static synchronized void release(Lease lease) {
        if (lease.released) return;
        lease.released = true;
        lease.schema.setRenderer(null);
        lease.schema.setMultiSchema(null);
        int blocks = lease.schema.getStructureBlocks().size();
        if (lease.generation != generation || lease.dirty || lease.schema.getMapSchema() == null ||
                blocks > MAX_BLOCKS)
            return;
        var previous = SCHEMAS.put(lease.key, lease.schema);
        if (previous != null) cachedBlocks -= previous.getStructureBlocks().size();
        cachedBlocks += blocks;
        var iterator = SCHEMAS.values().iterator();
        while (SCHEMAS.size() > MAX_SCHEMAS || cachedBlocks > MAX_BLOCKS) {
            cachedBlocks -= iterator.next().getStructureBlocks().size();
            iterator.remove();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ReloadEvents {

        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> clear());
        }
    }

    private record Key(MultiblockMachineDefinition definition, UUID design) {}

    record MachineRow(MultiblockMachineDefinition definition, Component title, String name, String id, int height) {

        boolean matches(String query) {
            return name.contains(query) || id.contains(query);
        }
    }

    static final class Lease {

        private Key key;
        private final MultiblockSchemaInfo schema;
        private final int generation;
        private boolean dirty;
        private boolean released;

        private Lease(Key key, MultiblockSchemaInfo schema, int generation) {
            this.key = key;
            this.schema = schema;
            this.generation = generation;
        }

        MultiblockSchemaInfo schema() {
            return schema;
        }

        void changed() {
            dirty = true;
        }

        void identify(UUID design) {
            key = new Key(key.definition(), design);
            dirty = false;
        }

        void release() {
            LeylineEncoderCache.release(this);
        }
    }
}
