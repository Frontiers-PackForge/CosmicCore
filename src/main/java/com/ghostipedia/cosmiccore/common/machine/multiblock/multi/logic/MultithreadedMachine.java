package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IMultithreadedMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;

import net.minecraft.world.item.DyeColor;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A multiblock machine that can run multiple independent recipes simultaneously.
 * Each "thread" is assigned a color-coded set of input buses/hatches.
 * The maximum number of threads is determined by the energy hatch amperage.
 * <p>
 * Design:
 * - 4A energy hatch = 4 max threads
 * - 16A energy hatch = 16 max threads
 * - Each thread needs a uniquely colored input bus/hatch pair
 * - All threads share output buses/hatches
 * - Energy is split evenly among active threads
 */
public class MultithreadedMachine extends WorkableElectricMultiblockMachine implements IMultithreadedMachine {

    /**
     * Maximum possible threads (limited by largest energy hatch amperage)
     */
    public static final int MAX_THREADS = 16;

    /**
     * Map of thread color -> RecipeLogic for that thread
     */
    private final Int2ObjectMap<MultithreadedRecipeLogic> threadLogics = new Int2ObjectLinkedOpenHashMap<>();

    private final List<MultithreadedRecipeLogic> threadLogicPool = new ArrayList<>(MAX_THREADS);

    /**
     * Map of thread color -> input handler list for that thread
     */
    private final Int2ObjectMap<List<RecipeHandlerList>> threadInputHandlers = new Int2ObjectLinkedOpenHashMap<>();

    /**
     * Shared output handlers for all threads
     */
    private List<RecipeHandlerList> sharedOutputHandlers = new ArrayList<>();

    /**
     * Maximum number of threads allowed by the energy hatch
     */
    @SaveField
    @SyncToClient
    private int maxThreads = 0;

    /**
     * Currently active thread count
     */
    @SaveField
    @SyncToClient
    private int activeThreadCount = 0;

    /**
     * Total amperage available from energy hatch(es)
     */
    private int totalAmperage = 0;

    @Nullable
    private TickableSubscription threadTickSubscription;

    /**
     * Rotates the per-tick iteration start so the same threads aren't always last in line for the shared energy pool.
     */
    private int tickRotation = 0;

    public MultithreadedMachine(BlockEntityCreationInfo holder) {
        super(holder, new RecipeLogic() {

            @Override
            public void serverTick() {}
        });
        for (int index = 0; index < MAX_THREADS; index++) {
            MultithreadedRecipeLogic logic = new MultithreadedRecipeLogic(index, -1);
            attachPersistentTrait("cosmiccore_thread_" + index, logic);
            threadLogicPool.add(logic);
        }
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);

        // Clear previous state
        threadLogics.clear();
        threadInputHandlers.clear();
        sharedOutputHandlers.clear();
        maxThreads = 0;
        totalAmperage = 0;

        // Detect energy hatch amperage to determine max threads
        detectEnergyHatchAmperage();

        // Partition input handlers by color
        partitionHandlersByColor();

        // Collect shared output handlers
        collectOutputHandlers();

        // Create thread logics for each color group
        createThreadLogics();

