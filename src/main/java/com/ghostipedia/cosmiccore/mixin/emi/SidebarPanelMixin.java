package com.ghostipedia.cosmiccore.mixin.emi;

import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkHeaderLayout;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkManager;
import com.ghostipedia.cosmiccore.integration.emi.favorites.CosmicBookmarkUiState;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SidebarButtonWidget;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EmiScreenManager.SidebarPanel.class, remap = false)
public abstract class SidebarPanelMixin {

    @Shadow
    public abstract SidebarType getType();

    @Shadow
    public EmiScreenManager.ScreenSpace space;

    @Shadow
    public boolean header;

    @Shadow
    public SizedButtonWidget pageLeft;

    @Shadow
    public SizedButtonWidget pageRight;

    @Shadow
    public SidebarButtonWidget cycle;

    @Inject(method = "updateWidgetPosition", at = @At("RETURN"))
    private void cosmiccore$positionBookmarkHeader(CallbackInfo ci) {
        if (getType() != SidebarType.FAVORITES) return;
        CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout
                .create((EmiScreenManager.SidebarPanel) (Object) this);
        if (layout == null) return;
        cycle.setX(layout.cycle().x());
        cycle.setY(layout.cycle().y());
    }

    @Inject(method = "updateWidgetVisibility", at = @At("RETURN"))
    private void cosmiccore$replaceBookmarkPageButtons(CallbackInfo ci) {
        if (getType() != SidebarType.FAVORITES) return;
        CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout
                .create((EmiScreenManager.SidebarPanel) (Object) this);
        if (layout == null) return;
        pageLeft.visible = false;
        pageRight.visible = false;
        cycle.visible = false;
    }

    @Inject(method = "drawHeader", at = @At("HEAD"), cancellable = true)
    private void cosmiccore$drawBookmarkHeader(EmiDrawContext context, int mouseX, int mouseY, float delta,
                                               int page, int totalPages, CallbackInfo ci) {
        CosmicBookmarkManager manager = CosmicBookmarkManager.getInstance();
        if (getType() != SidebarType.FAVORITES || !manager.isReady()) return;
        CosmicBookmarkHeaderLayout layout = CosmicBookmarkHeaderLayout
                .create((EmiScreenManager.SidebarPanel) (Object) this);
        if (layout == null) return;
        ci.cancel();

        boolean multipleGroups = manager.getGroupCount() > 1;
        boolean multiplePages = totalPages > 1;
        cosmiccore$drawFavoritesButton(context, layout.cycle(), mouseX, mouseY);
        cosmiccore$drawArrow(context, layout.groupPrevious(), false, multipleGroups, mouseX, mouseY);
        cosmiccore$drawArrow(context, layout.groupNext(), true, multipleGroups, mouseX, mouseY);
        cosmiccore$drawArrow(context, layout.pagePrevious(), false, multiplePages, mouseX, mouseY);
        cosmiccore$drawArrow(context, layout.pageNext(), true, multiplePages, mouseX, mouseY);
        cosmiccore$drawGroupAction(
                context,
                layout.groupAction(),
                CosmicBookmarkUiState.isForceDeleteModifierDown() ||
                        manager.getGroupCount() > 1 && manager.getActiveGroup().size() == 0,
                mouseX,
                mouseY);

        Component groupText = Component.translatable(
                "cosmiccore.emi.bookmarks.header",
                manager.getActiveIndex() + 1,
                manager.getGroupCount())
                .withStyle(manager.getActiveGroup().isRecipeGroup() ? ChatFormatting.YELLOW : ChatFormatting.AQUA);
        Component pageText = EmiRenderHelper.getPageText(page + 1, totalPages, layout.pageLabel().width());
        cosmiccore$drawClippedText(context, groupText, layout.groupLabel());
        cosmiccore$drawClippedText(context, pageText, layout.pageLabel());
        cosmiccore$drawProgress(context, layout.groupLabel(), manager.getActiveIndex(), manager.getGroupCount());
        cosmiccore$drawProgress(context, layout.pageLabel(), page, totalPages);
    }

