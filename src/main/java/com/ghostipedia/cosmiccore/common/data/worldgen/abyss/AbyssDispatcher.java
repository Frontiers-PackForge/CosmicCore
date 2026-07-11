package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicBundleMaterials;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class AbyssDispatcher {

    private AbyssDispatcher() {}

    private static final Logger LOGGER = LoggerFactory.getLogger("CosmicAbyss");
    private static final int COVER_DEPTH = 2;
    private static final double MARBLE_SCALE = 0.16;
    private static final double MARBLE_THRESHOLD = 0.60;
    private static final double ORE_SCALE = 0.30;
    private static final double ORE_THRESHOLD = 0.80;
    private static final long[] ORE_SALTS = { 0x517CC1B7L, 0x2545F491L };
    private static final double[] SURFACE_FLORA_CHANCE = { 0.02, 0.055, 0.035, 0.012, 0.0 };
    private static final double[] BELLY_FLORA_CHANCE = { 0.0, 0.045, 0.045, 0.0, 0.0 };
    private static final double BERRY_CHANCE = 0.35;
    private static final long FLORA_SALT = 0x5DEECE66DL;
    private static final long SPECIES_SALT = 0x9E3779B9L;
    private static final long STACK_SALT = 0x2545F4914F6CDD1DL;

    private static final String[] LAYER_STONE = {
            "minecraft:deepslate",
            "create:scorchia",
            "malum:twisted_rock",
            "minecraft:blackstone",
            "minecraft:calcite"
    };
    private static final String[] LAYER_ACCENT = {
            "minecraft:smooth_basalt",
            "create:scoria",
            "malum:tainted_rock",
            "biomesoplenty:smooth_black_sandstone",
            "minecraft:calcite"
    };
    private static final String[] LAYER_SKIN = {
            "undergarden:shiverstone",
            "undergarden:sediment",
            "malum:blighted_earth",
            "minecraft:basalt",
            "minecraft:calcite"
    };

    private static BlockState[] stoneByLayer;
    private static BlockState[] accentByLayer;
    private static BlockState[] skinByLayer;
    private static BlockState[][] oresByLayer;
    private static BlockState[][] surfaceFloraByLayer;
    private static boolean[] bellyVineLayers;
    private static BlockState kelpState;
    private static BlockState vineBodyState;
    private static BlockState vineTipState;
    private static BlockState vineBerryState;

    private static void initPalette() {
        if (stoneByLayer != null) return;
        stoneByLayer = resolve(LAYER_STONE);
        accentByLayer = resolve(LAYER_ACCENT);
        skinByLayer = resolve(LAYER_SKIN);
        initOres();
        initFlora();
    }

    private static void initFlora() {
        BlockState kelp = CosmicBlocks.MURK_KELP.getDefaultState();
        BlockState seagrass = CosmicBlocks.MURK_SEAGRASS.getDefaultState();
        BlockState gloomFan = CosmicBlocks.GLOOM_FAN.getDefaultState();
        BlockState shimmer = CosmicBlocks.SHIMMER_TUFT.getDefaultState();
        BlockState ditchbulb = CosmicBlocks.DITCHBULB.getDefaultState();
        BlockState bloodFan = CosmicBlocks.BLOOD_FAN.getDefaultState();
        BlockState polyp = CosmicBlocks.PALE_POLYP.getDefaultState();
        BlockState clinging = CosmicBlocks.CLINGING_BLIGHT.getDefaultState();
        BlockState blightroot = CosmicBlocks.BLIGHTROOT.getDefaultState();
        BlockState blightedGrowth = CosmicBlocks.BLIGHTED_GROWTH.getDefaultState();
        BlockState strangeCrystal = CosmicBlocks.STRANGE_CRYSTAL.getDefaultState();
        BlockState largeCrystal = CosmicBlocks.LARGE_STRANGE_CRYSTAL.getDefaultState();

        kelpState = kelp;
        vineBodyState = CosmicBlocks.ABYSS_VINE.getDefaultState();
        vineTipState = CosmicBlocks.ABYSS_VINE_TIP.getDefaultState();
        vineBerryState = CosmicBlocks.DROOP_STRAND.getDefaultState();
        surfaceFloraByLayer = new BlockState[][] {
                { kelp, seagrass },
                { seagrass, seagrass, kelp, kelp, gloomFan, shimmer, clinging },
                { bloodFan, seagrass, ditchbulb, clinging, blightroot, blightedGrowth, strangeCrystal },
                { polyp, polyp, strangeCrystal, largeCrystal },
                {}
        };
        bellyVineLayers = new boolean[] { false, true, true, false, false };
    }

    private static double rand01(long seed, int x, int y, int z, long salt) {
        long v = seed ^ salt;
        v ^= x * 0x9E3779B97F4A7C15L;
        v ^= y * 0xC2B2AE3D27D4EB4FL;
        v ^= z * 0x165667B19E3779F9L;
        v = (v ^ (v >>> 30)) * 0xBF58476D1CE4E5B9L;
        v = (v ^ (v >>> 27)) * 0x94D049BB133111EBL;
        v ^= v >>> 31;
        return (v >>> 11) / (double) (1L << 53);
    }

    private static void trySurfaceFlora(ChunkAccess chunk, long seed, int wx, int y, int wz, int layer, int ceilY) {
        if (y > ceilY) return;
        BlockState[] table = surfaceFloraByLayer[layer];
        if (table.length == 0) return;
        if (rand01(seed, wx, y, wz, FLORA_SALT) >= SURFACE_FLORA_CHANCE[layer]) return;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(wx, y, wz);
        if (!chunk.getBlockState(pos).is(Blocks.WATER)) return;

        int pick = (int) (rand01(seed, wx, y, wz, SPECIES_SALT) * table.length);
        BlockState flora = table[Math.min(pick, table.length - 1)];
        int height = flora == kelpState ? 1 + (int) (rand01(seed, wx, y, wz, STACK_SALT) * 3) : 1;
        for (int dy = 0; dy < height; dy++) {
            pos.set(wx, y + dy, wz);
            if (y + dy > ceilY || !chunk.getBlockState(pos).is(Blocks.WATER)) break;
            chunk.setBlockState(pos, flora, false);
        }
    }

    private static void tryBellyFlora(ChunkAccess chunk, long seed, int wx, int y, int wz, int floorY) {
        int layer = AbyssRegions.layerBlended(seed, wx, y, wz);
        if (layer < 0 || layer >= BELLY_FLORA_CHANCE.length) return;
        if (!bellyVineLayers[layer]) return;
        if (rand01(seed, wx, y, wz, FLORA_SALT) >= BELLY_FLORA_CHANCE[layer]) return;

        int length = 1 + (int) (rand01(seed, wx, y, wz, STACK_SALT) * 3);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int placed = 0;
        for (int dy = 0; dy < length; dy++) {
            pos.set(wx, y - dy, wz);
            if (y - dy < floorY || !chunk.getBlockState(pos).is(Blocks.WATER)) break;
            chunk.setBlockState(pos, vineBodyState, false);
            placed++;
        }
        if (placed == 0) return;
        pos.set(wx, y - placed + 1, wz);
        boolean berry = rand01(seed, wx, y, wz, SPECIES_SALT) < BERRY_CHANCE;
        chunk.setBlockState(pos, berry ? vineBerryState : vineTipState, false);
    }

    private static void initOres() {
        TagPrefix[] hostPrefix = {
                TagPrefix.oreDeepslate,
                TagPrefix.oreBasalt,
                TagPrefix.oreDeepslate,
                TagPrefix.oreBlackstone,
                TagPrefix.oreMarble
        };
        Material[][] layerMats = {
                {},
                { CosmicBundleMaterials.Utherite },
                { CosmicBundleMaterials.Vanachrome, CosmicBundleMaterials.Shimmerbloom },
                { CosmicBundleMaterials.Agarlite },
                {}
        };
        oresByLayer = new BlockState[layerMats.length][];
        for (int layer = 0; layer < layerMats.length; layer++) {
            List<BlockState> states = new ArrayList<>();
            for (Material mat : layerMats[layer]) {
                Block oreBlock = ChemicalHelper.getBlock(hostPrefix[layer], mat);
                if (oreBlock == null || oreBlock.defaultBlockState().isAir()) {
                    LOGGER.warn("Abyss ore splatter: no {} ore block for {}", hostPrefix[layer].name(),
                            mat.getName());
                    continue;
                }
                states.add(oreBlock.defaultBlockState());
            }
            oresByLayer[layer] = states.toArray(new BlockState[0]);
        }
    }

    private static BlockState rollOre(long seed, int x, int y, int z, int layer) {
        BlockState[] ores = oresByLayer[layer];
        for (int i = 0; i < ores.length; i++) {
            double n = AbyssShape.noise3(seed + ORE_SALTS[i] + layer * 7919L,
                    x * ORE_SCALE, y * ORE_SCALE, z * ORE_SCALE);
            if (n > ORE_THRESHOLD) return ores[i];
        }
        return null;
    }

    private static BlockState[] resolve(String[] ids) {
        BlockState[] out = new BlockState[ids.length];
        for (int i = 0; i < ids.length; i++) {
            out[i] = block(ids[i]);
        }
        return out;
    }

    private static BlockState block(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl != null) {
            Block b = BuiltInRegistries.BLOCK.get(rl);
            if (b != null && b != Blocks.AIR) return b.defaultBlockState();
        }
        LOGGER.warn("Abyss greybox: block '{}' not found, falling back to deepslate", id);
        return Blocks.DEEPSLATE.defaultBlockState();
    }

    public static void stampChunk(long seed, ResourceKey<Level> dim, ChunkAccess chunk) {
        try {
            initPalette();
            stamp(seed, dim, chunk);
        } catch (Exception e) {
            LOGGER.error("Abyss dispatch failed for chunk {}", chunk.getPos(), e);
        }
    }

    private static void stamp(long seed, ResourceKey<Level> dim, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();

        List<AbyssPlacement.Member> near = AbyssPlacement.membersNear(seed, dim, minX + 8, minZ + 8);
        if (near.isEmpty()) return;

        List<AbyssPlacement.Member> land = new ArrayList<>();
        List<AbyssPlacement.Member> sparse = new ArrayList<>();
        for (AbyssPlacement.Member m : near) {
            double r = m.reach();
            if (m.x() + r < minX || m.x() - r > minX + 15) continue;
            if (m.z() + r < minZ || m.z() - r > minZ + 15) continue;
            if (m.sparse()) sparse.add(m);
            else land.add(m);
        }
        if (land.isEmpty() && sparse.isEmpty()) return;

        int floorY = chunk.getMinBuildHeight() + 1;
        int ceilY = chunk.getMaxBuildHeight() - 1;
        int top = LAYER_STONE.length - 1;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        List<AbyssPlacement.Member> colLand = new ArrayList<>();
        List<AbyssPlacement.Member> colSparse = new ArrayList<>();

        for (int lx = 0; lx < 16; lx++) {
            int wx = minX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int wz = minZ + lz;
                colLand.clear();
                colSparse.clear();
                double yLo = Double.POSITIVE_INFINITY;
                double yHi = Double.NEGATIVE_INFINITY;
                for (AbyssPlacement.Member m : land) {
                    double r = m.reach();
                    if (wx < m.x() - r || wx > m.x() + r || wz < m.z() - r || wz > m.z() + r) continue;
                    colLand.add(m);
                    double pad = m.yReach() + AbyssShape.ROUGHNESS * m.size();
                    yLo = Math.min(yLo, m.y() - pad);
                    yHi = Math.max(yHi, m.y() + pad);
                }
                for (AbyssPlacement.Member m : sparse) {
                    double r = m.reach();
                    if (wx < m.x() - r || wx > m.x() + r || wz < m.z() - r || wz > m.z() + r) continue;
                    colSparse.add(m);
                    double pad = m.yReach() + AbyssShape.ROUGHNESS * m.size();
                    yLo = Math.min(yLo, m.y() - pad);
                    yHi = Math.max(yHi, m.y() + pad);
                }
                if (colLand.isEmpty() && colSparse.isEmpty()) continue;

                int y0 = Math.max(floorY, (int) Math.floor(yLo));
                int y1 = Math.min(ceilY, (int) Math.ceil(yHi));
                int coverLeft = 0;
                boolean prevLand = false;

                for (int y = y1; y >= y0; y--) {
                    boolean landSolid = !colLand.isEmpty() && AbyssShape.density(seed, wx, y, wz, colLand) < 0.0;
                    boolean blobSolid = !landSolid && !colSparse.isEmpty() &&
                            AbyssShape.density(seed, wx, y, wz, colSparse) < 0.0;
                    if (!landSolid && !blobSolid) {
                        if (prevLand) {
                            tryBellyFlora(chunk, seed, wx, y, wz, floorY);
                        }
                        prevLand = false;
                        continue;
                    }
                    cursor.set(wx, y, wz);
                    BlockState existing = chunk.getBlockState(cursor);
                    if (!existing.isAir() && existing.getFluidState().isEmpty()) {
                        prevLand = false;
                        continue;
                    }
                    int layer = AbyssRegions.layerBlended(seed, wx, y, wz);
                    if (layer < 0) layer = 0;
                    if (layer > top) layer = top;
                    if (landSolid) {
                        if (!prevLand) {
                            coverLeft = COVER_DEPTH;
                            trySurfaceFlora(chunk, seed, wx, y + 1, wz, layer, ceilY);
                        }
                        BlockState put;
                        if (coverLeft > 0) {
                            put = skinByLayer[layer];
                            coverLeft--;
                        } else {
                            put = rollOre(seed, wx, y, wz, layer);
                            if (put == null) {
                                double n = AbyssShape.noise3(seed, wx * MARBLE_SCALE, y * MARBLE_SCALE,
                                        wz * MARBLE_SCALE);
                                put = n > MARBLE_THRESHOLD ? accentByLayer[layer] : stoneByLayer[layer];
                            }
                        }
                        chunk.setBlockState(cursor, put, false);
                        prevLand = true;
                    } else {
                        chunk.setBlockState(cursor, stoneByLayer[layer], false);
                        prevLand = false;
                    }
                }
            }
        }
    }
}
