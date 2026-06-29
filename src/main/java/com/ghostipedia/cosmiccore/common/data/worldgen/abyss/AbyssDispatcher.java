package com.ghostipedia.cosmiccore.common.data.worldgen.abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
    private static final String[] LAYER_COLOR = { "white", "light_blue", "cyan", "blue", "purple" };
    private static final String[] ZONE_COLOR = { "red", "yellow", "lime", "orange" };

    private static BlockState[] layerStone;
    private static BlockState[] layerBlob;
    private static BlockState[] zoneCover;

    private static void initPalette() {
        if (layerStone != null) return;
        BlockState[] stone = new BlockState[LAYER_COLOR.length];
        BlockState[] blob = new BlockState[LAYER_COLOR.length];
        for (int i = 0; i < LAYER_COLOR.length; i++) {
            stone[i] = block(LAYER_COLOR[i] + "_concrete");
            blob[i] = block(LAYER_COLOR[i] + "_terracotta");
        }
        BlockState[] cover = new BlockState[ZONE_COLOR.length];
        for (int i = 0; i < ZONE_COLOR.length; i++) {
            cover[i] = block(ZONE_COLOR[i] + "_wool");
        }
        layerStone = stone;
        layerBlob = blob;
        zoneCover = cover;
    }

    private static BlockState block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace(path)).defaultBlockState();
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

                int zone = AbyssRegions.zone(seed, wx, wz) % zoneCover.length;
                BlockState cover = zoneCover[zone];

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
                    int layer = AbyssRegions.layer(y) % layerStone.length;
                    if (landSolid) {
                        if (!prevLand) coverLeft = COVER_DEPTH;
                        BlockState put = coverLeft > 0 ? cover : layerStone[layer];
                        if (coverLeft > 0) coverLeft--;
                        chunk.setBlockState(cursor, put, false);
                        prevLand = true;
                    } else {
                        chunk.setBlockState(cursor, layerBlob[layer], false);
                        prevLand = false;
                    }
                }
            }
        }
    }
}
