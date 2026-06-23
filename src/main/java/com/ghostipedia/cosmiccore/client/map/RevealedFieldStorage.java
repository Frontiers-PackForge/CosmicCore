package com.ghostipedia.cosmiccore.client.map;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RevealedFieldStorage {

    private RevealedFieldStorage() {}

    private static String loadedKey;

    public static void ensureLoaded() {
        String key = worldId();
        if (key == null || key.equals(loadedKey)) return;
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
}
