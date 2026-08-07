package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Iterator;

/**
 * A RecipeLogic instance that represents a single "thread" in a MultithreadedMachine.
 * Each thread can process one recipe independently of other threads.
 * <p>
 * This class implements IRecipeCapabilityHolder to provide a filtered view of handlers
 * that only includes this thread's color-coded inputs and shared outputs.
 */
public class MultithreadedRecipeLogic extends RecipeLogic implements IRecipeCapabilityHolder {

    /**
     * Toggle to log per-thread recipe matching and tick-drain decisions. Flip on when investigating thread behavior.
     */
    public static boolean DEBUG = false;

    private static String describe(ActionResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.reason() == null ? "<no reason>" : r.reason().getString());
        if (r.capability() != null) sb.append(" cap=").append(r.capability().id);
        if (r.io() != null) sb.append(" io=").append(r.io());
        return sb.toString();
    }

    private final int threadIndex;

    @SaveField
    @SyncToClient
    private int threadColor;

    @SaveField
    @SyncToClient
    private boolean threadActive = false;

    /**
     * Maximum EU/t this thread can use.
     * Set by the parent MultithreadedMachine based on energy hatch amperage / thread count.
     */
    private long maxEUtPerThread = 0;

    /**
     * This thread's capability proxy - filtered to only include its handlers.
     * Set by the parent MultithreadedMachine.
     */
    private Map<IO, List<RecipeHandlerList>> threadCapabilitiesProxy = new EnumMap<>(IO.class);

    /**
     * Flattened capability map for this thread.
     */
    private Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadCapabilitiesFlat = new EnumMap<>(IO.class);

    public MultithreadedRecipeLogic(int threadIndex, int threadColor) {
        super();
        this.threadIndex = threadIndex;
        this.threadColor = threadColor;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public int getThreadColor() {
        return threadColor;
    }

    public void bindThreadColor(int threadColor) {
        if (this.threadColor == threadColor) return;
        deactivateThread();
        setStatus(Status.IDLE);
        progress = 0;
        duration = 0;
        consecutiveRecipes = 0;
        isActive = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        lastFailedMatches = null;
        recipeDirty = true;
        clearFailureReason();
        this.threadColor = threadColor;
    }

    public boolean isThreadActive() {
        return threadActive;
    }

    public long getMaxEUtPerThread() {
        return maxEUtPerThread;
    }

    public void setMaxEUtPerThread(long maxEUtPerThread) {
        this.maxEUtPerThread = maxEUtPerThread;
    }

    public void setThreadCapabilitiesProxy(Map<IO, List<RecipeHandlerList>> threadCapabilitiesProxy) {
        this.threadCapabilitiesProxy = threadCapabilitiesProxy;
    }

    public void setThreadCapabilitiesFlat(
                                          Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadCapabilitiesFlat) {
        this.threadCapabilitiesFlat = threadCapabilitiesFlat;
    }

    // === IRecipeCapabilityHolder implementation ===
    // These methods provide a filtered view of handlers for this thread only

    @Override
    public Map<IO, List<RecipeHandlerList>> getCapabilitiesProxy() {
        return threadCapabilitiesProxy;
    }

    @Override
    public Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat() {
        return threadCapabilitiesFlat;
    }

    /**
     * Called by the parent machine to activate this thread.
     */
    public void activateThread() {
        this.threadActive = true;
    }

    /**
     * Called by the parent machine to deactivate this thread.
     */
    public void deactivateThread() {
        this.threadActive = false;
        if (isWorking()) {
            setStatus(Status.IDLE);
            this.progress = 0;
            this.duration = 0;
            this.lastRecipe = null;
        }
    }

    /**
     * Check if this thread can process recipes.
     */
    public boolean canWork() {
        return threadActive && getRLMachine().isRecipeLogicAvailable();
    }

    @Override
    public void serverTick() {
        if (!canWork()) {
            if (isWorking()) {
                setStatus(Status.SUSPEND);
            }
            return;
        }
        super.serverTick();
    }

    /**
     * MTRL already has it's own ticksub, this fuckass no-op prevents double dipping into the EU pool the machine has
     * and breaking thread EU alloc
     * this took me 2hrs to realize this fucking override existed again, ugh.
     */
    @Override
    public void updateTickSubscription() {}

    /**
     * Get the parent MultithreadedMachine.
     */
    @Nullable
    private MultithreadedMachine getParentMachine() {
        if (getMachine() instanceof MultithreadedMachine mtm) {
            return mtm;
        }
        return null;
    }

    /**
     * Standard overclock voltage multiplier (4x per OC level).
     */
    private static final double OC_VOLTAGE_FACTOR = 4.0;

    /**
     * Standard overclock duration multiplier (0.5x per OC level - halves duration).
     */
    private static final double OC_DURATION_FACTOR = 0.5;

    /**
     * Calculate the maximum number of overclock levels that fit within the thread's EU/t budget.
     * Each overclock level multiplies EU/t by 4 and halves duration.
     *
     * @param baseEUt The base recipe EU/t
     * @return Number of overclock levels possible (0 = no overclocking)
     */
    protected int calculateMaxOverclockLevels(long baseEUt) {
        if (maxEUtPerThread <= 0 || baseEUt <= 0) {
            return 0;
        }

        int levels = 0;
        long currentEUt = baseEUt;

        // Each OC level multiplies EU/t by 4
        while (currentEUt * OC_VOLTAGE_FACTOR <= maxEUtPerThread) {
            currentEUt = (long) (currentEUt * OC_VOLTAGE_FACTOR);
            levels++;
        }

        return levels;
    }

    /**
     * Apply overclocking to a recipe within this thread's energy budget.
     * Overclocking multiplies EU/t by 4 and halves duration per level.
     *
     * @param recipe The base recipe
     * @return The overclocked recipe copy, or a copy of the original if no overclocking is possible
     */
    @Nullable
    protected GTRecipe applyThreadOverclock(GTRecipe recipe) {
        long baseEUt = recipe.getInputEUt().getTotalEU();

        // If base recipe exceeds budget, can't run at all
        if (baseEUt > maxEUtPerThread && maxEUtPerThread > 0) {
            return null;
        }

        int ocLevels = calculateMaxOverclockLevels(baseEUt);
        if (ocLevels <= 0) {
            // No overclocking possible, return a copy of the recipe
            return recipe.copy();
        }

        // Calculate overclocked values
        double eutMultiplier = Math.pow(OC_VOLTAGE_FACTOR, ocLevels);
        double durationMultiplier = Math.pow(OC_DURATION_FACTOR, ocLevels);

        // Build modifier to apply overclock
        ModifierFunction modifier = ModifierFunction.builder()
                .eutMultiplier(eutMultiplier)
                .durationMultiplier(durationMultiplier)
                .build();

        // Apply to a COPY of the recipe to avoid modifying the original
        return modifier.apply(recipe.copy());
    }

    /**
     * Override to apply thread-specific recipe modification and check availability.
     * Applies overclocking within the thread's energy budget.
     *
     * IMPORTANT: The 'match' parameter is the RAW recipe from the recipe type,
     * NOT yet modified by the getRLMachine(). We must NOT call getRLMachine().fullModifyRecipe()
     * as that would apply overclock based on full machine power.
     */
    @Override
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        long baseEUt = match.getInputEUt().getTotalEU();

        if (baseEUt > maxEUtPerThread && maxEUtPerThread > 0) {
            if (DEBUG) CosmicCore.LOGGER.info("[basin t#{} c=0x{}] reject {}: EU {} > budget {}",
                    threadIndex, Integer.toHexString(threadColor), match.getId(), baseEUt, maxEUtPerThread);
            return false;
        }

        GTRecipe modified = applyThreadOverclock(match);
        if (modified == null) {
            if (DEBUG) CosmicCore.LOGGER.info("[basin t#{} c=0x{}] reject {}: overclock returned null",
                    threadIndex, Integer.toHexString(threadColor), match.getId());
            return false;
        }

        GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, getRLMachine().getOutputLimits());
        if (trimmed == null) {
            if (DEBUG) CosmicCore.LOGGER.info("[basin t#{} c=0x{}] reject {}: trimRecipeOutputs returned null",
                    threadIndex, Integer.toHexString(threadColor), match.getId());
            return false;
        }

        ActionResult result = checkRecipe(trimmed);
        if (!result.isSuccess()) {
            recordFailureReason(match, result.reason(), result.score());
            if (DEBUG) CosmicCore.LOGGER.info("[basin t#{} c=0x{}] reject {}: checkRecipe -> {}",
                    threadIndex, Integer.toHexString(threadColor), match.getId(),
                    describe(result));
            return false;
        }

        setupRecipe(trimmed);

        if (lastRecipe != null && getStatus() == Status.WORKING) {
            lastOriginRecipe = match;
            lastFailedMatches = null;
            if (DEBUG) CosmicCore.LOGGER.info("[basin t#{} c=0x{}] STARTED {}",
                    threadIndex, Integer.toHexString(threadColor), match.getId());
            return true;
        }

        if (DEBUG) CosmicCore.LOGGER.info(
                "[basin t#{} c=0x{}] reject {}: setupRecipe finished but lastRecipe={} status={}",
                threadIndex, Integer.toHexString(threadColor), match.getId(),
                lastRecipe == null ? "null" : "<set>", getStatus());
        return false;
    }

    /**
     * Override searchRecipe to use THIS THREAD as the capability holder,
     * not the getRLMachine(). This is critical for proper recipe filtering.
     */
    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe() {
        if (DEBUG) {
            var flat = getCapabilitiesFlat();
            var inMap = flat.getOrDefault(IO.IN, java.util.Collections.emptyMap());
            var outMap = flat.getOrDefault(IO.OUT, java.util.Collections.emptyMap());
            CosmicCore.LOGGER.info("[basin t#{} c=0x{}] searchRecipe: IN caps={} OUT caps={} budget={}",
                    threadIndex, Integer.toHexString(threadColor),
                    inMap.keySet(), outMap.keySet(), maxEUtPerThread);
        }
        return getRLMachine().getRecipeType().searchRecipe(this, recipe -> true);
    }

    /**
     * Override findAndHandleRecipe to ensure we ALWAYS go through our custom
     * checkMatchedRecipeAvailable, even when re-running a cached recipe.
     * The base implementation has a shortcut path that bypasses our EU budget checks.
     */
    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;
        clearFailureReason();

        // If we have a cached origin recipe, try to re-apply our thread-specific overclock
        if (!recipeDirty && lastOriginRecipe != null) {
            // Re-apply OUR thread overclock to the origin recipe
            GTRecipe modified = applyThreadOverclock(lastOriginRecipe);
            if (modified != null) {
                GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, getRLMachine().getOutputLimits());
                if (trimmed != null) {
                    ActionResult result = checkRecipe(trimmed);
                    if (result.isSuccess()) {
                        setupRecipe(trimmed);
                        recipeDirty = false;
                        return;
                    }
                    recordFailureReason(trimmed, result.reason(), Double.POSITIVE_INFINITY);
                }
            }
        }

        // No valid cached recipe, search for a new one
        lastRecipe = null;
        lastOriginRecipe = null;
        handleSearchingRecipes(searchRecipe());
        syncDataHolder.markClientSyncFieldDirty("lastRecipe");
        recipeDirty = false;
    }

    /**
     * Override checkRecipe to use this thread's capability holder.
     * Also checks recipe conditions.
     */
    @Override
    public ActionResult checkRecipe(GTRecipe recipe) {
        // First check recipe conditions
        var conditionResult = RecipeHelper.checkConditions(recipe, this);
        if (!conditionResult.isSuccess()) return conditionResult;

        // Use this thread as the capability holder for matching
        return matchRecipe(recipe);
    }

    /**
     * Override to use this thread's capability holder for recipe matching.
     * This ensures recipe searching uses this thread's handlers.
     */
    @Override
    protected ActionResult matchRecipe(GTRecipe recipe) {
        // Use this thread as the capability holder instead of the machine
        return RecipeHelper.matchContents(this, recipe);
    }

    /**
     * Override to use this thread's handlers instead of the machine's global handlers.
     */
    @Override
    protected ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
        // Use this thread as the capability holder instead of the machine
        return RecipeHelper.handleRecipeIO(this, recipe, io, chanceCaches);
    }

    /**
     * Override to use this thread's handlers for tick-based IO.
     */
    @Override
    protected ActionResult handleTickRecipeIO(GTRecipe recipe, IO io) {
        if (DEBUG && io == IO.IN && getMachine() instanceof MultithreadedMachine mtm) {
            var ec = mtm.getEnergyContainer();
            long before = ec != null ? ec.getEnergyStored() : -1;
            long requested = sumEURequest(recipe);
            ActionResult result = RecipeHelper.handleTickRecipeIO(this, recipe, io, chanceCaches);
            long after = ec != null ? ec.getEnergyStored() : -1;
            CosmicCore.LOGGER.info(
                    "[basin t#{} c=0x{}] tick drain: req={} drained={} buffer {} -> {} {}",
                    threadIndex, Integer.toHexString(threadColor), requested, before - after, before, after,
                    result.isSuccess() ? "OK" : "FAIL: " + describe(result));
            return result;
        }
        return RecipeHelper.handleTickRecipeIO(this, recipe, io, chanceCaches);
    }

    private static long sumEURequest(GTRecipe recipe) {
        var euList = recipe.tickInputs.get(EURecipeCapability.CAP);
        if (euList == null) return 0;
        long total = 0;
        for (var content : euList) {
            if (content.content() instanceof com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack es) {
                total += es.getTotalEU();
            }
        }
        return total;
    }

    /**
     * Override to use this thread's capability holder for tick recipe matching.
     * This is critical for EU consumption - the base implementation uses the machine,
     * but we need to use this thread's handlers.
     */
    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (!recipe.hasTick()) return ActionResult.SUCCESS;

        var result = RecipeHelper.matchTickRecipe(this, recipe);
        if (!result.isSuccess()) return result;

        result = handleTickRecipeIO(recipe, IO.IN);
        if (!result.isSuccess()) return result;

        return handleTickRecipeIO(recipe, IO.OUT);
    }

    /**
     * Override onRecipeFinish to prevent the base class from applying machine-level overclock.
     * The base implementation calls getRLMachine().fullModifyRecipe(lastOriginRecipe) which would
     * apply overclock based on full machine power, ignoring our thread budget.
     */
    @Override
    public void onRecipeFinish() {
        getRLMachine().afterWorking();
        if (lastRecipe != null) {
            // Reset run attempt tracking
            // Note: runAttempt and runDelay are package-private in base class
            // but we can still access them since we're in the same hierarchy

            consecutiveRecipes++;
            handleRecipeIO(lastRecipe, IO.OUT);

            // CRITICAL: Do NOT call getRLMachine().fullModifyRecipe here!
            // Instead, re-apply OUR thread-specific overclock to the origin recipe
            if (lastOriginRecipe != null) {
                GTRecipe modified = applyThreadOverclock(lastOriginRecipe);
                if (modified == null) {
                    markLastRecipeDirty();
                } else {
                    // Trim outputs
                    GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, getRLMachine().getOutputLimits());
                    if (trimmed == null) {
                        markLastRecipeDirty();
                    } else {
                        lastRecipe = trimmed;
                    }
                }
            } else {
                markLastRecipeDirty();
            }

            // Try to run the recipe again
            var recipeCheck = checkRecipe(lastRecipe);
            if (!recipeDirty && !isSuspendAfterFinish() && recipeCheck.isSuccess()) {
                setupRecipe(lastRecipe);
            } else {
                if (isSuspendAfterFinish()) {
                    setStatus(Status.SUSPEND);
                } else {
                    setStatus(Status.IDLE);
                }
                consecutiveRecipes = 0;
                progress = 0;
                duration = 0;
                isActive = false;
            }
        }
    }

    /**
     * Get the recipe currently being processed by this thread.
     */
    @Nullable
    public GTRecipe getCurrentRecipe() {
        return lastRecipe;
    }

    /**
     * Get progress as a percentage (0.0 to 1.0)
     */
    public double getProgressPercent() {
        if (duration == 0) return 0;
        return (double) progress / duration;
    }
}
