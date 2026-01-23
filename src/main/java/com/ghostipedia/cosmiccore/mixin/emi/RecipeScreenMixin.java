package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.RecipeScreenAccessor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(value = RecipeScreen.class, remap = false)
public abstract class RecipeScreenMixin extends Screen implements RecipeScreenAccessor {

    @Shadow(remap = false)
    int x;

    @Shadow(remap = false)
    int backgroundHeight;

    @Shadow(remap = false)
    int backgroundWidth;

    protected RecipeScreenMixin(Component title) {
        super(title);
    }

    @Shadow(remap = false)
    public abstract int getResolveOffset();

    @Shadow(remap = false)
    int y;

    @Shadow(remap = false)
    private List<RecipeTab> tabs;

    @Shadow(remap = false)
    private int tab;

    @Shadow(remap = false)
    private List<WidgetGroup> currentPage;

    @ModifyArg(method = "m_88315_",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 2,
               remap = false,
               require = 0)
    private int modifyX(int originalX) {
        if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
            int columns = cosmicCore$getColumnCount();
            return x - 5 - 18 * columns;
        }
        return originalX;
    }

    @ModifyArg(method = "m_88315_",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 4,
               remap = false,
               require = 0)
    private int modifyw(int originalWidth) {
        return 10 + 18 * cosmicCore$getColumnCount();
    }

    @ModifyArg(method = "m_88315_",
               at = @At(value = "INVOKE",
                        target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V",
                        ordinal = 4,
                        remap = false),
               index = 5,
               remap = false,
               require = 0)
    private int modifyh(int originalHeight) {
        int workstations = cosmicCore$getWorkstationAmount();
        int maxPerColumn = cosmicCore$maxWorkstations();
        int rows = Math.min(workstations, maxPerColumn);
        return 10 + rows * 18 + getResolveOffset();
    }

    @Overwrite(remap = false)
    public Bounds getWorkstationBounds(int i) {
        int offset = 0;
        boolean isBackground = (i == -1);
        if (isBackground) {
            i = 0;
            offset = -getResolveOffset();
        }

        int maxPerColumn = cosmicCore$maxWorkstations();
        int column = maxPerColumn > 0 ? i / maxPerColumn : 0;
        int row = maxPerColumn > 0 ? i % maxPerColumn : i;
        int columns = cosmicCore$getColumnCount();

        if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
            int slotX;
            if (isBackground) {
                slotX = x - 18 * columns;
            } else {
                slotX = x - 18 * (column + 1);
            }
            return new Bounds(slotX, y + 9 + getResolveOffset() + row * 18 + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
            int slotX;
            if (isBackground) {
                slotX = x + backgroundWidth;
            } else {
                slotX = x + backgroundWidth + 18 * column;
            }
            return new Bounds(slotX, y + 9 + getResolveOffset() + row * 18 + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
            return new Bounds(x + 5 + getResolveOffset() + i * 18 + offset, y + backgroundHeight - 23, 18, 18);
        }
        return Bounds.EMPTY;
    }

    @Overwrite(remap = false)
    public int getMaxWorkstations() {
        return 23;
    }

    @Unique
    private int cosmicCore$getWorkstationAmount() {
        return EmiApi.getRecipeManager().getWorkstations(tabs.get(tab).category).size();
    }

    @Unique
    private int cosmicCore$maxWorkstations() {
        return switch (EmiConfig.workstationLocation) {
            case LEFT, RIGHT -> (backgroundHeight - getResolveOffset() - 18) / 18;
            case BOTTOM -> (backgroundWidth - getResolveOffset() - 18) / 18;
            default -> 0;
        };
    }

    @Unique
    private int cosmicCore$getColumnCount() {
        int workstations = cosmicCore$getWorkstationAmount();
        int maxPerColumn = cosmicCore$maxWorkstations();
        if (maxPerColumn <= 0) return 1;
        return (workstations + maxPerColumn - 1) / maxPerColumn; // ceil division
    }

    /**
     * Gets the hovered stack from the recipe screen for CTRL+A pinning.
     * Iterates through current page widgets to find SlotWidgets under the mouse.
     */
    @Unique
    public EmiIngredient getHoveredStack(int mouseX, int mouseY) {
        if (currentPage == null) return EmiStack.EMPTY;

        for (WidgetGroup group : currentPage) {
            for (Widget widget : group.widgets) {
                if (widget instanceof SlotWidget slot) {
                    Bounds bounds = slot.getBounds();
                    if (mouseX >= bounds.x() && mouseX < bounds.x() + bounds.width() &&
                            mouseY >= bounds.y() && mouseY < bounds.y() + bounds.height()) {
                        return slot.getStack();
                    }
                }
            }
        }
        return EmiStack.EMPTY;
    }

    /**
     * Gets the recipe containing the currently hovered stack for CTRL+SHIFT+A pinning.
     * Checks if mouse is within the widget group's bounds, not individual slots.
     */
    @Unique
    @Nullable
    public EmiRecipe getHoveredRecipe(int mouseX, int mouseY) {
        if (currentPage == null) return null;

        EmiRecipe fallback = null;

        for (WidgetGroup group : currentPage) {
            if (group.recipe == null) continue;

            if (fallback == null) {
                fallback = group.recipe;
            }

            // Check if mouse is within the group's bounds (x, y, width, height are public fields)
            if (mouseX >= group.x && mouseX < group.x + group.width &&
                    mouseY >= group.y && mouseY < group.y + group.height) {
                return group.recipe;
            }
        }

        // Fall back to first recipe on page if none matched
        return fallback;
    }
}
