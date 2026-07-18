package com.ghostipedia.cosmiccore.mixin.modularui;

import com.ghostipedia.cosmiccore.client.tooltip.FoodTooltips;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.GuiContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiDraw.class, remap = false)
public abstract class FoodTooltipBackgroundMixin {

    private static final int FRAME_MARGIN = 9;

    @Inject(method = "drawTooltipBackground", at = @At("HEAD"), remap = false)
    private static void cosmiccore$drawFoodTooltipBackground(GuiContext context, ItemStack stack,
                                                             List<ClientTooltipComponent> lines,
                                                             int x, int y, int textWidth, int height,
                                                             @Nullable IRichTextBuilder<?> tooltip,
                                                             CallbackInfo ci) {
        if (tooltip == null || !CosmicFoodRegistry.isConsumable(stack)) {
            return;
        }

        int margin = FRAME_MARGIN;
        context.getGraphics().blitSprite(FoodTooltips.FRAME, x - margin, y - margin,
                textWidth + 2 * margin, height + 2 * margin);
    }
}
