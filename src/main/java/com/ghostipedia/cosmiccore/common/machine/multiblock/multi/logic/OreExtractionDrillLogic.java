package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OreExtractionDrillLogic extends RecipeLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            OreExtractionDrillLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);

    public static final int TICKS_PER_ORE = 100;
    public static final int CHUNKS_PER_SIDE = 9;
    public static final int BLOCKS_PER_CHUNK = 16;
    public static final int AREA_SIZE = CHUNKS_PER_SIDE * BLOCKS_PER_CHUNK;
    public static final int SCAN_BLOCKS_PER_TICK = 16384;

    public enum DrillPhase {
        IDLE,
        SCANNING,
        MINING,
        COMPLETE
    }

    @Getter
    @Persisted
    private DrillPhase phase = DrillPhase.IDLE;

    @Persisted
    private List<BlockPos> pendingOres = new ArrayList<>();

    @Persisted
    private int currentOreIndex = 0;

    @Persisted
    private int miningProgress = 0;

    @Persisted
    private int centerChunkX = Integer.MIN_VALUE;
    @Persisted
    private int centerChunkZ = Integer.MIN_VALUE;

    @Persisted
    private int scanX = 0;
    @Persisted
    private int scanY = 0;
    @Persisted
    private int scanZ = 0;
    @Persisted
    private boolean scanningRight = true;

    private Set<ChunkPos> ourLoadedChunks = new HashSet<>();
    private Set<ChunkPos> structureChunks = new HashSet<>();

    @Getter
    private Map<String, Integer> oreTypeCounts = new LinkedHashMap<>();

    @Persisted
    private long totalBlocksToScan = 0;
    @Persisted
    private long blocksScanned = 0;

    private int minX, maxX, minZ, maxZ, minY, startY;
    private boolean boundsInitialized = false;

    @Nullable
    private NotifiableAccountedInvWrapper cachedItemHandler = null;

    public OreExtractionDrillLogic(OreExtractionDrillMachine machine) {
        super(machine);
    }

    @Override
    public OreExtractionDrillMachine getMachine() {
        return (OreExtractionDrillMachine) super.getMachine();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void serverTick() {
        if (!getMachine().isFormed() || !getMachine().isWorkingEnabled()) {
            return;
        }

        if (!boundsInitialized) {
            initializeBounds();
        }

        switch (phase) {
            case IDLE -> startScanning();
            case SCANNING -> tickScanning();
            case MINING -> tickMining();
            case COMPLETE -> {} // Do nothing, wait for restart
        }
    }

    private void initializeBounds() {
        BlockPos machinePos = getMachine().getPos();
        int halfArea = AREA_SIZE / 2;

        minX = machinePos.getX() - halfArea;
        maxX = machinePos.getX() + halfArea - 1;
        minZ = machinePos.getZ() - halfArea;
        maxZ = machinePos.getZ() + halfArea - 1;
        minY = getMachine().getLevel().getMinBuildHeight();
        startY = machinePos.getY();

        // Calculate total blocks to scan
        long width = maxX - minX + 1;
        long depth = maxZ - minZ + 1;
        long height = startY - minY + 1;
        totalBlocksToScan = width * depth * height;

        boundsInitialized = true;
    }

    private void startScanning() {
        pendingOres.clear();
        oreTypeCounts.clear();
        currentOreIndex = 0;
        miningProgress = 0;
        blocksScanned = 0;

        scanX = minX;
        scanY = startY;
        scanZ = minZ;
        scanningRight = true;

        phase = DrillPhase.SCANNING;
        setStatus(Status.WORKING);
    }

    private void tickScanning() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        for (int i = 0; i < SCAN_BLOCKS_PER_TICK; i++) {
            if (scanY < minY) {
                finishScanning();
                return;
            }

            BlockPos pos = new BlockPos(scanX, scanY, scanZ);
            BlockState state = serverLevel.getBlockState(pos);

            if (isOre(state)) {
                pendingOres.add(pos);
                String oreName = getOreMaterialName(state);
                oreTypeCounts.merge(oreName, 1, Integer::sum);
            }

            blocksScanned++;
            advanceScanPosition();
        }
    }

    private void advanceScanPosition() {
        if (scanningRight) {
            scanX++;
            if (scanX > maxX) {
                scanX = maxX;
                scanZ++;
                scanningRight = false;
                if (scanZ > maxZ) {
                    scanZ = minZ;
                    scanY--;
                    scanningRight = true;
                    scanX = minX;
                }
            }
        } else {
            scanX--;
            if (scanX < minX) {
                scanX = minX;
                scanZ++;
                scanningRight = true;
                if (scanZ > maxZ) {
                    scanZ = minZ;
                    scanY--;
                    scanX = minX;
                }
            }
        }
    }

    private void finishScanning() {
        if (pendingOres.isEmpty()) {
            phase = DrillPhase.COMPLETE;
            setStatus(Status.IDLE);
        } else {
            phase = DrillPhase.MINING;
            currentOreIndex = 0;
            miningProgress = 0;
            updateChunkWindow(pendingOres.get(0));
        }
    }

    private void tickMining() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        if (currentOreIndex >= pendingOres.size()) {
            releaseAllMiningChunks();
            startScanning();
            return;
        }

        if (!getMachine().drainEnergy(false)) {
            setStatus(Status.WAITING);
            return;
        }
        getMachine().drainEnergy(true);
        setStatus(Status.WORKING);

        miningProgress++;

        if (miningProgress >= TICKS_PER_ORE) {
            miningProgress = 0;
            processCurrentOre(serverLevel);
            currentOreIndex++;
            if (currentOreIndex < pendingOres.size()) {
                updateChunkWindow(pendingOres.get(currentOreIndex));
            }
        }
    }

    private void processCurrentOre(ServerLevel serverLevel) {
        if (currentOreIndex >= pendingOres.size()) return;

        BlockPos orePos = pendingOres.get(currentOreIndex);
        BlockState state = serverLevel.getBlockState(orePos);

        if (!isOre(state)) {
            return;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(orePos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        drops.addAll(state.getDrops(builder));
        outputDrops(drops);

        float removalChance = getMachine().getRemovalChance();
        if (serverLevel.getRandom().nextFloat() < removalChance) {
            serverLevel.setBlock(orePos, Blocks.STONE.defaultBlockState(), 3);
            pendingOres.set(currentOreIndex, null);
        }
    }

    private void outputDrops(NonNullList<ItemStack> drops) {
        var handler = getCachedItemHandler();
        if (handler == null) return;

        GTTransferUtils.addItemsToItemHandler(handler, false, drops);
    }

    @Nullable
    private NotifiableAccountedInvWrapper getCachedItemHandler() {
        if (cachedItemHandler == null) {
            var caps = getMachine().getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP);
            if (caps != null && !caps.isEmpty()) {
                cachedItemHandler = new NotifiableAccountedInvWrapper(caps.stream()
                        .map(IItemHandlerModifiable.class::cast)
                        .toArray(IItemHandlerModifiable[]::new));
            }
        }
        return cachedItemHandler;
    }

    public void invalidateCache() {
        cachedItemHandler = null;
    }

    private void updateChunkWindow(BlockPos targetPos) {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        ChunkPos newCenter = new ChunkPos(targetPos);
        if (newCenter.x == centerChunkX && newCenter.z == centerChunkZ) return;

        Set<ChunkPos> newWindow = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                newWindow.add(new ChunkPos(newCenter.x + dx, newCenter.z + dz));
            }
        }

        Set<ChunkPos> toUnload = new HashSet<>(ourLoadedChunks);
        toUnload.removeAll(newWindow);
        toUnload.removeAll(structureChunks);

        for (ChunkPos chunk : toUnload) {
            unloadChunk(serverLevel, chunk);
        }

        for (ChunkPos chunk : newWindow) {
            if (!ourLoadedChunks.contains(chunk)) {
                loadChunk(serverLevel, chunk);
            }
        }

        centerChunkX = newCenter.x;
        centerChunkZ = newCenter.z;
    }

    private void loadChunk(ServerLevel level, ChunkPos chunk) {
        if (ourLoadedChunks.contains(chunk)) return;

        BlockPos ownerPos = getMachine().getPos();
        boolean success = ForgeChunkManager.forceChunk(
                level,
                CosmicCore.MOD_ID,
                ownerPos,
                chunk.x,
                chunk.z,
                true,
                true);

        if (success) {
            ourLoadedChunks.add(chunk);
        }
    }

    private void unloadChunk(ServerLevel level, ChunkPos chunk) {
        if (structureChunks.contains(chunk)) return;
        if (!ourLoadedChunks.remove(chunk)) return;

        BlockPos ownerPos = getMachine().getPos();
        ForgeChunkManager.forceChunk(
                level,
                CosmicCore.MOD_ID,
                ownerPos,
                chunk.x,
                chunk.z,
                false,
                true);
    }

    public void loadStructureChunks() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        structureChunks.clear();

        BlockPos machinePos = getMachine().getPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                ChunkPos chunk = new ChunkPos(machinePos.offset(dx, 0, dz));
                if (!structureChunks.contains(chunk)) {
                    structureChunks.add(chunk);
                    loadChunk(serverLevel, chunk);
                }
            }
        }
    }

    private void releaseAllMiningChunks() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        Set<ChunkPos> toUnload = new HashSet<>(ourLoadedChunks);
        toUnload.removeAll(structureChunks);

        for (ChunkPos chunk : toUnload) {
            unloadChunk(serverLevel, chunk);
        }

        centerChunkX = Integer.MIN_VALUE;
        centerChunkZ = Integer.MIN_VALUE;
    }

    public void releaseAllChunks() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        for (ChunkPos chunk : ourLoadedChunks) {
            BlockPos ownerPos = getMachine().getPos();
            ForgeChunkManager.forceChunk(
                    serverLevel,
                    CosmicCore.MOD_ID,
                    ownerPos,
                    chunk.x,
                    chunk.z,
                    false,
                    true);
        }

        ourLoadedChunks.clear();
        structureChunks.clear();
        centerChunkX = Integer.MIN_VALUE;
        centerChunkZ = Integer.MIN_VALUE;
    }

    public void restartDrill() {
        releaseAllMiningChunks();
        phase = DrillPhase.IDLE;
        pendingOres.clear();
        oreTypeCounts.clear();
        currentOreIndex = 0;
        miningProgress = 0;
        blocksScanned = 0;
        boundsInitialized = false;
    }

    private boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    private String getOreMaterialName(BlockState state) {
        try {
            ItemStack blockItem = new ItemStack(state.getBlock());
            if (!blockItem.isEmpty()) {
                var materialStack = ChemicalHelper.getMaterialStack(blockItem);
                if (materialStack != null && materialStack.material() != null) {
                    Material mat = materialStack.material();
                    String localizedName = mat.getLocalizedName().getString();
                    if (!localizedName.contains("material.gtceu.") && !localizedName.contains("null")) {
                        return localizedName + " Ore";
                    }
                }
            }
        } catch (Exception ignored) {}

        return parseOreNameFromDescriptionId(state.getBlock().getDescriptionId());
    }

    private static final String[] STONE_TYPES = {
            "stone", "deepslate", "granite", "diorite", "andesite", "tuff",
            "sand", "red_sand", "gravel", "basalt", "netherrack", "endstone",
            "blackstone", "marble", "sandstone", "red_sandstone", "smooth_basalt"
    };

    private String parseOreNameFromDescriptionId(String descId) {
        String key = descId.toLowerCase();
        String[] parts = key.split("\\.");
        if (parts.length < 2) return descId;

        String orePart = parts[parts.length - 1].replace("_ore", "");

        for (String stone : STONE_TYPES) {
            String prefix = stone + "_";
            if (orePart.startsWith(prefix)) {
                orePart = orePart.substring(prefix.length());
                break;
            }
        }

        return formatMaterialName(orePart) + " Ore";
    }

    private String formatMaterialName(String raw) {
        String[] words = raw.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) result.append(" ");
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        return result.toString();
    }

    public int getPendingOreCount() {
        return pendingOres.size();
    }

    public int getCurrentOreIndex() {
        return currentOreIndex;
    }

    public int getMiningProgress() {
        return miningProgress;
    }

    public float getMiningProgressPercent() {
        return (float) miningProgress / TICKS_PER_ORE;
    }

    public int getMiningProgressSeconds() {
        return miningProgress / 20;
    }

    public int getTotalMiningSeconds() {
        return TICKS_PER_ORE / 20;
    }

    public float getScanProgressPercent() {
        if (totalBlocksToScan <= 0) return 0f;
        return (float) blocksScanned / totalBlocksToScan * 100f;
    }

    @Override
    public void findAndHandleRecipe() {}
}
