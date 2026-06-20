package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.CosmicFavorite;
import com.ghostipedia.cosmiccore.integration.emi.CosmicRecipeFavorite;
import com.ghostipedia.cosmiccore.integration.emi.RecipeScreenAccessor;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import net.minecraft.client.Minecraft;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.registry.EmiStackProviders;
import dev.emi.emi.runtime.EmiFavorites;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds CTRL+A to pin stacks with their amount, CTRL+scroll to adjust pinned amounts,
 * and [ / ] keys to cycle bookmark groups.
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Shadow
    private static int lastMouseX;
    @Shadow
    private static int lastMouseY;

    @Shadow
    public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean notClick) {
        throw new AssertionError();
    }

    @Shadow
    public static void repopulatePanels(SidebarType type) {
        throw new AssertionError();
    }

    @Shadow
    private static List<SidebarPanel> panels;

    // SHIFT+click on page arrows to cycle groups, MMB to create/delete groups
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$groupMouseControls(double mouseX, double mouseY, int button,
                                                      CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        // Block CTRL+click drag reordering for recipe groups - EMI's slot-based drag doesn't work
        // with our multi-slot recipe layout
        if (button == 0 && EmiInput.isControlDown() && manager.getActiveGroup().isRecipeGroup()) {
            for (SidebarPanel panel : panels) {
                if (panel.getType() != SidebarType.FAVORITES) continue;
                if (panel.space == null) continue;

                int px = panel.space.tx;
                int py = panel.space.ty;
                int pw = panel.space.tw * 18;
                int ph = panel.space.th * 18;

                if (mouseX >= px && mouseX < px + pw && mouseY >= py && mouseY < py + ph) {
                    // Consume the click to prevent EMI's drag from starting
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        // Middle mouse button on favorites panel header:
        // MMB = create regular group
        // SHIFT+MMB = create recipe group
        // CTRL+SHIFT+MMB = delete group
        if (button == 2) {
            for (SidebarPanel panel : panels) {
                if (panel.getType() != SidebarType.FAVORITES) continue;
                if (!cosmiccore$isOverFavoritesHeader(panel, mouseX, mouseY)) continue;

                if (EmiInput.isControlDown() && EmiInput.isShiftDown()) {
                    manager.removeGroup(manager.getActiveIndex());
                } else if (EmiInput.isShiftDown()) {
                    manager.addGroup("Recipe " + (manager.getGroupCount() + 1), CosmicBookmarkGroup.GroupType.RECIPE);
                } else {
                    manager.addGroup("Group " + (manager.getGroupCount() + 1));
                }
                repopulatePanels(SidebarType.FAVORITES);
                cir.setReturnValue(true);
                return;
            }
        }

        // SHIFT+left click on page arrows to cycle groups
        if (button == 0 && EmiInput.isShiftDown() && !EmiInput.isControlDown()) {
            for (SidebarPanel panel : panels) {
                if (panel.getType() != SidebarType.FAVORITES) continue;

                if (panel.pageLeft.isMouseOver(mouseX, mouseY)) {
                    manager.prevGroup();
                    repopulatePanels(SidebarType.FAVORITES);
                    cir.setReturnValue(true);
                    return;
                } else if (panel.pageRight.isMouseOver(mouseX, mouseY)) {
                    manager.nextGroup();
                    repopulatePanels(SidebarType.FAVORITES);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Unique
    private static boolean cosmiccore$isOverFavoritesHeader(SidebarPanel panel, double mouseX, double mouseY) {
        if (panel.space == null) return false;
        int headerY = panel.space.ty - 18;
        int headerHeight = 18;
        int headerX = panel.space.tx;
        int headerWidth = panel.space.tw * 18;
        return mouseX >= headerX && mouseX < headerX + headerWidth && mouseY >= headerY &&
                mouseY < headerY + headerHeight;
    }

    // [ and ] keys to cycle bookmark groups
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$cycleGroups(int keyCode, int scanCode, int modifiers,
                                               CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        if (keyCode == GLFW.GLFW_KEY_RIGHT_BRACKET) {
            manager.nextGroup();
            repopulatePanels(SidebarType.FAVORITES);
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_LEFT_BRACKET) {
            manager.prevGroup();
            repopulatePanels(SidebarType.FAVORITES);
            cir.setReturnValue(true);
        } else if (keyCode == GLFW.GLFW_KEY_BACKSLASH && EmiInput.isControlDown()) {
            if (EmiInput.isShiftDown()) {
                manager.addGroup("Recipe " + (manager.getGroupCount() + 1), CosmicBookmarkGroup.GroupType.RECIPE);
            } else {
                manager.addGroup("Group " + (manager.getGroupCount() + 1));
            }
            repopulatePanels(SidebarType.FAVORITES);
            cir.setReturnValue(true);
        }
    }

    // Handle favorite key: plain A removes CosmicRecipeFavorite, CTRL+A pins with amount, CTRL+SHIFT+A pins recipe
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$ctrlAPin(int keyCode, int scanCode, int modifiers,
                                            CallbackInfoReturnable<Boolean> cir) {
        boolean ctrl = EmiInput.isControlDown();
        boolean shift = EmiInput.isShiftDown();
        boolean isFavKey = cosmiccore$isFavoriteKey(keyCode);

        if (!isFavKey) return;

        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        // Plain A on recipe group: find and remove the hovered recipe
        if (!ctrl && !shift && manager.getActiveGroup().isRecipeGroup()) {
            CosmicRecipeFavorite recipe = cosmiccore$getHoveredRecipeFavorite();
            if (recipe != null) {
                EmiFavorites.favorites.remove(recipe);
                repopulatePanels(SidebarType.FAVORITES);
                cir.setReturnValue(true);
                return;
            }
        }

        // Plain A on regular favorites: let EMI handle it
        if (!ctrl && !shift) {
            return;
        }

        if (!ctrl) return;

        var screen = Minecraft.getInstance().screen;

        // CTRL+SHIFT+A: Toggle recipe favorite (add or remove)
        if (shift && screen instanceof RecipeScreenAccessor recipeScreen) {
            EmiRecipe recipe = recipeScreen.getHoveredRecipe(lastMouseX, lastMouseY);
            if (recipe != null && !recipe.getOutputs().isEmpty()) {
                EmiStack output = recipe.getOutputs().get(0);

                // Check if a matching recipe favorite already exists - remove it if so
                int existingIndex = cosmiccore$findRecipeFavoriteIndex(output);
                if (existingIndex >= 0) {
                    EmiFavorites.favorites.remove(existingIndex);
                    repopulatePanels(SidebarType.FAVORITES);
                    cir.setReturnValue(true);
                    return;
                }

                // Switch to recipe group if needed
                if (!manager.getActiveGroup().isRecipeGroup()) {
                    int recipeGroupIndex = cosmiccore$findOrCreateRecipeGroup(manager);
                    manager.setActiveIndex(recipeGroupIndex);
                }

                long outputAmount = output.getAmount();
                if (outputAmount <= 0) outputAmount = 1;

                // Build input list
                List<CosmicRecipeFavorite.InputEntry> inputs = new ArrayList<>();
                for (EmiIngredient input : recipe.getInputs()) {
                    if (input.isEmpty()) continue;
                    long inputAmount = input.getEmiStacks().isEmpty() ? 1 : input.getEmiStacks().get(0).getAmount();
                    if (inputAmount <= 0) inputAmount = 1;
                    inputs.add(new CosmicRecipeFavorite.InputEntry(input, inputAmount));
                }

                CosmicRecipeFavorite recipeFav = new CosmicRecipeFavorite(output, outputAmount, inputs);
                EmiFavorites.favorites.add(recipeFav);

                repopulatePanels(SidebarType.FAVORITES);
                cir.setReturnValue(true);
                return;
            }
        }

        // CTRL+A: Pin stack with amount (existing behavior)
        EmiIngredient hoveredIngredient = EmiStack.EMPTY;

        EmiStackInteraction sidebarHovered = getHoveredStack(lastMouseX, lastMouseY, true);
        if (!sidebarHovered.getStack().isEmpty()) {
            hoveredIngredient = sidebarHovered.getStack();
        } else {
            if (screen instanceof RecipeScreenAccessor recipeScreen) {
                hoveredIngredient = recipeScreen.getHoveredStack(lastMouseX, lastMouseY);
            }
            if (hoveredIngredient.isEmpty() && screen != null) {
                hoveredIngredient = EmiStackProviders.getStackAt(screen, lastMouseX, lastMouseY, true).getStack();
            }
        }

        if (hoveredIngredient.isEmpty()) return;

        long amount = hoveredIngredient.getEmiStacks().isEmpty() ? 1 :
                hoveredIngredient.getEmiStacks().get(0).getAmount();
        if (amount <= 0) amount = 1;

        EmiFavorites.addFavorite(new CosmicFavorite(hoveredIngredient, amount), null);
        repopulatePanels(SidebarType.FAVORITES);
        cir.setReturnValue(true);
    }

    @Unique
    private static int cosmiccore$findOrCreateRecipeGroup(CosmicBookmarkManager manager) {
        for (int i = 0; i < manager.getGroupCount(); i++) {
            if (manager.getGroupAt(i).isRecipeGroup()) {
                return i;
            }
        }
        manager.addGroup("Recipe " + (manager.getGroupCount() + 1), CosmicBookmarkGroup.GroupType.RECIPE);
        return manager.getGroupCount() - 1;
    }

    @Unique
    private static int cosmiccore$findRecipeFavoriteIndex(EmiIngredient output) {
        for (int i = 0; i < EmiFavorites.favorites.size(); i++) {
            if (EmiFavorites.favorites.get(i) instanceof CosmicRecipeFavorite recipeFav) {
                if (recipeFav.strictEquals(output)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$ctrlScrollAdjust(double mouseX, double mouseY, double scrollDelta,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (!EmiInput.isControlDown()) return;

        EmiStackInteraction hovered = getHoveredStack((int) mouseX, (int) mouseY, true);
        if (!(hovered.getStack() instanceof CosmicFavorite fav)) return;

        long step = fav.getScrollStep(EmiInput.isShiftDown());
        fav.adjustAmount((long) scrollDelta * step);

        repopulatePanels(SidebarType.FAVORITES);
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean cosmiccore$isFavoriteKey(int keyCode) {
        return EmiConfig.favorite.boundKeys.stream()
                .anyMatch(k -> k.key().getValue() == keyCode);
    }

    @Unique
    private static CosmicRecipeFavorite cosmiccore$getHoveredRecipeFavorite() {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.getActiveGroup().isRecipeGroup()) return null;

        for (SidebarPanel panel : panels) {
            if (panel.getType() != SidebarType.FAVORITES) continue;
            if (panel.space == null) continue;

            int tx = panel.space.tx;
            int ty = panel.space.ty;
            int tw = panel.space.tw;
            int th = panel.space.th;

            CosmicRecipeFavorite recipe = manager.getRecipeAtPosition(
                    lastMouseX, lastMouseY, tx, ty, tw, th, manager.getCurrentRecipePage());
            if (recipe != null) {
                return recipe;
            }
        }

        return null;
    }
}
