package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IMultithreadedMachine;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
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

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MultithreadedMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    /**
     * Maximum possible threads (limited by largest energy hatch amperage)
     */
    public static final int MAX_THREADS = 16;

    /**
     * Map of thread color -> RecipeLogic for that thread
     */
    @Getter
    private final Int2ObjectMap<MultithreadedRecipeLogic> threadLogics = new Int2ObjectLinkedOpenHashMap<>();

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
    @Persisted
    @DescSynced
    @Getter
    private int maxThreads = 0;

    /**
     * Currently active thread count
     */
    @Persisted
    @DescSynced
    @Getter
    private int activeThreadCount = 0;

    /**
     * Total amperage available from energy hatch(es)
     */
    @Getter
    private int totalAmperage = 0;

    @Nullable
    private TickableSubscription threadTickSubscription;

    public MultithreadedMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        // We don't use the default recipe logic - we manage multiple thread logics instead
        // Return a dummy that does nothing, actual work is done by thread logics
        return new RecipeLogic(this) {

            @Override
            public void serverTick() {
                // Do nothing - threading is handled separately
            }
        };
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

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
    public void onStructureInvalid() {
        super.onStructureInvalid();

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
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext()
                .getOrCreate("ioMap", Long2ObjectMaps::emptyMap);

        for (IMultiPart part : getParts()) {
            if (part instanceof EnergyHatchPartMachine energyHatch) {
                IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.IN);
                if (io == IO.IN || io == IO.BOTH) {
                    totalAmperage += energyHatch.getAmperage();
                }
            }
        }

        // Max threads = total amperage, capped at MAX_THREADS
        maxThreads = Math.min(totalAmperage, MAX_THREADS);
    }

    /**
     * Partition input handlers by their paint color.
     * Each unique color becomes a potential thread.
     */
    private void partitionHandlersByColor() {
        for (IMultiPart part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            for (RecipeHandlerList handlerList : handlerLists) {
                if (handlerList.getHandlerIO() == IO.IN || handlerList.getHandlerIO() == IO.BOTH) {
                    // Check if this handler has item or fluid capability (not just energy)
                    boolean hasItemOrFluid = handlerList.hasCapability(ItemRecipeCapability.CAP) ||
                            handlerList.hasCapability(FluidRecipeCapability.CAP);

                    if (hasItemOrFluid) {
                        int color = handlerList.getColor();
                        threadInputHandlers.computeIfAbsent(color, k -> new ArrayList<>()).add(handlerList);
                    }
                }
            }
        }
    }

    /**
     * Collect output handlers that will be shared by all threads.
     */
    private void collectOutputHandlers() {
        for (IMultiPart part : getParts()) {
            var handlerLists = part.getRecipeHandlers();
            for (RecipeHandlerList handlerList : handlerLists) {
                if (handlerList.getHandlerIO() == IO.OUT || handlerList.getHandlerIO() == IO.BOTH) {
                    // Check if this handler has item or fluid capability
                    boolean hasItemOrFluid = handlerList.hasCapability(ItemRecipeCapability.CAP) ||
                            handlerList.hasCapability(FluidRecipeCapability.CAP);

                    if (hasItemOrFluid) {
                        sharedOutputHandlers.add(handlerList);
                    }
                }
            }
        }
    }

    /**
     * Create a MultithreadedRecipeLogic for each color group, up to maxThreads.
     */
    private void createThreadLogics() {
        int threadIndex = 0;

        // Calculate EU/t budget per thread
        // Each thread gets 1A of voltage from the total amperage pool
        // With 16A UV hatch and 16 threads, each gets 1A UV = 524,288 EU/t
        // IMPORTANT: EnergyContainerList.getInputVoltage() returns TOTAL EU/t (voltage*amperage compacted)
        // We need to use getHighestInputVoltage() which returns the actual per-amp voltage
        long euPerThread = energyContainer != null ? energyContainer.getHighestInputVoltage() : 0;

        for (Int2ObjectMap.Entry<List<RecipeHandlerList>> entry : threadInputHandlers.int2ObjectEntrySet()) {
            if (threadIndex >= maxThreads) break;

            int color = entry.getIntKey();
            List<RecipeHandlerList> inputHandlers = entry.getValue();

            MultithreadedRecipeLogic logic = new MultithreadedRecipeLogic(this, threadIndex, color);

            // Set the EU/t budget for this thread
            logic.setMaxEUtPerThread(euPerThread);

            // Build capability maps for this thread
            Map<IO, List<RecipeHandlerList>> threadProxy = new EnumMap<>(IO.class);
            Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadFlat = new EnumMap<>(IO.class);

            // Add input handlers (thread-specific, color-coded)
            threadProxy.put(IO.IN, new ArrayList<>(inputHandlers));

            // Add output handlers (shared)
            threadProxy.put(IO.OUT, new ArrayList<>(sharedOutputHandlers));

            // Build flattened map from proxy
            for (Map.Entry<IO, List<RecipeHandlerList>> proxyEntry : threadProxy.entrySet()) {
                IO io = proxyEntry.getKey();
                Map<RecipeCapability<?>, List<IRecipeHandler<?>>> capMap = new HashMap<>();

                for (RecipeHandlerList handlerList : proxyEntry.getValue()) {
                    for (var capEntry : handlerList.getHandlerMap().entrySet()) {
                        RecipeCapability<?> cap = capEntry.getKey();
                        List<IRecipeHandler<?>> handlers = capEntry.getValue();
                        capMap.computeIfAbsent(cap, k -> new ArrayList<>()).addAll(handlers);
                    }
                }

                threadFlat.put(io, capMap);
            }

            // Also add energy handlers from machine for recipe EU consumption
            addEnergyHandlersToThread(threadProxy, threadFlat);

            logic.setThreadCapabilitiesProxy(threadProxy);
            logic.setThreadCapabilitiesFlat(threadFlat);
            logic.activateThread();

            threadLogics.put(color, logic);
            threadIndex++;
        }

        activeThreadCount = threadLogics.size();
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
     * Called every server tick to process all thread logics.
     */
    private void tickThreads() {
        if (!isFormed() || !isWorkingEnabled()) return;

        // Calculate energy per thread
        long availableEnergy = getEnergyPerThread();

        // Tick each active thread
        int runningThreads = 0;
        for (MultithreadedRecipeLogic logic : threadLogics.values()) {
            if (logic.isThreadActive()) {
                // Each thread gets its share of energy
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
     * Energy is split evenly among all active threads.
     */
    private long getEnergyPerThread() {
        if (energyContainer == null || activeThreadCount == 0) return 0;
        return energyContainer.getInputVoltage() * totalAmperage / Math.max(1, getRunningThreadCount());
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

    @Override
    public void addDisplayText(List<Component> textList) {
        // Basic multiblock status
        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(isWorkingEnabled(), getRunningThreadCount() > 0);

        if (isFormed()) {
            // Thread status header
            builder.addCustom(tl -> {
                tl.add(Component.translatable("cosmiccore.machine.multithreaded.thread_status")
                        .withStyle(ChatFormatting.AQUA));
                tl.add(Component.translatable("cosmiccore.machine.multithreaded.max_threads",
                        FormattingUtil.formatNumbers(maxThreads))
                        .withStyle(ChatFormatting.GRAY));
                tl.add(Component.translatable("cosmiccore.machine.multithreaded.active_threads",
                        FormattingUtil.formatNumbers(getRunningThreadCount()),
                        FormattingUtil.formatNumbers(threadLogics.size()))
                        .withStyle(ChatFormatting.GRAY));
            });

            // Per-thread status
            builder.addCustom(tl -> {
                for (MultithreadedRecipeLogic logic : threadLogics.values()) {
                    String colorName = getColorName(logic.getThreadColor());
                    ChatFormatting statusColor = logic.isWorking() ? ChatFormatting.GREEN : ChatFormatting.YELLOW;

                    String status;
                    if (logic.isWorking()) {
                        int percent = (int) (logic.getProgressPercent() * 100);
                        status = percent + "%";
                    } else if (logic.isIdle()) {
                        status = "Idle";
                    } else if (logic.isWaiting()) {
                        status = "Waiting";
                    } else {
                        status = "Suspended";
                    }

                    tl.add(Component.literal("  [" + colorName + "] " + status)
                            .withStyle(statusColor));
                }
            });

            // Energy info
            builder.addEnergyUsageLine(energyContainer);
            builder.addEnergyTierLine(tier);
        }

        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

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
