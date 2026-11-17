package com.ghostipedia.cosmiccore.client.recipe;

import com.ghostipedia.cosmiccore.CosmicCore;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.AbstractProgressDrawable;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public final class AutomatedRitualRecipeUI {

    private static final UITexture PROGRESS = UITexture.builder()
            .location(CosmicCore.id("textures/gui/progress_bar/automated_rituals.png"))
            .build();

    private AutomatedRitualRecipeUI() {}

    public static Flow create(GTRecipe recipe) {
        var circle = new ParentWidget<>().size(148, 148);
        circle.child(new RitualProgressDrawable()
                .emptyTexture(PROGRESS.getSubArea(0, 0, 1, 0.5f))
                .filledTexture(PROGRESS.getSubArea(0, 0.5f, 1, 1))
                .asWidget().value(DoubleValue.simulateProgress(2000)).size(96, 96).pos(26, 26));
        for (int index = 0; index < 13; index++) {
            boolean input = index < 12;
            int slotIndex = input ? index : 0;
            double angle = slotIndex * Math.PI / 6 - Math.PI / 2;
            int x = input ? 65 + (int) Math.round(58 * Math.cos(angle)) : 65;
            int y = input ? 65 + (int) Math.round(58 * Math.sin(angle)) : 65;
            circle.child(RecipeViewerSlotWidget.create(ItemStack.class)
                    .value(ItemStackList.of(ItemStack.EMPTY))
                    .background(GuiTextures.SLOT_ITEM)
                    .name(GTRecipeViewerWidget.capabilityWidgetName(ItemRecipeCapability.CAP,
                            input ? IO.IN : IO.OUT, slotIndex))
                    .pos(x, y));
        }
        return Flow.col().coverChildren().childPadding(3)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(circle)
                .child(Flow.col().childPadding(1).coverChildrenHeight().widthRel(1f)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .name(GTRecipeViewerWidget.capabilityWidgetName(EURecipeCapability.CAP, IO.IN, 0)));
    }

    private static final class RitualProgressDrawable extends AbstractProgressDrawable<RitualProgressDrawable> {

        @Override
        protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height,
                                           WidgetTheme widgetTheme) {
            context.getStencil().push(() -> {
                ShaderInstance shader = RenderSystem.getShader();
                RenderSystem.setShader(GameRenderer::getPositionShader);
                Matrix4f pose = context.graphicsPose().last().pose();
                BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                        DefaultVertexFormat.POSITION);
                double radius = 45 - 31 * progress;
                for (int row = 0; row < 96; row++) {
                    double dy = row - 47.5;
                    double half = Math.sqrt(Math.max(0, radius * radius - dy * dy));
                    int left = Math.max(0, (int) Math.ceil(47.5 - half));
                    int right = 96 - left;
                    for (int side = 0; side < 2; side++) {
                        float x0 = x + width * (side == 0 ? 0 : right) / 96f;
                        float x1 = x + width * (side == 0 ? left : 96) / 96f;
                        float y0 = y + height * row / 96f;
                        float y1 = y + height * (row + 1) / 96f;
                        buffer.addVertex(pose, x0, y0, 0);
                        buffer.addVertex(pose, x0, y1, 0);
                        buffer.addVertex(pose, x1, y1, 0);
                        buffer.addVertex(pose, x1, y0, 0);
                    }
                }
                BufferUploader.drawWithShader(buffer.buildOrThrow());
                RenderSystem.setShader(() -> shader);
            }, x, y, width, height);
        }
    }
}
