package com.ghostipedia.cosmiccore.client.renderer;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.StarLadderResearchHubPatterns;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import java.util.*;

public class RingUpgradePreviewRenderer {

    private static final Map<BlockPos, PreviewData> ACTIVE_PREVIEWS = new HashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();

    // Cached delta maps: position relative to controller -> block to place
    private static Map<BlockPos, Block> DELTA_NOTHING_TO_T0;
    private static Map<BlockPos, Block> DELTA_T0_TO_T1;
    private static Map<BlockPos, Block> DELTA_T1_TO_T2;
    private static Map<BlockPos, Block> DELTA_T2_TO_T3;
    private static Map<BlockPos, Block> DELTA_T3_TO_T4;

    // Block mapping for unified character scheme
    private static Map<Character, Block> CHAR_TO_BLOCK;

    private record PreviewData(BlockPos controllerPos, Map<BlockPos, Block> deltaBlocks) {}

    static {
        initializeBlockMapping();
        computeDeltas();
    }

    private static void initializeBlockMapping() {
        CHAR_TO_BLOCK = new HashMap<>();
        CHAR_TO_BLOCK.put('A', CosmicBlocks.SUPERHEAVY_STEEL_CASING.get());
        CHAR_TO_BLOCK.put('B', CosmicBlocks.BOLTED_HEAVY_FRAME_CASING.get());
        CHAR_TO_BLOCK.put('C', CosmicBlocks.SOMARUST_CASING.get());
        CHAR_TO_BLOCK.put('D', CosmicBlocks.SOUL_MUTED_CASING.get());
        CHAR_TO_BLOCK.put('E', CosmicBlocks.BICHROMAL_NEVRAMITE_CASING.get());
        CHAR_TO_BLOCK.put('F', CosmicBlocks.OSCILLATING_GILDED_PTHANTERUM_CASING.get());
        CHAR_TO_BLOCK.put('G', CosmicBlocks.HIGHLY_FLEXIBLE_REINFORCED_TRINAVINE_CASING.get());
        CHAR_TO_BLOCK.put('H', CosmicBlocks.ROYAL_ICHORIUM_CASING.get());
        CHAR_TO_BLOCK.put('I', CosmicBlocks.MULTIPURPOSE_INTERSTELLAR_GRADE_CASING.get());
        CHAR_TO_BLOCK.put('J', CosmicBlocks.ULTRA_POWERED_CASING.get());
    }

    private static void computeDeltas() {
        // Parse each tier's pattern
        Map<BlockPos, Character> t0Blocks = parsePatternRelativeToController(StarLadderResearchHubPatterns.TIER_0,
                "T0");
        Map<BlockPos, Character> t1Blocks = parsePatternRelativeToController(StarLadderResearchHubPatterns.TIER_1,
                "T1");
        Map<BlockPos, Character> t2Blocks = parsePatternRelativeToController(StarLadderResearchHubPatterns.TIER_2,
                "T2");
        Map<BlockPos, Character> t3Blocks = parsePatternRelativeToController(StarLadderResearchHubPatterns.TIER_3,
                "T3");

        // Compute deltas between consecutive tiers
        // Delta from nothing (controller-only) to T0 is just all T0 blocks
        DELTA_NOTHING_TO_T0 = computeDelta(new HashMap<>(), t0Blocks);
        DELTA_T0_TO_T1 = computeDelta(t0Blocks, t1Blocks);
        DELTA_T1_TO_T2 = computeDelta(t1Blocks, t2Blocks);
        DELTA_T2_TO_T3 = computeDelta(t2Blocks, t3Blocks);
        // T4 not yet available
        DELTA_T3_TO_T4 = new HashMap<>();
    }

