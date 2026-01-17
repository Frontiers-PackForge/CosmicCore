package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicRecipeBookmark;
import com.ghostipedia.cosmiccore.integration.emi.favorites.GroupRenameScreen;
import com.ghostipedia.cosmiccore.integration.emi.favorites.TodoListBoundsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

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
    public static EmiScreenManager.SidebarPanel getPanelFor(SidebarType type) {
        throw new AssertionError();
    }

    @Unique
    private static int cosmiccore$groupArrowLeftX = 0;
    @Unique
    private static int cosmiccore$groupArrowLeftY = 0;
    @Unique
    private static int cosmiccore$groupArrowRightX = 0;
    @Unique
    private static int cosmiccore$groupArrowRightY = 0;
    @Unique
    private static boolean cosmiccore$arrowsVisible = false;
    @Unique
    private static int cosmiccore$groupNameX = 0;
    @Unique
    private static int cosmiccore$groupNameY = 0;
    @Unique
    private static int cosmiccore$groupNameWidth = 0;
    @Unique
    private static int cosmiccore$groupNameHeight = 0;
    @Unique
    private static double cosmiccore$scrollAccumulator = 0;

    @Unique
    private static final int ARROW_LEFT_U = 224;
    @Unique
    private static final int ARROW_RIGHT_U = 240;
    @Unique
    private static final int ARROW_V_NORMAL = 0;
    @Unique
    private static final int ARROW_V_HOVER = 16;
    @Unique
    private static final int ARROW_SIZE = 16;
    @Unique
    private static final int PLUS_U = 82;
    @Unique
    private static final int PLUS_V = 0;
    @Unique
    private static final int PLUS_SIZE = 13;
    @Unique
    private static int cosmiccore$plusButtonX = 0;
    @Unique
    private static int cosmiccore$plusButtonY = 0;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$onKeyPressed(int keyCode, int scanCode, int modifiers,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!cosmiccore$wouldMatchFavoriteKey(keyCode, scanCode)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        if (manager.getActiveViewMode() == CosmicBookmarkGroup.ViewMode.TODO_LIST) {
            if (!EmiInput.isControlDown() && !EmiInput.isShiftDown()) {
                List<CosmicRecipeBookmark> recipes = manager.getActiveRecipeBookmarks();
                int recipeIndex = cosmiccore$getTodoListHoveredRecipeIndex(lastMouseX, lastMouseY);
                if (recipeIndex >= 0 && recipeIndex < recipes.size()) {
                    manager.removeRecipeBookmark(recipeIndex);
                    repopulatePanels(SidebarType.FAVORITES);
                    cir.setReturnValue(true);
                    return;
                }
            }

            if (EmiInput.isControlDown() && EmiInput.isShiftDown()) {
                if (mc.screen instanceof RecipeScreen recipeScreen) {
                    EmiRecipe currentRecipe = cosmiccore$getCurrentRecipe(recipeScreen);
                    if (currentRecipe != null) {
                        boolean success = manager.bookmarkRecipeWithInputs(currentRecipe);
                        if (success) {
                            repopulatePanels(SidebarType.FAVORITES);
                            if (mc.player != null) {
                                mc.player.displayClientMessage(
                                        Component.literal("§6[Bookmarks]§r Pinned recipe to TODO list"),
                                        true);
                            }
                        }
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }

            cir.setReturnValue(false);
            return;
        }

        if (EmiInput.isControlDown() && EmiInput.isShiftDown()) {
            if (mc.screen instanceof RecipeScreen recipeScreen) {
                EmiRecipe currentRecipe = cosmiccore$getCurrentRecipe(recipeScreen);

                if (currentRecipe != null) {
                    boolean success = manager.bookmarkRecipeWithInputs(currentRecipe);

                    if (success) {
                        repopulatePanels(SidebarType.FAVORITES);

                        if (mc.player != null) {
                            int inputCount = currentRecipe.getInputs().size();
                            int outputCount = currentRecipe.getOutputs().size();
                            mc.player.displayClientMessage(
                                    Component.literal("§6[Bookmarks]§r Pinned recipe with §e" +
                                            outputCount + "§r output(s) and §e" + inputCount + "§r input(s)"),
                                    true);
                        }
                    }

                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (EmiInput.isControlDown() && !EmiInput.isShiftDown()) {
            EmiIngredient ingredient = cosmiccore$getHoveredIngredient();

            if (!ingredient.isEmpty()) {
                long amount = ingredient.getAmount();
                if (!ingredient.getEmiStacks().isEmpty()) {
                    EmiStack first = ingredient.getEmiStacks().get(0);
                    amount = first.getAmount();
                }

                CosmicBookmarkManager.getInstance().addFavoriteWithAmount(ingredient, null, amount);
                repopulatePanels(SidebarType.FAVORITES);

                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Unique
    private static EmiRecipe cosmiccore$getCurrentRecipe(RecipeScreen recipeScreen) {
        try {
            java.lang.reflect.Field currentPageField = RecipeScreen.class.getDeclaredField("currentPage");
            currentPageField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<?> currentPage = (List<?>) currentPageField.get(recipeScreen);

            if (currentPage == null || currentPage.isEmpty()) {
                return null;
            }

            java.lang.reflect.Field recipeField = null;
            java.lang.reflect.Field xField = null;
            java.lang.reflect.Field yField = null;
            java.lang.reflect.Field widthField = null;
            java.lang.reflect.Field heightField = null;

            EmiRecipe fallbackRecipe = null;

            for (Object widgetGroup : currentPage) {
                if (recipeField == null) {
                    Class<?> wgClass = widgetGroup.getClass();
                    recipeField = wgClass.getDeclaredField("recipe");
                    recipeField.setAccessible(true);
                    xField = wgClass.getDeclaredField("x");
                    xField.setAccessible(true);
                    yField = wgClass.getDeclaredField("y");
                    yField.setAccessible(true);
                    widthField = wgClass.getDeclaredField("width");
                    widthField.setAccessible(true);
                    heightField = wgClass.getDeclaredField("height");
                    heightField.setAccessible(true);
                }

                EmiRecipe recipe = (EmiRecipe) recipeField.get(widgetGroup);

                if (recipe == null) {
                    continue;
                }

                if (fallbackRecipe == null) {
                    fallbackRecipe = recipe;
                }

                int groupX = xField.getInt(widgetGroup);
                int groupY = yField.getInt(widgetGroup);
                int groupWidth = widthField.getInt(widgetGroup);
                int groupHeight = heightField.getInt(widgetGroup);

                if (lastMouseX >= groupX && lastMouseX < groupX + groupWidth &&
                        lastMouseY >= groupY && lastMouseY < groupY + groupHeight) {
                    return recipe;
                }
            }

            return fallbackRecipe;

        } catch (Exception e) {
            CosmicCore.LOGGER.error("Failed to get hovered recipe from RecipeScreen", e);
        }
        return null;
    }

    @Unique
    private static EmiIngredient cosmiccore$getHoveredIngredient() {
        Minecraft mc = Minecraft.getInstance();

        EmiStackInteraction hovered = getHoveredStack(lastMouseX, lastMouseY, true);
        if (!hovered.getStack().isEmpty()) {
            return hovered.getStack();
        }

        if (mc.screen instanceof RecipeScreen recipeScreen) {
            EmiIngredient recipeHovered = recipeScreen.getHoveredStack();
            if (!recipeHovered.isEmpty()) {
                return recipeHovered;
            }
        }

        return EmiStack.EMPTY;
    }

    @Unique
    private static boolean cosmiccore$wouldMatchFavoriteKey(int keyCode, int scanCode) {
        for (var boundKey : EmiConfig.favorite.boundKeys) {
            if (boundKey.key().getValue() == keyCode) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private static void cosmiccore$renderGroupArrows(EmiDrawContext context, int mouseX, int mouseY, float delta,
                                                     CallbackInfo ci) {
        // Safety check - don't run during EMI reload when screen might be null
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.screen == null) {
            cosmiccore$arrowsVisible = false;
            return;
        }

        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        EmiScreenManager.SidebarPanel favPanel = getPanelFor(SidebarType.FAVORITES);
        if (favPanel == null) {
            cosmiccore$arrowsVisible = false;
            return;
        }

        var bounds = favPanel.getBounds();
        if (bounds.empty()) {
            cosmiccore$arrowsVisible = false;
            return;
        }

        int arrowY = bounds.y() - 18;
        int leftArrowX = bounds.x();
        int rightArrowX = bounds.x() + bounds.width() - ARROW_SIZE;

        cosmiccore$groupArrowLeftX = leftArrowX;
        cosmiccore$groupArrowLeftY = arrowY;
        cosmiccore$groupArrowRightX = rightArrowX;
        cosmiccore$groupArrowRightY = arrowY;
        cosmiccore$arrowsVisible = true;

        boolean hoverLeft = mouseX >= leftArrowX && mouseX < leftArrowX + ARROW_SIZE &&
                mouseY >= arrowY && mouseY < arrowY + ARROW_SIZE;
        int leftV = hoverLeft ? ARROW_V_HOVER : ARROW_V_NORMAL;
        context.drawTexture(EmiRenderHelper.BUTTONS, leftArrowX, arrowY, ARROW_LEFT_U, leftV, ARROW_SIZE, ARROW_SIZE);

        boolean hoverRight = mouseX >= rightArrowX && mouseX < rightArrowX + ARROW_SIZE &&
                mouseY >= arrowY && mouseY < arrowY + ARROW_SIZE;
        int rightV = hoverRight ? ARROW_V_HOVER : ARROW_V_NORMAL;
        context.drawTexture(EmiRenderHelper.BUTTONS, rightArrowX, arrowY, ARROW_RIGHT_U, rightV,
                ARROW_SIZE, ARROW_SIZE);

        // Draw + button to the right of the right arrow
        int plusX = rightArrowX + ARROW_SIZE + 2;
        int plusY = arrowY + (ARROW_SIZE - PLUS_SIZE) / 2;
        cosmiccore$plusButtonX = plusX;
        cosmiccore$plusButtonY = plusY;

        boolean hoverPlus = mouseX >= plusX && mouseX < plusX + PLUS_SIZE &&
                mouseY >= plusY && mouseY < plusY + PLUS_SIZE;
        int plusColor = hoverPlus ? 0xFFFFFF00 : 0xFFFFFFFF;

        // Draw the plus from EMI's widgets texture
        context.push();
        if (hoverPlus) {
            context.matrices().translate(0, 0, 100);
        }
        context.drawTexture(EmiRenderHelper.WIDGETS, plusX, plusY, PLUS_U, PLUS_V, PLUS_SIZE, PLUS_SIZE);
        context.pop();

        String groupName = manager.getActiveGroup().getName();
        int current = manager.getActiveGroupIndex() + 1;
        int total = manager.getGroupCount();

        String modeIndicator = manager.getActiveViewMode() == CosmicBookmarkGroup.ViewMode.TODO_LIST ? " [L]" : "";
        String text = groupName + " (" + current + "/" + total + ")" + modeIndicator;

        int textWidth = mc.font.width(text);
        int textX = bounds.x() + (bounds.width() - textWidth) / 2;
        int textY = arrowY + 4;

        cosmiccore$groupNameX = textX;
        cosmiccore$groupNameY = textY;
        cosmiccore$groupNameWidth = textWidth;
        cosmiccore$groupNameHeight = mc.font.lineHeight;

        boolean hoverName = mouseX >= textX && mouseX < textX + textWidth &&
                mouseY >= textY && mouseY < textY + mc.font.lineHeight;
        int textColor = hoverName ? 0xFFFFFF00 : 0xFFFFFFFF;

        context.drawTextWithShadow(Component.literal(text), textX, textY, textColor);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$onMouseClicked(double mouseX, double mouseY, int button,
                                                  CallbackInfoReturnable<Boolean> cir) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (cosmiccore$handleTodoListClick(mx, my, button, cir)) {
            return;
        }

        if (!cosmiccore$arrowsVisible) {
            return;
        }

        if (mx >= cosmiccore$groupNameX && mx < cosmiccore$groupNameX + cosmiccore$groupNameWidth &&
                my >= cosmiccore$groupNameY && my < cosmiccore$groupNameY + cosmiccore$groupNameHeight) {
            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

            if (button == 0) {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreen(new GroupRenameScreen(
                        mc.screen,
                        manager.getActiveGroupIndex(),
                        manager.getActiveGroup().getName()));
            } else if (button == 1) {
                manager.toggleViewMode();
                repopulatePanels(SidebarType.FAVORITES);

                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    String modeName = manager.getActiveViewMode() == CosmicBookmarkGroup.ViewMode.TODO_LIST ?
                            "TODO List" : "Grid";
                    mc.player.displayClientMessage(
                            Component.literal("§6[Bookmarks]§r View mode: §e" + modeName),
                            true);
                }
            }
            cir.setReturnValue(true);
            return;
        }

        if (button != 0) {
            return;
        }

        if (mx >= cosmiccore$groupArrowLeftX && mx < cosmiccore$groupArrowLeftX + 16 &&
                my >= cosmiccore$groupArrowLeftY && my < cosmiccore$groupArrowLeftY + 16) {
            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
            manager.previousGroup();
            cosmiccore$showGroupChangeMessage(manager);
            cir.setReturnValue(true);
            return;
        }

        if (mx >= cosmiccore$groupArrowRightX && mx < cosmiccore$groupArrowRightX + 16 &&
                my >= cosmiccore$groupArrowRightY && my < cosmiccore$groupArrowRightY + 16) {
            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
            manager.nextGroup();
            cosmiccore$showGroupChangeMessage(manager);
            cir.setReturnValue(true);
            return;
        }

        // Handle + button click to create new group
        if (mx >= cosmiccore$plusButtonX && mx < cosmiccore$plusButtonX + PLUS_SIZE &&
                my >= cosmiccore$plusButtonY && my < cosmiccore$plusButtonY + PLUS_SIZE) {
            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
            int newGroupNum = manager.getGroupCount() + 1;
            CosmicBookmarkGroup newGroup = manager.createGroup("Group " + newGroupNum);
            manager.setActiveGroup(manager.getGroupCount() - 1);

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.literal("§6[Bookmarks]§r Created new group: §e" + newGroup.getName()),
                        true);
            }

            repopulatePanels(SidebarType.FAVORITES);
            cir.setReturnValue(true);
            return;
        }
    }

    @Unique
    private static void cosmiccore$showGroupChangeMessage(CosmicBookmarkManager manager) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            String groupName = manager.getActiveGroup().getName();
            int current = manager.getActiveGroupIndex() + 1;
            int total = manager.getGroupCount();
            mc.player.displayClientMessage(
                    Component.literal("§6[Bookmarks]§r " + groupName + " §7(" + current + "/" + total + ")"),
                    true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$onMouseScrolled(double mouseX, double mouseY, double amount,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (!EmiInput.isControlDown()) {
            return;
        }

        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        if (manager.getActiveViewMode() == CosmicBookmarkGroup.ViewMode.TODO_LIST) {
            cosmiccore$handleTodoListScroll((int) mouseX, (int) mouseY, amount, cir);
            return;
        }

        EmiStackInteraction hovered = getHoveredStack((int) mouseX, (int) mouseY, true);
        if (hovered.getStack().isEmpty()) {
            return;
        }

        EmiIngredient hoveredStack = hovered.getStack();
        if (!(hoveredStack instanceof EmiFavorite)) {
            return;
        }

        int favoriteIndex = manager.findFavoriteIndex(hoveredStack);

        if (favoriteIndex < 0) {
            return;
        }

        cosmiccore$scrollAccumulator += amount;
        int scrollDelta = (int) cosmiccore$scrollAccumulator;
        cosmiccore$scrollAccumulator -= scrollDelta;

        if (scrollDelta == 0) {
            cir.setReturnValue(true);
            return;
        }

        long step = 1;
        if (EmiInput.isShiftDown()) {
            step = cosmiccore$getDefaultStep(hoveredStack);
        }

        long delta = scrollDelta * step;
        manager.adjustFavoriteAmount(favoriteIndex, delta);

        repopulatePanels(SidebarType.FAVORITES);

        cir.setReturnValue(true);
    }

    @Unique
    private static boolean cosmiccore$handleTodoListScroll(int mx, int my, double amount,
                                                           CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        List<CosmicRecipeBookmark> recipes = manager.getActiveRecipeBookmarks();

        if (recipes.isEmpty()) {
            return false;
        }

        int todoListX = TodoListBoundsHelper.getX();
        int todoListY = TodoListBoundsHelper.getY();
        int todoListWidth = TodoListBoundsHelper.getWidth();
        int todoListHeight = TodoListBoundsHelper.getHeight();
        int slotSize = TodoListBoundsHelper.getSlotSize();

        if (mx < todoListX || mx >= todoListX + todoListWidth ||
                my < todoListY || my >= todoListY + todoListHeight) {
            return false;
        }

        int row = (my - todoListY) / slotSize;
        if (row < 0 || row >= recipes.size()) {
            return false;
        }

        cosmiccore$scrollAccumulator += amount;
        int scrollDelta = (int) cosmiccore$scrollAccumulator;
        cosmiccore$scrollAccumulator -= scrollDelta;

        if (scrollDelta == 0) {
            cir.setReturnValue(true);
            return true;
        }

        long step = EmiInput.isShiftDown() ? 10 : 1;
        long delta = scrollDelta * step;
        CosmicRecipeBookmark recipe = recipes.get(row);
        recipe.adjustMultiplier(delta);
        manager.save();

        repopulatePanels(SidebarType.FAVORITES);

        cir.setReturnValue(true);
        return true;
    }

    @Unique
    private static long cosmiccore$getDefaultStep(EmiIngredient ingredient) {
        if (!ingredient.getEmiStacks().isEmpty()) {
            EmiStack first = ingredient.getEmiStacks().get(0);
            if (first.getKey() instanceof net.minecraft.world.level.material.Fluid) {
                return 1000;
            }
        }
        return 64;
    }

    @Unique
    private static int cosmiccore$getTodoListHoveredRecipeIndex(int mx, int my) {
        int todoListX = TodoListBoundsHelper.getX();
        int todoListY = TodoListBoundsHelper.getY();
        int todoListWidth = TodoListBoundsHelper.getWidth();
        int todoListHeight = TodoListBoundsHelper.getHeight();
        int slotSize = TodoListBoundsHelper.getSlotSize();

        if (mx < todoListX || mx >= todoListX + todoListWidth ||
                my < todoListY || my >= todoListY + todoListHeight) {
            return -1;
        }

        return (my - todoListY) / slotSize;
    }

    @Unique
    private static boolean cosmiccore$handleTodoListClick(int mx, int my, int button,
                                                          CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        if (manager.getActiveViewMode() != CosmicBookmarkGroup.ViewMode.TODO_LIST) {
            return false;
        }

        EmiScreenManager.SidebarPanel favPanel = getPanelFor(SidebarType.FAVORITES);
        if (favPanel == null) {
            return false;
        }

        var bounds = favPanel.getBounds();
        if (bounds.empty()) {
            return false;
        }

        if (mx < bounds.x() || mx >= bounds.x() + bounds.width() ||
                my < bounds.y() || my >= bounds.y() + bounds.height()) {
            return false;
        }

        List<CosmicRecipeBookmark> recipes = manager.getActiveRecipeBookmarks();

        int todoListX = TodoListBoundsHelper.getX();
        int todoListY = TodoListBoundsHelper.getY();
        int todoListCols = TodoListBoundsHelper.getCols();
        int slotSize = TodoListBoundsHelper.getSlotSize();

        int row = -1;
        int col = -1;
        if (todoListX > 0 && todoListY > 0 && slotSize > 0) {
            row = (my - todoListY) / slotSize;
            col = (mx - todoListX) / slotSize;
        }

        if (row >= 0 && row < recipes.size() && col >= 0 && col < todoListCols) {
            CosmicRecipeBookmark recipe = recipes.get(row);

            if (button == 0) {
                if (col == 0) {
                    EmiRecipe emiRecipe = recipe.getRecipe();
                    if (emiRecipe != null) {
                        EmiApi.displayRecipe(emiRecipe);
                    } else {
                        EmiApi.displayRecipes(recipe.getOutput());
                    }
                } else {
                    int inputIndex = col - 1;
                    List<EmiIngredient> inputs = recipe.getInputs();
                    if (inputIndex >= 0 && inputIndex < inputs.size()) {
                        EmiIngredient input = inputs.get(inputIndex);
                        if (!input.isEmpty()) {
                            EmiApi.displayRecipes(input);
                        }
                    }
                }
            } else if (button == 1) {
                if (col == 0) {
                    EmiApi.displayUses(recipe.getOutput());
                } else {
                    int inputIndex = col - 1;
                    List<EmiIngredient> inputs = recipe.getInputs();
                    if (inputIndex >= 0 && inputIndex < inputs.size()) {
                        EmiIngredient input = inputs.get(inputIndex);
                        if (!input.isEmpty()) {
                            EmiApi.displayUses(input);
                        }
                    }
                }
            } else if (button == 2) {
                manager.removeRecipeBookmark(row);
                repopulatePanels(SidebarType.FAVORITES);
            }
        }

        cir.setReturnValue(true);
        return true;
    }
}
