package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Modifies the favorites sidebar header to show "Group X/Y | Page A/B"
 * and enables page arrows when multiple groups/recipe pages exist.
 */
@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class SidebarPanelMixin {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Inject(method = "hasMultiplePages", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$enableArrowsForGroups(CallbackInfoReturnable<Boolean> cir) {
        if (getType() == SidebarType.FAVORITES && !cir.getReturnValue()) {
            CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
            if (manager.getGroupCount() > 1) {
                cir.setReturnValue(true);
            } else if (manager.getActiveGroup().isRecipeGroup() && space != null) {
                int pageCount = manager.getRecipePageCount(space.tw, space.th);
                if (pageCount > 1) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Unique
    private int cosmiccore$recipePageCount = 1;

    @Unique
    private int cosmiccore$currentRecipePage = 0;

    @Redirect(
              method = "drawHeader",
              at = @At(
                       value = "INVOKE",
                       target = "Ldev/emi/emi/EmiRenderHelper;getPageText(III)Lnet/minecraft/network/chat/Component;"))
    private Component cosmiccore$modifyPageText(int page, int total, int maxWidth) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();

        if (getType() == SidebarType.FAVORITES) {
            int groupIndex = manager.getActiveIndex() + 1;
            int groupCount = manager.getGroupCount();

            // For recipe groups, calculate our own page count
            int displayPage = page;
            int displayTotal = total;
            if (manager.getActiveGroup().isRecipeGroup() && space != null) {
                cosmiccore$recipePageCount = manager.getRecipePageCount(space.tw, space.th);
                displayTotal = cosmiccore$recipePageCount;
                // EMI's page is 0-indexed based on slot count, convert to recipe pages
                cosmiccore$currentRecipePage = space.pageSize > 0 ?
                        page * space.pageSize / Math.max(1, space.tw * space.th) : 0;
                if (cosmiccore$currentRecipePage >= cosmiccore$recipePageCount) {
                    cosmiccore$currentRecipePage = cosmiccore$recipePageCount - 1;
                }
                displayPage = cosmiccore$currentRecipePage;
                manager.setCurrentRecipePage(cosmiccore$currentRecipePage);
            }

            Component pageText = EmiRenderHelper.getPageText(displayPage, displayTotal, maxWidth);

            if (groupCount > 1) {
                ChatFormatting color = manager.getActiveGroup().isRecipeGroup() ? ChatFormatting.YELLOW :
                        ChatFormatting.AQUA;
                String groupPrefix = "Group " + groupIndex + "/" + groupCount + " | ";
                return EmiPort.literal(groupPrefix).withStyle(color).append(pageText);
            }

            return pageText;
        }

        return EmiRenderHelper.getPageText(page, total, maxWidth);
    }

    @Unique
    public int cosmiccore$getRecipePageCount() {
        return cosmiccore$recipePageCount;
    }

    @Unique
    public int cosmiccore$getCurrentRecipePage() {
        return cosmiccore$currentRecipePage;
    }
}
