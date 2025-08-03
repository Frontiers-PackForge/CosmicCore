package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.mixin.accessor.RecipeLogicAccessor;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiRecipeLogic extends RecipeLogic {

    public final IRecipeLogicMachine machine;

    private List<RecipeLogic> logics = new ArrayList<>();

    protected TickableSubscription subscription;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MultiRecipeLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    public MultiRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
        this.machine = machine;
    }

    public void resetAllLogics() {
        for(var logic : logics) {
            logic.resetRecipeLogic();
        }
    }

    public void resetRecipeLogic(RecipeLogic logic) {
        if(logics.contains(logic)) {
            logic.resetRecipeLogic();
        }
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        updateTickSubscription();
    }

    public void updateTickSubscription() {
        boolean allSuspended = true;
        for(var logic : logics) {
            if(!logic.isSuspend()) {
                allSuspended = false;
                break;
            }
        }
        if(logics.isEmpty()) {
            allSuspended = false;
        }
        if (!machine.isRecipeLogicAvailable() || allSuspended) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        } else {
            subscription = getMachine().subscribeServerTick(subscription, this::serverTick);
        }
    }

    public void addLogic(RecipeLogic logic) {
        logics.add(logic);
    }

    public void removeLogic(RecipeLogic logic) {
        if(!logic.isSuspend() || logic.getLastRecipe() != null || !logic.lastFailedMatches.isEmpty() || logic.getProgress() != 0) {
            GTCEu.LOGGER.warn("tryign to remove a recipe logic when it is still running!");
            return;
        }
        logics.remove(logic);
    }

    @Override
    public void serverTick() {
        for(var logic : logics) {
            if (!logic.isSuspend()) {
                if (!logic.isIdle() && logic.getLastRecipe() != null) {
                    if (logic.getProgress() < logic.getDuration()) {
                        int delay = ((RecipeLogicAccessor)logic).getRunDelay();
                        if (delay > 0) {
                            ((RecipeLogicAccessor)logic).setRunDelay(--delay);
                        } else {
                            logic.handleRecipeWorking();
                        }
                    }
                    if (logic.getProgress() < logic.getDuration()) {
                        logic.onRecipeFinish();
                    }
                } else if (logic.getLastRecipe() != null) {
                    findAndHandleRecipe(logic);
                } else if (!logic.machine.keepSubscribing() || logic.getMachine().getOffsetTimer() % 5 == 0) {
                    findAndHandleRecipe(logic);
                    if (logic.lastFailedMatches != null) {
                        for (GTRecipe match : logic.lastFailedMatches) {
                            if (logic.checkMatchedRecipeAvailable(match)) break;
                        }
                    }
                }
            }
            boolean unsubscribe = false;
            if (isSuspend()) {
                // Machine is paused and can unsubscribe
                unsubscribe = true;
            } else if (lastRecipe == null && isIdle() && !machine.keepSubscribing() && !recipeDirty &&
                    lastFailedMatches == null) {
                // No recipes available and the machine wants to unsubscribe until notified
                unsubscribe = true;
            }

            if (unsubscribe && subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    public void findAndHandleRecipe(RecipeLogic logic) {
        logic.lastFailedMatches = null;
        // try to execute last recipe if possible
        if (!logic.isRecipeDirty() && logic.getLastRecipe() != null && ((RecipeLogicAccessor)logic).callCheckRecipe(lastRecipe).isSuccess()) {
            GTRecipe recipe = logic.getLastRecipe();
            ((RecipeLogicAccessor)logic).setLastRecipe(null);
            ((RecipeLogicAccessor)logic).setLastOriginRecipe(null);
            logic.setupRecipe(recipe);
        } else { // try to find and handle a new recipe
            ((RecipeLogicAccessor)logic).setLastRecipe(null);
            ((RecipeLogicAccessor)logic).setLastOriginRecipe(null);
            handleSearchingRecipes(logic.searchRecipe(), logic);
        }
        ((RecipeLogicAccessor)logic).setRecipeDirty(false);
    }

    private boolean isRecipeAlreadyRunning(GTRecipe recipe) {
        for (var logic : logics) {
            if(logic.getLastOriginRecipe() == recipe) {
                return true;
            }
        }
        return false;
    }

    protected void handleSearchingRecipes(@NotNull Iterator<GTRecipe> matches, RecipeLogic logic) {
        while (matches.hasNext()) {
            GTRecipe match = matches.next();
            if (match == null) continue;

            // If a new recipe was found, cache found recipe.
            if (logic.checkMatchedRecipeAvailable(match) && !isRecipeAlreadyRunning(match))
                return;

            // cache matching recipes.
            if (logic.lastFailedMatches == null) {
                logic.lastFailedMatches = new ArrayList<>();
            }
            logic.lastFailedMatches.add(match);
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
