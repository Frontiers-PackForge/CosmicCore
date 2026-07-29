package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OreExtractionDrillLogic extends RecipeLogic {

    public static final int TICKS_PER_ORE = 100;
    public static final int DRILLING_FLUID_PER_BLOCK = 100;
    public static final int DRILLING_FLUID_CYCLE_TICKS = 50;
    public static final int ADVANCED_DRILLING_FLUID_CYCLE_TICKS = 20;
    public static final int EXTREME_DRILLING_FLUID_CYCLE_TICKS = 10;
    public static final int LEGACY_CHUNKS_PER_SIDE = 9;
    public static final int BLOCKS_PER_CHUNK = 16;
    public static final int SCAN_BLOCKS_PER_TICK = 16384;

    public enum DrillPhase {
        IDLE,
        SCANNING,
        MINING,
        COMPLETE
    }

    @SaveField
    private DrillPhase phase = DrillPhase.IDLE;

    @SaveField
    private List<BlockPos> pendingOres = new ArrayList<>();

    @SaveField
    private int currentOreIndex = 0;

    @SaveField
    private int miningProgress = 0;

    @SaveField
    private int currentCycleTicks = TICKS_PER_ORE;

    private int centerChunkX = Integer.MIN_VALUE;
    private int centerChunkZ = Integer.MIN_VALUE;

    @SaveField
    private int scanX = 0;
    @SaveField
    private int scanY = 0;
    @SaveField
    private int scanZ = 0;
    @SaveField
    private boolean scanningRight = true;

    @SaveField
    private int scanChunksPerSide = 0;

    private Set<ChunkPos> ourLoadedChunks = new HashSet<>();
    private Set<ChunkPos> structureChunks = new HashSet<>();

    @SaveField
    private List<String> ledgerKeys = new ArrayList<>();
    @SaveField
    private List<String> ledgerTranslationKeys = new ArrayList<>();
    @SaveField
    private List<String> ledgerItemIds = new ArrayList<>();
    @SaveField
    private List<Integer> ledgerCounts = new ArrayList<>();

    private List<OreLedgerEntry> publishedLedger = List.of();
    private boolean ledgerSnapshotInitialized = false;
    private boolean ledgerDirty = false;
    private int ledgerPublishTicks = 0;

    @SaveField
    private long totalBlocksToScan = 0;
    @SaveField
    private long blocksScanned = 0;

    private int minX, maxX, minZ, maxZ, minY, startY;
    private boolean boundsInitialized = false;

    @Nullable
    private NotifiableAccountedInvWrapper cachedItemHandler = null;

    @Nullable
    private FluidHandlerList cachedFluidHandler = null;

    public OreExtractionDrillLogic() {
        super();
    }

    @Override
    public OreExtractionDrillMachine getMachine() {
        return (OreExtractionDrillMachine) super.getMachine();
    }

    @Override
    public void serverTick() {
        if (!getMachine().isFormed() || !getMachine().isWorkingEnabled()) {
            return;
        }

        if (!boundsInitialized) {
            initializeBounds();
        }
        if (!ledgerSnapshotInitialized) {
            publishLedger();
        }

        switch (phase) {
            case IDLE -> startScanning();
            case SCANNING -> tickScanning();
            case MINING -> tickMining();
            case COMPLETE -> {} // Do nothing, wait for restart
        }
    }

    private void initializeBounds() {
        BlockPos machinePos = getMachine().getBlockPos();
        if (scanChunksPerSide <= 0) {
            scanChunksPerSide = phase == DrillPhase.IDLE ?
                    getMachine().getChunkDiameter() :
                    LEGACY_CHUNKS_PER_SIDE;
        }
        int areaSize = scanChunksPerSide * BLOCKS_PER_CHUNK;
        int halfArea = areaSize / 2;

        minX = machinePos.getX() - halfArea;
        maxX = minX + areaSize - 1;
        minZ = machinePos.getZ() - halfArea;
        maxZ = minZ + areaSize - 1;
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
        int configuredChunksPerSide = getMachine().getChunkDiameter();
        if (scanChunksPerSide != configuredChunksPerSide) {
            scanChunksPerSide = configuredChunksPerSide;
            boundsInitialized = false;
        }
        if (!boundsInitialized) {
            initializeBounds();
        }

        pendingOres.clear();
        clearLedger();
        currentOreIndex = 0;
        miningProgress = 0;
        currentCycleTicks = TICKS_PER_ORE;
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
                recordOre(state);
            }

            blocksScanned++;
            advanceScanPosition();
        }
        publishLedgerIfDue();
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
        publishLedger();
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

        if (miningProgress >= currentCycleTicks) {
            finishCurrentOre(serverLevel);
            return;
        }

        if (!getMachine().drainEnergy(false)) {
            setStatus(Status.WAITING);
            return;
        }
        if (miningProgress == 0) {
            currentCycleTicks = selectCycleTicks();
        }
        getMachine().drainEnergy(true);
        setStatus(Status.WORKING);

        miningProgress++;

        if (miningProgress >= currentCycleTicks) {
            finishCurrentOre(serverLevel);
        }
    }

    private void finishCurrentOre(ServerLevel serverLevel) {
        if (!processCurrentOre(serverLevel)) {
            setStatus(Status.WAITING);
            return;
        }

        miningProgress = 0;
        currentOreIndex++;
        if (currentOreIndex < pendingOres.size()) {
            updateChunkWindow(pendingOres.get(currentOreIndex));
        }
    }

    private boolean processCurrentOre(ServerLevel serverLevel) {
        if (currentOreIndex >= pendingOres.size()) return true;

        BlockPos orePos = pendingOres.get(currentOreIndex);
        BlockState state = serverLevel.getBlockState(orePos);

        if (!isOre(state)) {
            return true;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(orePos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        drops.addAll(state.getDrops(builder));
        if (!outputDrops(drops)) {
            return false;
        }

        serverLevel.setBlock(orePos, Blocks.STONE.defaultBlockState(), 3);
        return true;
    }

    private boolean outputDrops(NonNullList<ItemStack> drops) {
        var handler = getCachedItemHandler();
        if (handler == null || !GTTransferUtils.addItemsToItemHandler(handler, true, drops)) {
            return false;
        }

        return GTTransferUtils.addItemsToItemHandler(handler, false, drops);
    }

    @Nullable
    private NotifiableAccountedInvWrapper getCachedItemHandler() {
        if (cachedItemHandler == null) {
            var caps = getMachine().getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP);
            if (caps != null && !caps.isEmpty()) {
                cachedItemHandler = new NotifiableAccountedInvWrapper(caps.stream()
                        .filter(IItemHandlerModifiable.class::isInstance)
                        .map(IItemHandlerModifiable.class::cast)
                        .toArray(IItemHandlerModifiable[]::new));
            }
        }
        return cachedItemHandler;
    }

    public void invalidateCache() {
        cachedItemHandler = null;
        cachedFluidHandler = null;
    }

    private int selectCycleTicks() {
        int tierIndex = getMachine().getTierIndex();
        if (tierIndex >= 2 && consumeAccelerationFluid(CosmicMaterials.ExtremeDrillingFluid)) {
            return EXTREME_DRILLING_FLUID_CYCLE_TICKS;
        }
        if (tierIndex >= 1 && consumeAccelerationFluid(CosmicMaterials.AdvancedDrillingFluid)) {
            return ADVANCED_DRILLING_FLUID_CYCLE_TICKS;
        }
        if (consumeAccelerationFluid(com.gregtechceu.gtceu.common.data.GTMaterials.DrillingFluid)) {
            return DRILLING_FLUID_CYCLE_TICKS;
        }
        return TICKS_PER_ORE;
    }

    private boolean consumeAccelerationFluid(Material material) {
        var handler = getCachedFluidHandler();
        if (handler == null) return false;

        FluidStack requested = material.getFluid(DRILLING_FLUID_PER_BLOCK);
        FluidStack simulated = GTTransferUtils.drainFluidAccountNotifiableList(
                handler, requested, IFluidHandler.FluidAction.SIMULATE);
        if (simulated.getAmount() != DRILLING_FLUID_PER_BLOCK) return false;

        FluidStack drained = GTTransferUtils.drainFluidAccountNotifiableList(
                handler, requested, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount() == DRILLING_FLUID_PER_BLOCK;
    }

    @Nullable
    private FluidHandlerList getCachedFluidHandler() {
        if (cachedFluidHandler == null) {
            var caps = getMachine().getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP);
            if (caps != null && !caps.isEmpty()) {
                List<IFluidHandler> handlers = caps.stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .toList();
                if (!handlers.isEmpty()) {
                    cachedFluidHandler = new FluidHandlerList(handlers);
                }
            }
        }
        return cachedFluidHandler;
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

        level.setChunkForced(chunk.x, chunk.z, true);
        ourLoadedChunks.add(chunk);
    }

    private void unloadChunk(ServerLevel level, ChunkPos chunk) {
        if (structureChunks.contains(chunk)) return;
        if (!ourLoadedChunks.remove(chunk)) return;

        level.setChunkForced(chunk.x, chunk.z, false);
    }

    public void loadStructureChunks() {
        if (!(getMachine().getLevel() instanceof ServerLevel serverLevel)) return;

        structureChunks.clear();

        BlockPos machinePos = getMachine().getBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                ChunkPos chunk = new ChunkPos(machinePos.offset(dx, 0, dz));
                if (!structureChunks.contains(chunk)) {
                    structureChunks.add(chunk);
                    loadChunk(serverLevel, chunk);
                }
            }
        }
        if (phase == DrillPhase.MINING && currentOreIndex < pendingOres.size()) {
            updateChunkWindow(pendingOres.get(currentOreIndex));
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
            serverLevel.setChunkForced(chunk.x, chunk.z, false);
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
        clearLedger();
        currentOreIndex = 0;
        miningProgress = 0;
        currentCycleTicks = TICKS_PER_ORE;
        blocksScanned = 0;
        scanChunksPerSide = 0;
        boundsInitialized = false;
    }

    private boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    private OreIdentity getOreIdentity(BlockState state) {
        Item item = state.getBlock().asItem();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        ResourceLocation itemId = item == Items.AIR ?
                BuiltInRegistries.ITEM.getKey(Items.BARRIER) :
                BuiltInRegistries.ITEM.getKey(item);

        try {
            ItemStack blockItem = new ItemStack(item);
            if (!blockItem.isEmpty()) {
                var materialStack = ChemicalHelper.getMaterialStack(blockItem);
                if (materialStack != null && materialStack.material() != null) {
                    Material mat = materialStack.material();
                    ResourceLocation materialId = mat.getResourceLocation();
                    return new OreIdentity("material:" + materialId, mat.getUnlocalizedName(), itemId);
                }
            }
        } catch (Exception ignored) {}

        return new OreIdentity("block:" + blockId, state.getBlock().getDescriptionId(), itemId);
    }

    private void recordOre(BlockState state) {
        OreIdentity identity = getOreIdentity(state);
        int index = ledgerKeys.indexOf(identity.key());
        if (index >= 0) {
            ledgerCounts.set(index, ledgerCounts.get(index) + 1);
        } else {
            ledgerKeys.add(identity.key());
            ledgerTranslationKeys.add(identity.translationKey());
            ledgerItemIds.add(identity.itemId().toString());
            ledgerCounts.add(1);
        }
        ledgerDirty = true;
    }

    private void clearLedger() {
        ledgerKeys.clear();
        ledgerTranslationKeys.clear();
        ledgerItemIds.clear();
        ledgerCounts.clear();
        publishedLedger = List.of();
        ledgerSnapshotInitialized = true;
        ledgerDirty = false;
        ledgerPublishTicks = 0;
    }

    private void publishLedgerIfDue() {
        if (!ledgerDirty) return;
        ledgerPublishTicks++;
        if (ledgerPublishTicks >= 20) {
            publishLedger();
        }
    }

    private void publishLedger() {
        int size = Math.min(Math.min(ledgerKeys.size(), ledgerTranslationKeys.size()),
                Math.min(ledgerItemIds.size(), ledgerCounts.size()));
        List<OreLedgerEntry> entries = new ArrayList<>(size);
        ResourceLocation fallbackItem = BuiltInRegistries.ITEM.getKey(Items.BARRIER);
        for (int i = 0; i < size; i++) {
            ResourceLocation itemId = ResourceLocation.tryParse(ledgerItemIds.get(i));
            entries.add(new OreLedgerEntry(
                    ledgerTranslationKeys.get(i),
                    itemId == null ? fallbackItem : itemId,
                    ledgerCounts.get(i)));
        }
        entries.sort(Comparator.comparingInt(OreLedgerEntry::count)
                .reversed()
                .thenComparing(OreLedgerEntry::translationKey));
        publishedLedger = List.copyOf(entries);
        ledgerSnapshotInitialized = true;
        ledgerDirty = false;
        ledgerPublishTicks = 0;
    }

    public DrillPhase getPhase() {
        return phase;
    }

    public Map<String, Integer> getOreTypeCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (OreLedgerEntry entry : getOreLedgerEntries()) {
            counts.put(Component.translatable(entry.translationKey()).getString(), entry.count());
        }
        return counts;
    }

    public List<OreLedgerEntry> getOreLedgerEntries() {
        if (!ledgerSnapshotInitialized) {
            publishLedger();
        }
        return publishedLedger;
    }

    public int getPendingOreCount() {
        return pendingOres.size();
    }

    public int getExcavatedOreCount() {
        return Math.min(currentOreIndex, pendingOres.size());
    }

    public int getRemainingOreCount() {
        return Math.max(0, pendingOres.size() - currentOreIndex);
    }

    public int getSurveyChunksPerSide() {
        return scanChunksPerSide > 0 ? scanChunksPerSide : getMachine().getChunkDiameter();
    }

    public long getEstimatedSecondsRemaining() {
        if (phase == DrillPhase.COMPLETE) return 0;
        if (phase != DrillPhase.MINING) return -1;
        long ticks = (long) getRemainingOreCount() * getCurrentCycleTicks() - miningProgress;
        return Math.max(0, (ticks + 19) / 20);
    }

    public int getCurrentOreIndex() {
        return currentOreIndex;
    }

    public int getMiningProgress() {
        return miningProgress;
    }

    public int getCurrentCycleTicks() {
        return Math.max(1, currentCycleTicks);
    }

    public float getMiningProgressPercent() {
        return (float) miningProgress / getCurrentCycleTicks();
    }

    public int getMiningProgressSeconds() {
        return miningProgress / 20;
    }

    public int getTotalMiningSeconds() {
        return getCurrentCycleTicks() / 20;
    }

    public float getScanProgressPercent() {
        if (totalBlocksToScan <= 0) return 0f;
        return (float) blocksScanned / totalBlocksToScan * 100f;
    }

    public double getOperationProgress() {
        return switch (phase) {
            case IDLE -> 0.0;
            case SCANNING -> Mth.clamp(getScanProgressPercent() / 100.0, 0.0, 1.0);
            case MINING -> pendingOres.isEmpty() ? 0.0 :
                    Mth.clamp((double) currentOreIndex / pendingOres.size(), 0.0, 1.0);
            case COMPLETE -> 1.0;
        };
    }

    public record OreLedgerEntry(String translationKey, ResourceLocation itemId, int count) {}

    private record OreIdentity(String key, String translationKey, ResourceLocation itemId) {}

    @Override
    public void findAndHandleRecipe() {}
}