        // Start the thread tick subscription
        updateThreadSubscription();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);

        // Deactivate all threads
        for (MultithreadedRecipeLogic logic : threadLogics.values()) {
            logic.deactivateThread();
        }

        threadLogics.clear();
        threadInputHandlers.clear();
        sharedOutputHandlers.clear();
        maxThreads = 0;
        activeThreadCount = 0;
        totalAmperage = 0;

        if (threadTickSubscription != null) {
            threadTickSubscription.unsubscribe();
            threadTickSubscription = null;
        }
    }

    /**
     * Detect the amperage of energy hatches to determine max thread count.
     */
    private void detectEnergyHatchAmperage() {
        // 8.0.0: getMatchContext()/the pattern ioMap was removed. Energy hatches are always inputs, so
        // sum their amperage directly (the old ioMap defaulted to IO.IN for these).
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof EnergyHatchPartMachine energyHatch) {
                totalAmperage += energyHatch.getAmperage();
            }
        }

        // Max threads = total amperage, capped at MAX_THREADS
        maxThreads = Math.min(totalAmperage, MAX_THREADS);
    }

    /**
     * Partition input handlers by their paint color.
     * Reads color from the part directly because RecipeHandlerList caches its color at first
     * access and never updates after a repaint.
     */
    private void partitionHandlersByColor() {
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MaintenanceHatchPartMachine) continue;
            int color = part.getPaintingColor();
            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                if (handlerList.getHandlerIO() != IO.IN && handlerList.getHandlerIO() != IO.BOTH) continue;
                if (!hasRecipeCapability(handlerList)) continue;
                threadInputHandlers.computeIfAbsent(color, k -> new ArrayList<>()).add(handlerList);
            }
        }
    }

    /**
     * Collect output handlers that will be shared by all threads.
     */
    private void collectOutputHandlers() {
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MaintenanceHatchPartMachine) continue;
            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                if (handlerList.getHandlerIO() != IO.OUT && handlerList.getHandlerIO() != IO.BOTH) continue;
                if (!hasRecipeCapability(handlerList)) continue;
                sharedOutputHandlers.add(handlerList);
            }
        }
    }

    /**
     * True if the handler list carries any recipe capability other than EU. Energy is wired in
     * separately via {@link #addEnergyHandlersToThread} so it must be excluded here, but every
     * other capability — items, fluids, souls, embers, heat, sterile, future ones — is fair game
     * for thread partitioning and shared outputs.
     */
    private static boolean hasRecipeCapability(RecipeHandlerList handlerList) {
        for (RecipeCapability<?> cap : handlerList.getHandlerMap().keySet()) {
            if (cap != EURecipeCapability.CAP) return true;
        }
        return false;
    }

    /**
     * Create a MultithreadedRecipeLogic for each color group, up to maxThreads.
     */
    private void createThreadLogics() {
        // Calculate EU/t budget per thread
        // Each thread gets 1A of voltage from the total amperage pool
        // With 16A UV hatch and 16 threads, each gets 1A UV = 524,288 EU/t
        // IMPORTANT: EnergyContainerList.getInputVoltage() returns TOTAL EU/t (voltage*amperage compacted)
        // We need to use getHighestInputVoltage() which returns the actual per-amp voltage
        long euPerThread = energyContainer != null ? energyContainer.getHighestInputVoltage() : 0;

        for (Int2ObjectMap.Entry<List<RecipeHandlerList>> entry : threadInputHandlers.int2ObjectEntrySet()) {
            if (threadLogics.size() >= maxThreads) break;

            int color = entry.getIntKey();
            MultithreadedRecipeLogic logic = acquireThreadLogic(color);
            if (logic == null) break;
            logic.bindThreadColor(color);
            logic.setMaxEUtPerThread(euPerThread);
            applyThreadHandlers(logic, entry.getValue());
            logic.activateThread();
            threadLogics.put(color, logic);
        }

        for (MultithreadedRecipeLogic logic : threadLogicPool) {
            if (logic.isThreadActive() && !threadLogics.containsValue(logic)) {
                logic.deactivateThread();
            }
        }

        activeThreadCount = threadLogics.size();
    }

    @Nullable
    private MultithreadedRecipeLogic acquireThreadLogic(int color) {
        for (MultithreadedRecipeLogic logic : threadLogicPool) {
            if (logic.getThreadColor() == color && !threadLogics.containsValue(logic)) {
                return logic;
            }
        }
        for (MultithreadedRecipeLogic logic : threadLogicPool) {
            if (!logic.isThreadActive() && !threadLogics.containsValue(logic)) {
                return logic;
            }
        }
        return null;
    }

    /**
     * Build and assign the per-thread proxy + flat capability maps for the given input handlers.
     * Output handlers and energy handlers are shared across threads.
     */
    private void applyThreadHandlers(MultithreadedRecipeLogic logic, List<RecipeHandlerList> inputHandlers) {
        Map<IO, List<RecipeHandlerList>> threadProxy = new EnumMap<>(IO.class);
        Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadFlat = new EnumMap<>(IO.class);

        threadProxy.put(IO.IN, new ArrayList<>(inputHandlers));
        threadProxy.put(IO.OUT, new ArrayList<>(sharedOutputHandlers));

        for (Map.Entry<IO, List<RecipeHandlerList>> proxyEntry : threadProxy.entrySet()) {
            IO io = proxyEntry.getKey();
            Map<RecipeCapability<?>, List<IRecipeHandler<?>>> capMap = new HashMap<>();
            for (RecipeHandlerList handlerList : proxyEntry.getValue()) {
                for (var capEntry : handlerList.getHandlerMap().entrySet()) {
                    capMap.computeIfAbsent(capEntry.getKey(), k -> new ArrayList<>()).addAll(capEntry.getValue());
                }
            }
            threadFlat.put(io, capMap);
        }

        addEnergyHandlersToThread(threadProxy, threadFlat);

        logic.setThreadCapabilitiesProxy(threadProxy);
        logic.setThreadCapabilitiesFlat(threadFlat);
    }

    /**
     * Add energy handlers to a thread's capability maps so recipes can consume EU.
     * Must add to both proxy and flat maps for full compatibility.
     */
    private void addEnergyHandlersToThread(
                                           Map<IO, List<RecipeHandlerList>> threadProxy,
                                           Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadFlat) {
        // Get energy handlers from the machine's global capabilities (proxy)
        var machineProxy = getCapabilitiesProxy();
        if (machineProxy != null && machineProxy.containsKey(IO.IN)) {
            for (RecipeHandlerList handlerList : machineProxy.get(IO.IN)) {
                if (handlerList.hasCapability(EURecipeCapability.CAP)) {
                    threadProxy.computeIfAbsent(IO.IN, k -> new ArrayList<>()).add(handlerList);
                }
            }
        }

        // Also add to flat map
        var machineFlat = getCapabilitiesFlat();
        if (machineFlat != null && machineFlat.containsKey(IO.IN)) {
            var inCaps = machineFlat.get(IO.IN);
            if (inCaps != null && inCaps.containsKey(EURecipeCapability.CAP)) {
                var energyHandlers = inCaps.get(EURecipeCapability.CAP);
                if (energyHandlers != null && !energyHandlers.isEmpty()) {
                    threadFlat.computeIfAbsent(IO.IN, k -> new HashMap<>())
                            .put(EURecipeCapability.CAP, new ArrayList<>(energyHandlers));
                }
            }
        }
    }

    /**
     * Re-run input partitioning after a part's paint color changes. Preserves in-progress
     * recipes on threads whose color still exists, only colors that disappeared from the
     * structure get their threads torn down. New colors that appear get fresh threads
     * (subject to the maxThreads cap).
     * This might be a genuinely hacky solution, I'm not sure lmoa :icant:
     */
    public void refreshThreadPartitioning() {
        if (!isFormed()) return;
        if (getLevel() == null || getLevel().isClientSide) return;

        Int2ObjectMap<List<RecipeHandlerList>> newPartition = new Int2ObjectLinkedOpenHashMap<>();
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof MaintenanceHatchPartMachine) continue;
            int color = part.getPaintingColor();
            for (RecipeHandlerList handlerList : part.getRecipeHandlers()) {
                if (handlerList.getHandlerIO() != IO.IN && handlerList.getHandlerIO() != IO.BOTH) continue;
                if (!hasRecipeCapability(handlerList)) continue;
                newPartition.computeIfAbsent(color, k -> new ArrayList<>()).add(handlerList);
            }
        }

        IntList staleColors = new IntArrayList();
        for (int color : threadLogics.keySet()) {
            if (!newPartition.containsKey(color)) staleColors.add(color);
        }
        for (int color : staleColors) {
            MultithreadedRecipeLogic stale = threadLogics.remove(color);
            if (stale != null) stale.deactivateThread();
            threadInputHandlers.remove(color);
        }

        long euPerThread = energyContainer != null ? energyContainer.getHighestInputVoltage() : 0;
        for (Int2ObjectMap.Entry<List<RecipeHandlerList>> entry : newPartition.int2ObjectEntrySet()) {
            int color = entry.getIntKey();
            List<RecipeHandlerList> inputHandlers = entry.getValue();
            threadInputHandlers.put(color, inputHandlers);

            MultithreadedRecipeLogic logic = threadLogics.get(color);
            if (logic == null) {
                if (threadLogics.size() >= maxThreads) continue;
                logic = acquireThreadLogic(color);
                if (logic == null) continue;
                logic.bindThreadColor(color);
                logic.activateThread();
                threadLogics.put(color, logic);
            }
            logic.setMaxEUtPerThread(euPerThread);
            applyThreadHandlers(logic, inputHandlers);
        }

        activeThreadCount = threadLogics.size();
        updateThreadSubscription();
    }

    /**
     * Update the tick subscription for thread processing.
     */
    private void updateThreadSubscription() {
        if (isFormed() && !threadLogics.isEmpty()) {
            threadTickSubscription = subscribeServerTick(threadTickSubscription, this::tickThreads);
        } else if (threadTickSubscription != null) {
            threadTickSubscription.unsubscribe();
            threadTickSubscription = null;
        }
    }

    /**
     * Called every server tick to process all thread logics. If this fails to be called I and everyone else should
     * panic
     */
    private void tickThreads() {
        if (!isFormed() || !isWorkingEnabled()) return;

        int runningThreads = 0;
        for (MultithreadedRecipeLogic logic : threadLogics.values()) {
            if (logic.isThreadActive()) {
                logic.serverTick();
                if (logic.isWorking()) {
                    runningThreads++;
                }
            }
        }

        activeThreadCount = runningThreads;
    }

    /**
     * Calculate energy available per thread.
     * Energy is split evenly among all active threads, should be evenly but for all i knwo this is wrong
     */
    private long getEnergyPerThread() {
        if (energyContainer == null || activeThreadCount == 0) return 0;
        return energyContainer.getInputVoltage() * totalAmperage / Math.max(1, getRunningThreadCount());
    }

    public Int2ObjectMap<MultithreadedRecipeLogic> getThreadLogics() {
        return threadLogics;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    public int getTotalAmperage() {
        return totalAmperage;
    }

    /**
     * Get the number of threads currently running recipes.
     */
    public int getRunningThreadCount() {
        int count = 0;
        for (MultithreadedRecipeLogic logic : threadLogics.values()) {
            if (logic.isWorking()) count++;
        }
        return count;
    }

    /**
     * Get the input handlers for a specific thread color.
     */
    @Nullable
    public List<RecipeHandlerList> getThreadInputHandlers(int color) {
        return threadInputHandlers.get(color);
    }

    /**
     * Get the shared output handlers.
     */
    public List<RecipeHandlerList> getSharedOutputHandlers() {
        return sharedOutputHandlers;
    }

    /**
     * Get a color name for display purposes.
     */
    public static String getColorName(int color) {
        if (color == -1) return "Unpainted";
        for (DyeColor dye : DyeColor.values()) {
            if (dye.getFireworkColor() == color || dye.getTextColor() == color) {
                return dye.getName();
            }
        }
        return "Color #" + Integer.toHexString(color);
    }

    // TODO(8.0.0 MUI2): addDisplayText (LDLib multiblock status readout) was removed in GTCEu 8.0.0
    // (stock GTCEu machines like HPCAMachine comment out the same). Rebuild the thread-status display on
    // MUI2 when ported; the thread/amperage data is all preserved on this machine.

    // === IMultithreadedMachine interface implementation ===

    @Override
    public Int2ObjectMap<MultithreadedRecipeLogic> getThreadLogicsMap() {
        return threadLogics;
    }

    @Override
    public int getMaxThreadCount() {
        return maxThreads;
    }

    @Override
    public int getCurrentThreadCount() {
        return threadLogics.size();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        // Called by individual thread logics
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        // Called by individual thread logics
        return super.onWorking();
    }

    @Override
    public void afterWorking() {
        // Called by individual thread logics
        super.afterWorking();
    }
}
