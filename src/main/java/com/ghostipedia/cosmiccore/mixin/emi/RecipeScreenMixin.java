package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.RecipeScreenAccessor;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.RecipeTab;
import dev.emi.emi.screen.WidgetGroup;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

    /**
     * @author CosmicCore
     * @reason Multi-column workstation layout for categories with many workstations
     */
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

        if (EmiConfig.workstationLocation == SidebarSide.LEFT) {
            int slotX = isBackground ? x - 18 : x - 18 * (column + 1);
            return new Bounds(slotX, y + 9 + getResolveOffset() + row * 18 + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.RIGHT) {
            int slotX = isBackground ? x + backgroundWidth : x + backgroundWidth + 18 * column;
            return new Bounds(slotX, y + 9 + getResolveOffset() + row * 18 + offset, 18, 18);
        } else if (EmiConfig.workstationLocation == SidebarSide.BOTTOM) {
            return new Bounds(x + 5 + getResolveOffset() + i * 18 + offset, y + backgroundHeight - 23, 18, 18);
        }
        return Bounds.EMPTY;
    }

    /**
     * @author CosmicCore
     * @reason Increase max workstations for multi-column layout
     */
    @Overwrite(remap = false)
    public int getMaxWorkstations() {
        return 23;
    }

    @Unique
    private int cosmicCore$getWorkstationAmount() {
        int total = EmiApi.getRecipeManager().getWorkstations(tabs.get(tab).category).size();
        return Math.min(total, getMaxWorkstations());
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
        return (workstations + maxPerColumn - 1) / maxPerColumn;
    }

    @Redirect(
              method = { "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                      "m_88315_(Lnet/minecraft/client/gui/GuiGraphics;IIF)V" },
              at = @At(value = "INVOKE",
                       target = "Ldev/emi/emi/EmiRenderHelper;drawNinePatch(Ldev/emi/emi/runtime/EmiDrawContext;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"),
              remap = false,
              require = 0)
    private void cosmicCore$widenWorkstationBackground(EmiDrawContext context, ResourceLocation texture, int x, int y,
                                                       int width, int height, int u, int v, int cornerSize,
                                                       int centerSize) {
        if (v == 0 && (u == 36 || u == 47 || u == 58)) {
            int columns = cosmicCore$getColumnCount();
            if (columns > 1) {
                int maxPerColumn = cosmicCore$maxWorkstations();
                int workstations = cosmicCore$getWorkstationAmount();
                int resolveOffset = (workstations > 0) ? 0 : getResolveOffset();
                int correctHeight = 10 + 18 * maxPerColumn + resolveOffset;

                if (u == 36) {
                    x -= 18 * (columns - 1);
                    width += 18 * (columns - 1);
                    height = correctHeight;
                } else if (u == 47) {
                    width += 18 * (columns - 1);
                    height = correctHeight;
                }
            }
        }
        EmiRenderHelper.drawNinePatch(context, texture, x, y, width, height, u, v, cornerSize, centerSize);
    }

    @Unique
    public EmiIngredient getHoveredStack(int mouseX, int mouseY) {
        if (currentPage == null) return EmiStack.EMPTY;

        for (WidgetGroup group : currentPage) {
            int localX = mouseX - group.x;
            int localY = mouseY - group.y;
            for (Widget widget : group.widgets) {
                if (widget instanceof SlotWidget slot) {
                    Bounds bounds = slot.getBounds();
                    if (localX >= bounds.x() && localX < bounds.x() + bounds.width() &&
                            localY >= bounds.y() && localY < bounds.y() + bounds.height()) {
                        return slot.getStack();
                    }
                }
            }
        }
        return EmiStack.EMPTY;
    }

    @Unique
    @Nullable
    public EmiRecipe getHoveredRecipe(int mouseX, int mouseY) {
        if (currentPage == null) return null;

        for (WidgetGroup group : currentPage) {
            if (group.recipe == null) continue;
            if (mouseX >= group.x && mouseX < group.x + group.width &&
                    mouseY >= group.y && mouseY < group.y + group.height) {
                return group.recipe;
            }
        }
        return null;
    }
}
