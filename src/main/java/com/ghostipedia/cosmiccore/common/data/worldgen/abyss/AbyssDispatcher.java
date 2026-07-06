package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

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

    private static void initPalette() {
        if (stoneByLayer != null) return;
        stoneByLayer = resolve(LAYER_STONE);
        accentByLayer = resolve(LAYER_ACCENT);
        skinByLayer = resolve(LAYER_SKIN);
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
                        if (!prevLand) coverLeft = COVER_DEPTH;
                        BlockState put;
                        if (coverLeft > 0) {
                            put = skinByLayer[layer];
                            coverLeft--;
                        } else {
                            double n = AbyssShape.noise3(seed, wx * MARBLE_SCALE, y * MARBLE_SCALE, wz * MARBLE_SCALE);
                            put = n > MARBLE_THRESHOLD ? accentByLayer[layer] : stoneByLayer[layer];
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