    private static Map<BlockPos, Character> parsePatternRelativeToController(String[] patternData, String tierName) {
        if (patternData == null || patternData.length == 0) return new HashMap<>();

        List<List<String>> aisles = new ArrayList<>();
        for (String aisleStr : patternData) {
            if (aisleStr.contains("|")) {
                aisles.add(Arrays.asList(aisleStr.split("\\|", -1)));
            }
        }
        if (aisles.isEmpty()) return new HashMap<>();

        // Find controller position (marked with '#')
        BlockPos controllerPos = null;
        outer:
        for (int z = 0; z < aisles.size(); z++) {
            List<String> aisle = aisles.get(z);
            for (int y = 0; y < aisle.size(); y++) {
                String layer = aisle.get(y);
                for (int x = 0; x < layer.length(); x++) {
                    if (layer.charAt(x) == '#') {
                        controllerPos = new BlockPos(x, y, z);
                        break outer;
                    }
                }
            }
        }
        if (controllerPos == null) return new HashMap<>();

        // Extract all blocks relative to controller
        Map<BlockPos, Character> blocks = new HashMap<>();
        int cx = controllerPos.getX(), cy = controllerPos.getY(), cz = controllerPos.getZ();

        for (int z = 0; z < aisles.size(); z++) {
            List<String> aisle = aisles.get(z);
            for (int y = 0; y < aisle.size(); y++) {
                String layer = aisle.get(y);
                for (int x = 0; x < layer.length(); x++) {
                    char ch = layer.charAt(x);
                    if (ch != ' ' && ch != '@' && ch != '#') {
                        blocks.put(new BlockPos(x - cx, y - cy, z - cz), ch);
                    }
                }
            }
        }
        return blocks;
    }

    private static Map<BlockPos, Block> computeDelta(Map<BlockPos, Character> prevTier,
                                                     Map<BlockPos, Character> nextTier) {
        Map<BlockPos, Block> delta = new HashMap<>();

        for (Map.Entry<BlockPos, Character> entry : nextTier.entrySet()) {
            BlockPos pos = entry.getKey();
            char ch = entry.getValue();

            // Block is new if it wasn't in previous tier or was a different type
            if (!prevTier.containsKey(pos) || !prevTier.get(pos).equals(ch)) {
                Block block = CHAR_TO_BLOCK.get(ch);
                if (block != null) {
                    delta.put(pos, block);
                }
            }
        }

        return delta;
    }

    public static void enablePreview(BlockPos controllerPos, Direction facing, int currentTier) {
        if (currentTier >= 3) return; // T3 is max currently (T4 not available)

        Map<BlockPos, Block> delta = getDeltaForTier(currentTier);
        if (delta == null || delta.isEmpty()) return;

        // Rotate delta positions based on controller facing
        Map<BlockPos, Block> rotatedDelta = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : delta.entrySet()) {
            BlockPos relPos = entry.getKey();
            BlockPos rotated = rotateOffset(relPos.getX(), relPos.getY(), relPos.getZ(), facing);
            BlockPos worldPos = controllerPos.offset(rotated);
            rotatedDelta.put(worldPos, entry.getValue());
        }

