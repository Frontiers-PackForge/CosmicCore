package com.ghostipedia.cosmiccore.client.map;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RevealedFieldStorage {

    private RevealedFieldStorage() {}

    private static String loadedKey;
    private static final List<PendingReveal> pending = new ArrayList<>();

    public static void receive(ResourceKey<Level> dimension, List<RevealedField> fields) {
        pending.add(new PendingReveal(dimension, List.copyOf(fields)));
        ensureLoaded();
    }

    public static void ensureLoaded() {
        String key = worldId();
        if (key == null) return;
        if (!key.equals(loadedKey)) {
            loadedKey = key;
            Path file = file(key);
            try {
                if (Files.exists(file)) {
                    RevealedFields.INSTANCE.fromNbt(NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()));
                } else {
                    RevealedFields.INSTANCE.clearAll();
                }
            } catch (Exception e) {
                CosmicCore.LOGGER.error("[FieldMap] failed to load revealed fields", e);
            }
        }
        if (!pending.isEmpty()) {
            for (PendingReveal reveal : pending) {
                for (RevealedField field : reveal.fields()) {
                    RevealedFields.INSTANCE.put(reveal.dimension(), field);
                }
            }
            pending.clear();
            save();
        }
    }

    public static void save() {
        String key = worldId();
        if (key == null) return;
        Path file = file(key);
        try {
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(RevealedFields.INSTANCE.toNbt(), file);
        } catch (Exception e) {
            CosmicCore.LOGGER.error("[FieldMap] failed to save revealed fields", e);
        }
    }

    public static void reset() {
        loadedKey = null;
        pending.clear();
    }

    private static Path file(String key) {
        return FMLPaths.GAMEDIR.get().resolve("cosmiccore").resolve("field_reveal_cache").resolve(key + ".nbt");
    }

    private static String worldId() {
        Minecraft mc = Minecraft.getInstance();
        File worldFolder = ClientCacheManager.getWorldFolder();
        if (mc.player == null || worldFolder == null) return null;
        return mc.player.getUUID() + "/" + worldFolder.getName();
    }

    private record PendingReveal(ResourceKey<Level> dimension, List<RevealedField> fields) {}
}
