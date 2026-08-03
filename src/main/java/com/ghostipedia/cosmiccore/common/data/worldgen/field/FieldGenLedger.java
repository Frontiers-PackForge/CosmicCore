package com.ghostipedia.cosmiccore.common.data.worldgen.field;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The world's permanent memory of which bundle every materialized ore field is. Once a field's chunks have been
 * baked, its type is recorded here and can never change, no matter how the live assignment function evolves
 * (regional bag rebalances, new bundle types, weight changes). The dowsing rod, survey scanner, extraction drill,
 * and worldgen all read through {@link OreFieldPlacement}, which consults these records before the live function,
 * so nothing player-facing can ever disagree with ore already in the ground.
 * <p>
 * Records are captured from region-file headers alone (the 4KB offset tables, no chunk data) at server start
 * (migration of pre-ledger worlds, recorded with the legacy roll they were baked with), at every autosave of the
 * overworld, and at server stop. Assignment parameters can only change between sessions, so land recorded in the
 * session that baked it always matches its ore; a hard process kill can leave at most one autosave interval of
 * land to be recorded at the next boot, and if that boot also changed the bag parameters the fingerprint check
 * logs a warning. The snapshot is immutable during play, which makes reads safe from worldgen threads.
 * <p>
 * The ledger fails closed: saves are atomic with a rotating backup, an unreadable file falls back to the backup,
 * and if both are unreadable the session runs on the live function without recording or overwriting anything, so
 * recorded identities can be corrupted only by deliberate file deletion. A recorded bundle whose material is no
 * longer registered pins its field dormant (skipped entirely) rather than baking a contradictory live type under
 * the stale record.
 */
@EventBusSubscriber(modid = CosmicCore.MOD_ID)
public final class FieldGenLedger {

    private FieldGenLedger() {}

    private static final int FORMAT_VERSION = 1;
    private static final int FOOTPRINT_PAD = OreFieldPlacement.DEFAULT_FIELD_RADIUS + 60;
    private static final long AUTOSAVE_SCAN_INTERVAL_MS = 5 * 60 * 1000;
    private static final Pattern REGION_FILE = Pattern.compile("^r\\.(-?\\d+)\\.(-?\\d+)\\.mca$");

    private static final Map<ResourceLocation, Map<Long, String>> RAW_RECORDS = new HashMap<>();
    private static final Map<ResourceLocation, String> LOADED_FINGERPRINTS = new HashMap<>();
    private static volatile Map<ResourceLocation, Map<Long, Material>> snapshot = Map.of();
    private static volatile Map<ResourceLocation, Set<Long>> unresolvable = Map.of();

    private static Path ledgerFile;
    private static long worldSeed;
    private static boolean dirty;
    private static boolean recordingEnabled;
    private static long lastAutosaveScan;

    public static Material recorded(ResourceKey<Level> dimension, int cellX, int cellZ) {
        Map<Long, Material> dim = snapshot.get(dimension.location());
        return dim == null ? null : dim.get(pack(cellX, cellZ));
    }

    public static boolean isUnresolvable(ResourceKey<Level> dimension, int cellX, int cellZ) {
        Set<Long> dim = unresolvable.get(dimension.location());
        return dim != null && dim.contains(pack(cellX, cellZ));
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        MinecraftServer server = event.getServer();
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        ledgerFile = worldRoot.resolve("data").resolve("cosmiccore_fieldgen.nbt");
        worldSeed = server.getWorldData().worldGenOptions().seed();
        RAW_RECORDS.clear();
        LOADED_FINGERPRINTS.clear();
        dirty = false;
        recordingEnabled = true;
        lastAutosaveScan = System.currentTimeMillis();

        boolean hadLedger = Files.exists(ledgerFile) || Files.exists(backupFile());
        if (hadLedger && !load()) {
            CosmicCore.LOGGER.error("[FieldGen] field ledger and backup are both unreadable. Recording is " +
                    "DISABLED this session so the files are never overwritten; ore fields run on live assignment. " +
                    "Restore {} (or its .bak) from a backup, or delete both deliberately to rebuild.", ledgerFile);
            recordingEnabled = false;
            publish();
            return;
        }

        boolean legacyWorld = !hadLedger && anyRegionFilesExist(worldRoot);
        boolean clean = scanAndRecord(worldRoot, legacyWorld);
        if (legacyWorld && !clean) {
            CosmicCore.LOGGER.error("[FieldGen] legacy migration scan hit unreadable region files. Deferring " +
                    "ledger creation; migration will retry on the next boot.");
            RAW_RECORDS.clear();
            recordingEnabled = false;
            publish();
            return;
        }
        warnOnFingerprintDrift();
        if (dirty || !hadLedger) {
            if (!save() && !hadLedger) {
                throw new IllegalStateException("[FieldGen] could not write the field ledger sentinel for a new " +
                        "world; refusing to generate unrecordable ore fields. See the log for the IO error.");
            }
        }
        publish();
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (!recordingEnabled || ledgerFile == null) return;
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) return;
        long now = System.currentTimeMillis();
        if (now - lastAutosaveScan < AUTOSAVE_SCAN_INTERVAL_MS) return;
        lastAutosaveScan = now;
        scanAndRecord(level.getServer().getWorldPath(LevelResource.ROOT), false);
        if (dirty) {
            save();
            publish();
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        if (ledgerFile != null && recordingEnabled) {
            scanAndRecord(event.getServer().getWorldPath(LevelResource.ROOT), false);
            if (dirty) {
                save();
            }
        }
        RAW_RECORDS.clear();
        LOADED_FINGERPRINTS.clear();
        snapshot = Map.of();
        unresolvable = Map.of();
        ledgerFile = null;
        dirty = false;
        recordingEnabled = false;
    }

