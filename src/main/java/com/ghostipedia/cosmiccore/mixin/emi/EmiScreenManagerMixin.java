package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.RecipeScreenAccessor;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkGroup;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkHeaderLayout;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkUiState;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mixin(value = EmiScreenManager.class, remap = false)
public abstract class EmiScreenManagerMixin {

    @Shadow
    private static int lastMouseX;

    @Shadow
    private static int lastMouseY;

    @Shadow
    private static List<SidebarPanel> panels;

    @Shadow
    private static EmiIngredient pressedStack;

    @Shadow
    private static EmiIngredient draggedStack;

    @Shadow
    public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean notClick) {
        throw new AssertionError();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$favoritesButton(double mouseX, double mouseY, int button,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (button != 0 || EmiScreenManager.isDisabled()) return;
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady()) return;
        for (SidebarPanel panel : panels) {
            if (panel.getType() != SidebarType.FAVORITES || !panel.isVisible()) continue;
            CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout.create(panel);
            if (layout == null || !layout.cycle().contains((int) mouseX, (int) mouseY)) continue;
            if (EmiInput.isControlDown() || EmiInput.isShiftDown()) {
                panel.cycleType(EmiInput.isShiftDown() ? -1 : 1);
            } else {
                CosmicBookmarkUiState.toggleAlert();
            }
            cosmiccore$playClick();
            cir.setReturnValue(true);
            return;
        }
    }

    @Inject(
            method = "mouseClicked",
            at = @At(
                     value = "INVOKE",
                     target = "Ldev/emi/emi/screen/EmiScreenManager;isDisabled()Z",
                     ordinal = 0),
            cancellable = true,
            require = 1)
    private static void cosmiccore$groupMouseControls(double mouseX, double mouseY, int button,
                                                      CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady() || EmiScreenManager.isDisabled()) return;
        if (button != 0) return;
        for (SidebarPanel panel : panels) {
            if (panel.getType() != SidebarType.FAVORITES || !panel.isVisible()) continue;
            CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout.create(panel);
            if (layout == null) continue;
            if (layout.groupPrevious().contains((int) mouseX, (int) mouseY)) {
                if (manager.getGroupCount() > 1) {
                    manager.prevGroup();
                    cosmiccore$resetFavoritePages();
                    cosmiccore$playClick();
                }
                cir.setReturnValue(true);
                return;
            }
            if (layout.groupNext().contains((int) mouseX, (int) mouseY)) {
                if (manager.getGroupCount() > 1) {
                    manager.nextGroup();
                    cosmiccore$resetFavoritePages();
                    cosmiccore$playClick();
                }
                cir.setReturnValue(true);
                return;
            }
            if (layout.pagePrevious().contains((int) mouseX, (int) mouseY)) {
                if (panel.hasMultiplePages()) {
                    panel.scroll(-1);
                    cosmiccore$playClick();
                }
                cir.setReturnValue(true);
                return;
            }
            if (layout.pageNext().contains((int) mouseX, (int) mouseY)) {
                if (panel.hasMultiplePages()) {
                    panel.scroll(1);
                    cosmiccore$playClick();
                }
                cir.setReturnValue(true);
                return;
            }
            if (layout.groupAction().contains((int) mouseX, (int) mouseY)) {
                boolean forceDelete = CosmicBookmarkUiState.isForceDeleteModifierDown();
                boolean deleteEmpty = manager.getGroupCount() > 1 && manager.getActiveGroup().size() == 0;
                if (forceDelete) {
                    manager.forceRemoveGroup(manager.getActiveIndex());
                } else if (deleteEmpty) {
                    manager.removeGroup(manager.getActiveIndex());
                } else {
                    cosmiccore$addGroup(manager, EmiInput.isShiftDown() ?
                            CosmicBookmarkGroup.GroupType.RECIPE : CosmicBookmarkGroup.GroupType.REGULAR);
                }
                cosmiccore$resetFavoritePages();
                cosmiccore$playClick();
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "drawForeground", at = @At("TAIL"))
    private static void cosmiccore$drawBookmarkHelp(EmiDrawContext context, int mouseX, int mouseY, float delta,
                                                    CallbackInfo ci) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        var screen = Minecraft.getInstance().screen;
        if (!manager.isReady() || EmiScreenManager.isDisabled() || screen == null) return;

        for (SidebarPanel panel : panels) {
            if (panel.getType() != SidebarType.FAVORITES || !panel.isVisible()) continue;
            CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout.create(panel);
            if (layout == null) continue;
            if (layout.cycle().contains(mouseX, mouseY)) {
                cosmiccore$drawTooltip(
                        context,
                        cosmiccore$helpLines(),
                        mouseX,
                        mouseY,
                        Math.min(300, Math.max(80, screen.width / 2 - 16)));
                return;
            }
            if (layout.groupPrevious().contains(mouseX, mouseY)) {
                cosmiccore$drawTooltip(context, List.of(Component.translatable(
                        "cosmiccore.emi.bookmarks.action.previous_group")), mouseX, mouseY, 180);
                return;
            }
            if (layout.groupNext().contains(mouseX, mouseY)) {
                cosmiccore$drawTooltip(context, List.of(Component.translatable(
                        "cosmiccore.emi.bookmarks.action.next_group")), mouseX, mouseY, 180);
                return;
            }
            if (layout.pagePrevious().contains(mouseX, mouseY)) {
                cosmiccore$drawTooltip(context, List.of(Component.translatable(
                        "cosmiccore.emi.bookmarks.action.previous_page")), mouseX, mouseY, 180);
                return;
            }
            if (layout.pageNext().contains(mouseX, mouseY)) {
                cosmiccore$drawTooltip(context, List.of(Component.translatable(
                        "cosmiccore.emi.bookmarks.action.next_page")), mouseX, mouseY, 180);
                return;
            }
            if (layout.groupAction().contains(mouseX, mouseY)) {
                boolean forceDelete = CosmicBookmarkUiState.isForceDeleteModifierDown();
                boolean deleteEmpty = manager.getGroupCount() > 1 && manager.getActiveGroup().size() == 0;
                List<Component> lines = forceDelete ?
                        List.of(Component.translatable("cosmiccore.emi.bookmarks.action.force_delete")
                                .withStyle(ChatFormatting.RED)) :
                        deleteEmpty ?
                                List.of(Component.translatable("cosmiccore.emi.bookmarks.action.delete")
                                        .withStyle(ChatFormatting.RED)) :
                                List.of(
                                        Component.translatable("cosmiccore.emi.bookmarks.action.create_regular")
                                                .withStyle(ChatFormatting.GREEN),
                                        Component.translatable("cosmiccore.emi.bookmarks.action.create_recipe")
                                                .withStyle(ChatFormatting.GRAY),
                                        Component.translatable("cosmiccore.emi.bookmarks.action.force_delete")
                                                .withStyle(ChatFormatting.RED));
                cosmiccore$drawTooltip(context, lines, mouseX, mouseY, 240);
                return;
            }
        }
    }

    @Inject(
            method = "keyPressed",
            at = @At(
                     value = "INVOKE",
                     target = "Ldev/emi/emi/api/EmiApi;isCheatMode()Z",
                     ordinal = 0),
            cancellable = true,
            require = 1)
    private static void cosmiccore$bookmarkKeys(int keyCode, int scanCode, int modifiers,
                                                CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady()) return;
        int mode = cosmiccore$favoriteKeyChordMode(keyCode, scanCode);
        if (mode == 0) return;
        EmiStackInteraction hovered = getHoveredStack(lastMouseX, lastMouseY, true);
        EmiIngredient ingredient = hovered.getStack();
        EmiRecipe context = hovered.getRecipeContext();
        if (ingredient.isEmpty() && Minecraft.getInstance().screen instanceof RecipeScreenAccessor recipeScreen) {
            ingredient = recipeScreen.getHoveredStack(lastMouseX, lastMouseY);
            context = recipeScreen.getHoveredRecipe(lastMouseX, lastMouseY);
        }
        if (cosmiccore$applyPinChord(mode, ingredient, context)) cir.setReturnValue(true);
    }

    @Inject(method = "stackInteraction", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$removeProjectedEntry(EmiStackInteraction stack,
                                                        Function<EmiBind, Boolean> function,
                                                        CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady() || !manager.getActiveGroup().isRecipeGroup() ||
                !function.apply(EmiConfig.favorite)) {
            return;
        }
        String entryId = manager.getProjectedEntryId(stack.getStack());
        if (entryId == null) return;
        manager.removeEntry(entryId);
        cir.setReturnValue(true);
    }

    @WrapOperation(
                   method = "mouseScrolled",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/emi/emi/screen/EmiScreenManager$SidebarPanel;scroll(I)V",
                            ordinal = 0),
                   require = 1)
    private static void cosmiccore$adjustProjectedAmount(SidebarPanel panel, int direction, Operation<Void> original,
                                                         double mouseX, double mouseY, double amount) {
        if (!EmiInput.isControlDown() || direction == 0) {
            original.call(panel, direction);
            return;
        }
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        EmiScreenManager.ScreenSpace space = panel.getHoveredSpace((int) mouseX, (int) mouseY);
        if (!manager.isReady() || space == null || space.getType() != SidebarType.FAVORITES) {
            original.call(panel, direction);
            return;
        }
        EmiIngredient ingredient = getHoveredStack((int) mouseX, (int) mouseY, true).getStack();
        String entryId = manager.getProjectedEntryId(ingredient);
        if (entryId == null) {
            original.call(panel, direction);
            return;
        }
        long step;
        if (manager.isRecipeEntry(entryId)) {
            step = EmiInput.isShiftDown() ? 10 : 1;
        } else {
            boolean fluid = cosmiccore$isFluid(ingredient);
            if (EmiInput.isShiftDown()) {
                step = fluid ? 1000 : 64;
            } else if (fluid && EmiInput.isAltDown()) {
                step = 10;
            } else {
                step = 1;
            }
        }
        manager.adjustEntry(entryId, direction < 0 ? step : -step);
    }

    @WrapOperation(
                   method = "mouseReleased",
                   at = @At(
                            value = "INVOKE",
                            target = "Ldev/emi/emi/screen/EmiScreenManager;stackInteraction(Ldev/emi/emi/api/stack/EmiStackInteraction;Ljava/util/function/Function;)Z",
                            ordinal = 0),
                   require = 1)
    private static boolean cosmiccore$mousePinChord(EmiStackInteraction stack, Function<EmiBind, Boolean> function,
                                                    Operation<Boolean> original, double mouseX, double mouseY,
                                                    int button) {
        int mode = cosmiccore$favoriteMouseChordMode(button);
        if (mode == 0) return original.call(stack, function);
        if (cosmiccore$applyPinChord(mode, stack.getStack(), stack.getRecipeContext())) return true;
        return true;
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private static void cosmiccore$rejectRecipeCellDrag(double mouseX, double mouseY, int button,
                                                        double deltaX, double deltaY,
                                                        CallbackInfoReturnable<Boolean> cir) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (button == 0 && manager.isReady() && manager.getActiveGroup().isRecipeGroup() &&
                manager.getProjectedEntryId(pressedStack) != null) {
            pressedStack = EmiStack.EMPTY;
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static void cosmiccore$addGroup(CosmicBookmarkManager manager, CosmicBookmarkGroup.GroupType type) {
        String prefix = type == CosmicBookmarkGroup.GroupType.RECIPE ? "Recipe " : "Group ";
        manager.addGroup(prefix + (manager.getGroupCount() + 1), type);
    }

    @Unique
    private static List<Component> cosmiccore$helpLines() {
        Component favorite = EmiConfig.favorite.getBindText();
        List<Component> lines = new ArrayList<>();
        lines.add(SidebarType.FAVORITES.getText());
        lines.add(SidebarType.FAVORITES.getDescription());
        if (EmiConfig.favorite.isBound()) {
            lines.add(EmiPort.translatable("emi.sidebar.favorite_stack", favorite).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("cosmiccore.emi.bookmarks.help.title").withStyle(ChatFormatting.YELLOW));
        lines.add(
                Component.translatable("cosmiccore.emi.bookmarks.help.exact", favorite)
                        .withStyle(ChatFormatting.GRAY));
        lines.add(
                Component.translatable("cosmiccore.emi.bookmarks.help.recipe", favorite)
                        .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("cosmiccore.emi.bookmarks.help.adjust").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("cosmiccore.emi.bookmarks.help.adjust_fluid")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("cosmiccore.emi.bookmarks.help.adjust_fast")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(CosmicBookmarkUiState.isAlertVisible() ?
                "cosmiccore.emi.bookmarks.help.dismiss" : "cosmiccore.emi.bookmarks.help.restore")
                .withStyle(ChatFormatting.GOLD));
        return lines;
    }

    @Unique
    private static void cosmiccore$drawTooltip(EmiDrawContext context, List<Component> lines, int x, int y,
                                               int maxWidth) {
        var screen = Minecraft.getInstance().screen;
        if (screen == null) return;
        List<ClientTooltipComponent> components = lines.stream()
                .map(EmiPort::ordered)
                .map(ClientTooltipComponent::create)
                .toList();
        EmiRenderHelper.drawTooltip(screen, context, components, x, y, maxWidth);
    }

    @Unique
    private static void cosmiccore$playClick() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
    }

    @Unique
    private static int cosmiccore$favoriteKeyChordMode(int keyCode, int scanCode) {
        for (var boundKey : EmiConfig.favorite.boundKeys) {
            InputConstants.Key key = boundKey.key();
            boolean physicalMatch = false;
            if (keyCode == InputConstants.UNKNOWN.getValue()) {
                physicalMatch = key.getType() == InputConstants.Type.SCANCODE && key.getValue() == scanCode;
            } else if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() == keyCode) {
                physicalMatch = true;
            }
            if (physicalMatch) return cosmiccore$favoriteChordMode(boundKey.modifiersToMatch());
        }
        return 0;
    }

    @Unique
    private static int cosmiccore$favoriteMouseChordMode(int button) {
        for (var boundKey : EmiConfig.favorite.boundKeys) {
            InputConstants.Key key = boundKey.key();
            if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == button) {
                return cosmiccore$favoriteChordMode(boundKey.modifiersToMatch());
            }
        }
        return 0;
    }

    @Unique
    private static int cosmiccore$favoriteChordMode(int baseModifiers) {
        int current = EmiInput.getCurrentModifiers();
        int exact = baseModifiers | EmiInput.CONTROL_MASK;
        if (current == exact) return 1;
        if ((baseModifiers & EmiInput.SHIFT_MASK) == 0 && current == (exact | EmiInput.SHIFT_MASK)) return 2;
        return 0;
    }

    @Unique
    private static boolean cosmiccore$applyPinChord(int mode, EmiIngredient ingredient, EmiRecipe context) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (!manager.isReady()) return false;
        if (mode == 2) {
            EmiRecipe recipe = context;
            if (recipe == null && Minecraft.getInstance().screen instanceof RecipeScreenAccessor recipeScreen) {
                recipe = recipeScreen.getHoveredRecipe(lastMouseX, lastMouseY);
            }
            if (recipe == null) return false;
            manager.toggleRecipe(recipe);
        } else {
            if (ingredient.isEmpty()) return false;
            manager.toggleStack(ingredient, Math.max(1, ingredient.getAmount()), null);
        }
        cosmiccore$resetFavoritePages();
        return true;
    }

    @Unique
    private static boolean cosmiccore$isFluid(EmiIngredient ingredient) {
        return !ingredient.getEmiStacks().isEmpty() && ingredient.getEmiStacks().get(0).getKey() instanceof Fluid;
    }

    @Unique
    private static void cosmiccore$resetFavoritePages() {
        for (SidebarPanel panel : panels) {
            if (panel.getType() == SidebarType.FAVORITES) panel.page = 0;
        }
    }
}
