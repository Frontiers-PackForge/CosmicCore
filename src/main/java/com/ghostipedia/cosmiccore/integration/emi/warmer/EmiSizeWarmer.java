package com.ghostipedia.cosmiccore.integration.emi.warmer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiReloadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public final class EmiSizeWarmer {

    public static volatile boolean enabled = true;

    private static final Logger LOGGER = LoggerFactory.getLogger("CosmicCore/EmiSizeWarmer");
    private static final long HOLD_BUDGET_NANOS = 30_000_000L;
    private static final int HOLD_MAX_STEPS = 5000;

    private static final ArrayDeque<ModularUIEmiRecipe> QUEUE = new ArrayDeque<>();
    private static volatile boolean bakeSeen = false;
    private static volatile boolean warmComplete = false;
    private static boolean loggedError = false;
    private static int totalQueued = 0;
    private static int warmedTotal = 0;

    private EmiSizeWarmer() {}

    public static boolean isHoldingEmi() {
        return enabled && bakeSeen && !warmComplete;
    }

    public static void tick() {
        if (!enabled) return;

        boolean baked = Minecraft.getInstance().level != null && EmiReloadManager.getStatus() == 2;
        if (!baked) {
            if (bakeSeen) reset();
            return;
        }

        if (!bakeSeen) {
            refill();
            bakeSeen = true;
            totalQueued = QUEUE.size();
            LOGGER.info("EMI size warmer: holding EMI and pre-sizing {} recipes", totalQueued);
            if (QUEUE.isEmpty()) {
                warmComplete = true;
                return;
            }
        }

        if (warmComplete) return;

        long deadline = System.nanoTime() + HOLD_BUDGET_NANOS;
        int steps = 0;
        while (!QUEUE.isEmpty() && steps < HOLD_MAX_STEPS && System.nanoTime() < deadline) {
            ModularUIEmiRecipe recipe = QUEUE.poll();
            steps++;
            warmedTotal++;
            try {
                recipe.getDisplayWidth();
            } catch (Throwable t) {
                if (!loggedError) {
                    loggedError = true;
                    LOGGER.debug("EMI size warmer: a recipe failed to pre-size; skipping it and continuing", t);
                }
            }
        }

        if (QUEUE.isEmpty()) {
            warmComplete = true;
            EmiReloadManager.reloadStep = Component.literal("");
            LOGGER.info("EMI size warmer: done; pre-sized {} recipes, releasing EMI", warmedTotal);
        } else {
            EmiReloadManager.reloadWorry = Long.MAX_VALUE;
            EmiReloadManager.reloadStep = Component.literal(
                    "CosmicCore: pre-sizing recipes " + warmedTotal + "/" + totalQueued);
        }
    }

    private static void refill() {
        QUEUE.clear();
        loggedError = false;
        warmComplete = false;
        warmedTotal = 0;
        for (EmiRecipe recipe : EmiApi.getRecipeManager().getRecipes()) {
            if (recipe instanceof ModularUIEmiRecipe modularRecipe) {
                QUEUE.add(modularRecipe);
            }
        }
    }

    private static void reset() {
        QUEUE.clear();
        bakeSeen = false;
        warmComplete = false;
        warmedTotal = 0;
        totalQueued = 0;
    }
}