    private static boolean scanAndRecord(Path worldRoot, boolean legacy) {
        boolean clean = true;
        for (ResourceKey<Level> dimension : OreFieldPlacement.fieldDimensions()) {
            Path regionDir = DimensionType.getStorageFolder(dimension, worldRoot).resolve("region");
            if (!Files.isDirectory(regionDir)) continue;
            Map<Long, String> raw = RAW_RECORDS.computeIfAbsent(dimension.location(), k -> new HashMap<>());
            Set<Long> candidateCells = new HashSet<>();
            try (DirectoryStream<Path> files = Files.newDirectoryStream(regionDir, "r.*.mca")) {
                for (Path file : files) {
                    if (!collectCandidateCells(file, candidateCells)) {
                        clean = false;
                    }
                }
            } catch (IOException e) {
                CosmicCore.LOGGER.error("[FieldGen] failed to scan region files for {}", dimension.location(), e);
                clean = false;
            }
            for (long packed : candidateCells) {
                if (raw.containsKey(packed)) continue;
                int cellX = unpackX(packed);
                int cellZ = unpackZ(packed);
                long[] core = OreFieldPlacement.survivingCore(worldSeed, dimension, cellX, cellZ);
                if (core == null) continue;
                Material bundle = legacy ?
                        OreFieldPlacement.assignBundleLegacy(worldSeed, dimension, (int) core[0], (int) core[1]) :
                        OreFieldPlacement.assignBundleFresh(worldSeed, dimension, cellX, cellZ);
                if (bundle == null) continue;
                raw.put(packed, bundle.getResourceLocation().toString());
                dirty = true;
            }
        }
        return clean;
    }

    private static boolean collectCandidateCells(Path regionFile, Set<Long> out) {
        Matcher matcher = REGION_FILE.matcher(regionFile.getFileName().toString());
        if (!matcher.matches()) return true;
        int regionX = Integer.parseInt(matcher.group(1));
        int regionZ = Integer.parseInt(matcher.group(2));

        int[] header = new int[1024];
        try {
            if (Files.size(regionFile) < 4096) {
                return true;
            }
            try (InputStream in = Files.newInputStream(regionFile);
                    DataInputStream data = new DataInputStream(in)) {
                for (int i = 0; i < 1024; i++) {
                    header[i] = data.readInt();
                }
            }
        } catch (IOException e) {
            CosmicCore.LOGGER.error("[FieldGen] failed to read region header {}", regionFile, e);
            return false;
        }

        int cellSize = OreFieldPlacement.MIN_DISTANCE;
        for (int i = 0; i < 1024; i++) {
            if (header[i] == 0) continue;
            int blockX = (regionX * 32 + (i & 31)) * 16;
            int blockZ = (regionZ * 32 + (i >> 5)) * 16;
            int cxLo = Math.floorDiv(blockX - FOOTPRINT_PAD, cellSize) - 1;
            int cxHi = Math.floorDiv(blockX + 15 + FOOTPRINT_PAD, cellSize);
            int czLo = Math.floorDiv(blockZ - FOOTPRINT_PAD, cellSize) - 1;
            int czHi = Math.floorDiv(blockZ + 15 + FOOTPRINT_PAD, cellSize);
            for (int cx = cxLo; cx <= cxHi; cx++) {
                for (int cz = czLo; cz <= czHi; cz++) {
                    long cellMinX = (long) cx * cellSize - FOOTPRINT_PAD;
                    long cellMaxX = (long) cx * cellSize + cellSize + FOOTPRINT_PAD;
                    long cellMinZ = (long) cz * cellSize - FOOTPRINT_PAD;
                    long cellMaxZ = (long) cz * cellSize + cellSize + FOOTPRINT_PAD;
                    if (blockX + 16 > cellMinX && blockX < cellMaxX &&
                            blockZ + 16 > cellMinZ && blockZ < cellMaxZ) {
                        out.add(pack(cx, cz));
                    }
                }
            }
        }
        return true;
    }