        ACTIVE_PREVIEWS.put(controllerPos, new PreviewData(controllerPos, rotatedDelta));
    }

    public static void disablePreview(BlockPos controllerPos) {
        ACTIVE_PREVIEWS.remove(controllerPos);
    }

    public static void updatePreview(BlockPos controllerPos, Direction facing, int newTier) {
        if (ACTIVE_PREVIEWS.containsKey(controllerPos)) {
            disablePreview(controllerPos);
            enablePreview(controllerPos, facing, newTier);
        }
    }

    public static void clearAllPreviews() {
        ACTIVE_PREVIEWS.clear();
    }

    private static Map<BlockPos, Block> getDeltaForTier(int currentTier) {
        return switch (currentTier) {
            case -1 -> DELTA_NOTHING_TO_T0;
            case 0 -> DELTA_T0_TO_T1;
            case 1 -> DELTA_T1_TO_T2;
            case 2 -> DELTA_T2_TO_T3;
            case 3 -> DELTA_T3_TO_T4;
            default -> null;
        };
    }

    private static BlockPos rotateOffset(int x, int y, int z, Direction facing) {
        // Maps pattern coords to world coords based on GTCEu's setActualRelativeOffset
        return switch (facing) {
            case NORTH -> new BlockPos(-x, y, -z);
            case SOUTH -> new BlockPos(x, y, z);
            case EAST -> new BlockPos(z, y, -x);
            case WEST -> new BlockPos(-z, y, x);
            default -> new BlockPos(x, y, z);
        };
    }

    public static void renderPreviews(PoseStack poseStack, Camera camera) {
        if (ACTIVE_PREVIEWS.isEmpty()) return;

        var mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 camPos = camera.getPosition();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = null;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        for (PreviewData preview : ACTIVE_PREVIEWS.values()) {
            if (!mc.level.isLoaded(preview.controllerPos)) continue;

            for (Map.Entry<BlockPos, Block> entry : preview.deltaBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                Block block = entry.getValue();

                // Only render ghost blocks where there's air
                if (!mc.level.isEmptyBlock(pos)) continue;

                BlockState state = block.defaultBlockState();
                renderGhostBlock(poseStack, dispatcher, buffer, tesselator, pos, state);
            }
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderGhostBlock(PoseStack poseStack, BlockRenderDispatcher dispatcher,
                                         BufferBuilder buffer, Tesselator tesselator,
                                         BlockPos pos, BlockState state) {
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.translate(-0.5, -0.5, -0.5);

        for (RenderType renderType : RenderType.chunkBufferLayers()) {
            if (!ItemBlockRenderTypes.getRenderLayers(state).contains(renderType)) continue;
            renderType.setupRenderState();
            buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            dispatcher.renderBatched(state, pos, Minecraft.getInstance().level, poseStack, buffer, false, RANDOM);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            renderType.clearRenderState();
        }
        poseStack.popPose();
    }

    public static int getDeltaBlockCount(int currentTier) {
        Map<BlockPos, Block> delta = getDeltaForTier(currentTier);
        return delta != null ? delta.size() : 0;
    }

    public static Map<Block, Integer> getDeltaBlockCounts(int currentTier) {
        Map<BlockPos, Block> delta = getDeltaForTier(currentTier);
        if (delta == null) return new HashMap<>();

        Map<Block, Integer> counts = new HashMap<>();
        for (Block block : delta.values()) {
            counts.merge(block, 1, Integer::sum);
        }
        return counts;
    }

    public static Block getRingBlock(int tier) {
        Map<BlockPos, Block> delta = getDeltaForTier(tier - 1);
        if (delta == null || delta.isEmpty()) return null;

        Map<Block, Integer> counts = new HashMap<>();
        for (Block block : delta.values()) {
            counts.merge(block, 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static int getRingBlockCount(int tier) {
        Map<BlockPos, Block> delta = getDeltaForTier(tier - 1);
        return delta != null ? delta.size() : 0;
    }

    public static Set<BlockPos> calculateRingPositions(BlockPos controllerPos, Direction facing, int targetTier) {
        Map<BlockPos, Block> delta = getDeltaForTier(targetTier - 1);
        if (delta == null || delta.isEmpty()) return new HashSet<>();

        Set<BlockPos> positions = new HashSet<>();
        for (BlockPos relPos : delta.keySet()) {
            BlockPos rotated = rotateOffset(relPos.getX(), relPos.getY(), relPos.getZ(), facing);
            positions.add(controllerPos.offset(rotated));
        }
        return positions;
    }

    public static Map<BlockPos, Block> calculateRingPositionsWithBlocks(BlockPos controllerPos, Direction facing,
                                                                        int targetTier) {
        Map<BlockPos, Block> delta = getDeltaForTier(targetTier - 1);
        if (delta == null || delta.isEmpty()) return new HashMap<>();

        Map<BlockPos, Block> positions = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : delta.entrySet()) {
            BlockPos relPos = entry.getKey();
            BlockPos rotated = rotateOffset(relPos.getX(), relPos.getY(), relPos.getZ(), facing);
            positions.put(controllerPos.offset(rotated), entry.getValue());
        }
        return positions;
    }
}
