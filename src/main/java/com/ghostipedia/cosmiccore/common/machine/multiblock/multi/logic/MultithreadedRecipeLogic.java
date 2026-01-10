package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Getter;
import lombok.Setter;
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

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MultithreadedRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);

    @Getter
    private final int threadIndex;

    @Getter
    private final int threadColor;

    @Persisted
    @DescSynced
    @Getter
    private boolean threadActive = false;

    /**
     * Maximum EU/t this thread can use.
     * Set by the parent MultithreadedMachine based on energy hatch amperage / thread count.
     */
    @Setter
    @Getter
    private long maxEUtPerThread = 0;

    /**
     * This thread's capability proxy - filtered to only include its handlers.
     * Set by the parent MultithreadedMachine.
     */
    @Setter
    private Map<IO, List<RecipeHandlerList>> threadCapabilitiesProxy = new EnumMap<>(IO.class);

    /**
     * Flattened capability map for this thread.
     */
    @Setter
    private Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> threadCapabilitiesFlat = new EnumMap<>(IO.class);

    public MultithreadedRecipeLogic(IRecipeLogicMachine machine, int threadIndex, int threadColor) {
        super(machine);
        this.threadIndex = threadIndex;
        this.threadColor = threadColor;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        return threadActive && machine.isRecipeLogicAvailable();
    }

    @Override
    public void serverTick() {
        if (!canWork()) {
            if (isWorking()) {
                // Thread was deactivated mid-recipe, pause it
                setStatus(Status.SUSPEND);
            }
            return;
        }
        super.serverTick();
    }

    /**
     * Get the parent MultithreadedMachine.
     */
    @Nullable
    private MultithreadedMachine getParentMachine() {
        if (machine instanceof MultithreadedMachine mtm) {
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
     * NOT yet modified by the machine. We must NOT call machine.fullModifyRecipe()
     * as that would apply overclock based on full machine power.
     */
    @Override
    public boolean checkMatchedRecipeAvailable(GTRecipe match) {
        // Get the BASE recipe EU/t before any modification
        long baseEUt = match.getInputEUt().getTotalEU();

        // Check if base recipe fits within thread's budget
        if (baseEUt > maxEUtPerThread && maxEUtPerThread > 0) {
            // Base recipe too expensive for this thread
            return false;
        }

        // Apply our custom overclock within thread's energy budget
        GTRecipe modified = applyThreadOverclock(match);
        if (modified == null) {
            return false;
        }

        // Trim outputs to fit in output slots
        GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, machine.getOutputLimits());
        if (trimmed == null) {
            return false;
        }

        // Check if the modified recipe matches our thread's inputs
        ActionResult result = checkRecipe(trimmed);
        if (result.isSuccess()) {
            // Store the modified recipe for execution
            setupRecipe(trimmed);

            // IMPORTANT: Store the original (unmodified) recipe for later re-application
            // This is used by onRecipeFinish to re-overclock when repeating the recipe
            if (lastRecipe != null && getStatus() == Status.WORKING) {
                lastOriginRecipe = match;
                lastFailedMatches = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Override searchRecipe to use THIS THREAD as the capability holder,
     * not the machine. This is critical for proper recipe filtering.
     */
    @Override
    public @NotNull Iterator<GTRecipe> searchRecipe() {
        // Use THIS thread as the capability holder for recipe searching
        return machine.getRecipeType().searchRecipe(this, r -> matchRecipe(r).isSuccess());
    }

    /**
     * Override findAndHandleRecipe to ensure we ALWAYS go through our custom
     * checkMatchedRecipeAvailable, even when re-running a cached recipe.
     * The base implementation has a shortcut path that bypasses our EU budget checks.
     */
    @Override
    public void findAndHandleRecipe() {
        lastFailedMatches = null;

        // If we have a cached origin recipe, try to re-apply our thread-specific overclock
        if (!recipeDirty && lastOriginRecipe != null) {
            // Re-apply OUR thread overclock to the origin recipe
            GTRecipe modified = applyThreadOverclock(lastOriginRecipe);
            if (modified != null) {
                GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, machine.getOutputLimits());
                if (trimmed != null && checkRecipe(trimmed).isSuccess()) {
                    setupRecipe(trimmed);
                    recipeDirty = false;
                    return;
                }
            }
        }

        // No valid cached recipe, search for a new one
        lastRecipe = null;
        lastOriginRecipe = null;
        handleSearchingRecipes(searchRecipe());
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
        // Use this thread as the capability holder instead of the machine
        return RecipeHelper.handleTickRecipeIO(this, recipe, io, chanceCaches);
    }

    /**
     * Override to use this thread's capability holder for tick recipe matching.
     * This is critical for EU consumption - the base implementation uses the machine,
     * but we need to use this thread's handlers.
     */
    @Override
    public ActionResult handleTickRecipe(GTRecipe recipe) {
        if (recipe.hasTick()) {
            // Use this thread as the capability holder for matching
            var result = RecipeHelper.matchTickRecipe(this, recipe);
            if (!result.isSuccess()) {
                return result;
            }
            result = handleTickRecipeIO(recipe, IO.IN);
            if (!result.isSuccess()) {
                return result;
            }
            return handleTickRecipeIO(recipe, IO.OUT);
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Override onRecipeFinish to prevent the base class from applying machine-level overclock.
     * The base implementation calls machine.fullModifyRecipe(lastOriginRecipe) which would
     * apply overclock based on full machine power, ignoring our thread budget.
     */
    @Override
    public void onRecipeFinish() {
        machine.afterWorking();
        if (lastRecipe != null) {
            // Reset run attempt tracking
            // Note: runAttempt and runDelay are package-private in base class
            // but we can still access them since we're in the same hierarchy

            consecutiveRecipes++;
            handleRecipeIO(lastRecipe, IO.OUT);

            // CRITICAL: Do NOT call machine.fullModifyRecipe here!
            // Instead, re-apply OUR thread-specific overclock to the origin recipe
            if (lastOriginRecipe != null) {
                GTRecipe modified = applyThreadOverclock(lastOriginRecipe);
                if (modified == null) {
                    markLastRecipeDirty();
                } else {
                    // Trim outputs
                    GTRecipe trimmed = RecipeHelper.trimRecipeOutputs(modified, machine.getOutputLimits());
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