    private static boolean anyRegionFilesExist(Path worldRoot) {
        for (ResourceKey<Level> dimension : OreFieldPlacement.fieldDimensions()) {
            Path regionDir = DimensionType.getStorageFolder(dimension, worldRoot).resolve("region");
            if (!Files.isDirectory(regionDir)) continue;
            try (DirectoryStream<Path> files = Files.newDirectoryStream(regionDir, "r.*.mca")) {
                if (files.iterator().hasNext()) return true;
            } catch (IOException ignored) {}
        }
        return false;
    }

    private static boolean load() {
        if (Files.exists(ledgerFile) && loadFrom(ledgerFile)) return true;
        Path backup = backupFile();
        if (Files.exists(backup) && loadFrom(backup)) {
            CosmicCore.LOGGER.warn("[FieldGen] main ledger unreadable, recovered from backup {}", backup);
            dirty = true;
            return true;
        }
        return false;
    }

    private static boolean loadFrom(Path file) {
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            int version = root.getInt("version");
            if (version != FORMAT_VERSION) {
                CosmicCore.LOGGER.error("[FieldGen] ledger {} has unsupported version {} (expected {})", file,
                        version, FORMAT_VERSION);
                return false;
            }
            Map<ResourceLocation, Map<Long, String>> parsed = new HashMap<>();
            Map<ResourceLocation, String> fingerprints = new HashMap<>();
            ListTag dims = root.getList("dims", Tag.TAG_COMPOUND);
            for (int i = 0; i < dims.size(); i++) {
                CompoundTag dimTag = dims.getCompound(i);
                ResourceLocation dim = ResourceLocation.tryParse(dimTag.getString("dim"));
                if (dim == null) continue;
                ListTag palette = dimTag.getList("palette", Tag.TAG_STRING);
                long[] cells = dimTag.getLongArray("cells");
                int[] types = dimTag.getIntArray("types");
                if (cells.length != types.length) {
                    CosmicCore.LOGGER.error("[FieldGen] ledger {} has misaligned arrays for {}", file, dim);
                    return false;
                }
                Map<Long, String> raw = parsed.computeIfAbsent(dim, k -> new HashMap<>());
                for (int j = 0; j < cells.length; j++) {
                    int typeIndex = types[j];
                    if (typeIndex < 0 || typeIndex >= palette.size()) continue;
                    raw.put(cells[j], palette.getString(typeIndex));
                }
                if (dimTag.contains("fingerprint")) {
                    fingerprints.put(dim, dimTag.getString("fingerprint"));
                }
            }
            RAW_RECORDS.clear();
            RAW_RECORDS.putAll(parsed);
            LOADED_FINGERPRINTS.clear();
            LOADED_FINGERPRINTS.putAll(fingerprints);
            return true;
        } catch (IOException e) {
            CosmicCore.LOGGER.error("[FieldGen] failed to load field ledger {}", file, e);
            return false;
        }
    }

    private static boolean save() {
        CompoundTag root = new CompoundTag();
        root.putInt("version", FORMAT_VERSION);
        ListTag dims = new ListTag();
        for (Map.Entry<ResourceLocation, Map<Long, String>> entry : RAW_RECORDS.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("dim", entry.getKey().toString());
            ListTag palette = new ListTag();
            Map<String, Integer> paletteIndex = new HashMap<>();
            long[] cells = new long[entry.getValue().size()];
            int[] types = new int[entry.getValue().size()];
            int i = 0;
            for (Map.Entry<Long, String> record : entry.getValue().entrySet()) {
                Integer index = paletteIndex.get(record.getValue());
                if (index == null) {
                    index = palette.size();
                    paletteIndex.put(record.getValue(), index);
                    palette.add(StringTag.valueOf(record.getValue()));
                }
                cells[i] = record.getKey();
                types[i] = index;
                i++;
            }
            dimTag.put("palette", palette);
            dimTag.putLongArray("cells", cells);
            dimTag.putIntArray("types", types);
            dims.add(dimTag);
        }
        for (ResourceKey<Level> dimension : OreFieldPlacement.fieldDimensions()) {
            boolean present = false;
            for (int i = 0; i < dims.size(); i++) {
                CompoundTag dimTag = dims.getCompound(i);
                if (dimTag.getString("dim").equals(dimension.location().toString())) {
                    dimTag.putString("fingerprint", bagFingerprint(dimension));
                    present = true;
                    break;
                }
            }
            if (!present) {
                CompoundTag dimTag = new CompoundTag();
                dimTag.putString("dim", dimension.location().toString());
                dimTag.putString("fingerprint", bagFingerprint(dimension));
                dims.add(dimTag);
            }
        }
        root.put("dims", dims);
        try {
            Files.createDirectories(ledgerFile.getParent());
            Path tmp = ledgerFile.resolveSibling(ledgerFile.getFileName() + ".tmp");
            NbtIo.writeCompressed(root, tmp);
            if (Files.exists(ledgerFile)) {
                Files.move(ledgerFile, backupFile(), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tmp, ledgerFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, ledgerFile, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
            return true;
        } catch (IOException e) {
            CosmicCore.LOGGER.error("[FieldGen] failed to save field ledger", e);
            return false;
        }
    }

    private static void warnOnFingerprintDrift() {
        for (ResourceKey<Level> dimension : OreFieldPlacement.fieldDimensions()) {
            String stored = LOADED_FINGERPRINTS.get(dimension.location());
            if (stored == null) continue;
            String current = bagFingerprint(dimension);
            String terrain = OreFieldTerrainResolver.algorithmFingerprint(dimension);
            if (!terrain.isEmpty() && !stored.startsWith("@" + terrain)) {
                CosmicCore.LOGGER.warn("[FieldGen] terrain-resolution parameters for {} changed. Existing " +
                        "Firmament field markers and newly resolved anchors may disagree until affected land is " +
                        "regenerated.", dimension.location());
            }
            if (dirty && !stored.equals(current)) {
                CosmicCore.LOGGER.warn("[FieldGen] bundle parameters for {} changed since the last clean " +
                        "shutdown and unrecorded generated land was found. Fields baked in the interrupted " +
                        "session may be recorded with the new parameters.", dimension.location());
            }
        }
    }

    private static String bagFingerprint(ResourceKey<Level> dimension) {
        StringJoiner joiner = new StringJoiner(",");
        String terrainFingerprint = OreFieldTerrainResolver.algorithmFingerprint(dimension);
        if (!terrainFingerprint.isEmpty()) {
            joiner.add("@" + terrainFingerprint);
        }
        for (Material bundle : OreFieldPlacement.bundles()) {
            OreFieldPlacement.FieldProfile profile = OreFieldPlacement.profileFor(bundle);
            if (profile == null || !profile.dimensions().contains(dimension) || profile.weight() <= 0) continue;
            joiner.add(bundle.getResourceLocation() + "*" + profile.weight());
        }
        return joiner.toString();
    }

    private static void publish() {
        Map<String, Material> byId = new HashMap<>();
        for (Material bundle : OreFieldPlacement.bundles()) {
            byId.put(bundle.getResourceLocation().toString(), bundle);
        }
        Map<ResourceLocation, Map<Long, Material>> resolved = new HashMap<>();
        Map<ResourceLocation, Set<Long>> pinned = new HashMap<>();
        for (Map.Entry<ResourceLocation, Map<Long, String>> entry : RAW_RECORDS.entrySet()) {
            Map<Long, Material> dim = new HashMap<>();
            for (Map.Entry<Long, String> record : entry.getValue().entrySet()) {
                Material bundle = byId.get(record.getValue());
                if (bundle == null) {
                    CosmicCore.LOGGER.warn("[FieldGen] recorded bundle {} no longer registered, its field is " +
                            "dormant until the material returns", record.getValue());
                    pinned.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(record.getKey());
                    continue;
                }
                dim.put(record.getKey(), bundle);
            }
            resolved.put(entry.getKey(), dim);
        }
        snapshot = Map.copyOf(resolved);
        unresolvable = Map.copyOf(pinned);
    }

    private static Path backupFile() {
        return ledgerFile.resolveSibling(ledgerFile.getFileName() + ".bak");
    }

    private static long pack(int cellX, int cellZ) {
        return (cellX & 0xFFFFFFFFL) | ((long) cellZ << 32);
    }

    private static int unpackX(long packed) {
        return (int) packed;
    }

    private static int unpackZ(long packed) {
        return (int) (packed >> 32);
    }
}