    @Unique
    private static void cosmiccore$drawFavoritesButton(EmiDrawContext context, Bounds bounds, int mouseX, int mouseY) {
        int v = SidebarType.FAVORITES.v + (bounds.contains(mouseX, mouseY) ? 16 : 0);
        context.drawTexture(
                EmiRenderHelper.WIDGETS,
                bounds.x(),
                bounds.y(),
                bounds.width(),
                bounds.height(),
                SidebarType.FAVORITES.u,
                v,
                16,
                16,
                256,
                256);
        if (CosmicBookmarkUiState.isAlertVisible()) {
            context.drawTextWithShadow(
                    Component.literal("!").withStyle(ChatFormatting.GOLD),
                    bounds.x() + 1,
                    bounds.y() - 1);
        }
    }

    @Unique
    private static void cosmiccore$drawGroupAction(EmiDrawContext context, Bounds bounds, boolean delete,
                                                   int mouseX, int mouseY) {
        boolean hovered = bounds.contains(mouseX, mouseY);
        int x = bounds.x() + (bounds.width() - 12) / 2;
        int y = bounds.y() + (bounds.height() - 12) / 2;
        int v = hovered ? 12 : 0;
        if (!delete) {
            context.drawTexture(EmiRenderHelper.BUTTONS, x, y, 12, 12, 24, v, 12, 12, 256, 256);
            return;
        }
        context.drawTexture(EmiRenderHelper.BUTTONS, x, y, 12, 12, 72, v, 12, 12, 256, 256);
        context.drawTexture(EmiRenderHelper.BUTTONS, x + 3, y + 5, 6, 2, 27, v + 5, 6, 2, 256, 256);
    }

    @Unique
    private static void cosmiccore$drawArrow(EmiDrawContext context, Bounds bounds, boolean right, boolean active,
                                             int mouseX, int mouseY) {
        int v = active ? (bounds.contains(mouseX, mouseY) ? 16 : 0) : 32;
        context.drawTexture(
                EmiRenderHelper.BUTTONS,
                bounds.x(),
                bounds.y(),
                bounds.width(),
                bounds.height(),
                right ? 240 : 224,
                v,
                16,
                16,
                256,
                256);
    }

    @Unique
    private static void cosmiccore$drawClippedText(EmiDrawContext context, Component text, Bounds bounds) {
        Minecraft minecraft = Minecraft.getInstance();
        if (bounds.width() <= 0) return;
        FormattedCharSequence rendered;
        if (minecraft.font.width(text) <= bounds.width()) {
            rendered = text.getVisualOrderText();
        } else {
            int ellipsisWidth = minecraft.font.width(CommonComponents.ELLIPSIS);
            if (bounds.width() < ellipsisWidth) return;
            Component clipped = Component.literal(
                    minecraft.font.substrByWidth(text, bounds.width() - ellipsisWidth).getString().stripTrailing())
                    .withStyle(text.getStyle())
                    .append(CommonComponents.ELLIPSIS.copy().withStyle(text.getStyle()));
            rendered = clipped.getVisualOrderText();
        }
        int x = bounds.x() + Math.max(0, (bounds.width() - minecraft.font.width(rendered)) / 2);
        context.raw().enableScissor(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        try {
            context.drawText(rendered, x, bounds.y() + 3, -1);
        } finally {
            context.raw().disableScissor();
        }
    }

    @Unique
    private static void cosmiccore$drawProgress(EmiDrawContext context, Bounds bounds, int index, int total) {
        if (total <= 1 || bounds.width() <= 2) return;
        int y = bounds.bottom() - 4;
        context.fill(bounds.x(), y, bounds.width(), 2, 0x55555555);
        EmiRenderHelper.drawScroll(context, bounds.x(), y, bounds.width(), 2, index, total, 0xFFFFFFFF);
    }
}
