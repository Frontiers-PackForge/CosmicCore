package com.ghostipedia.cosmiccore.client.tooltip;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.food.CosmicFoodRegistry;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import brachy.modularui.screen.event.RichTooltipEvent;
import com.mojang.datafixers.util.Either;
import org.joml.Vector2ic;

import java.util.List;

@EventBusSubscriber(modid = CosmicCore.MOD_ID, value = Dist.CLIENT)
public final class FoodTooltipEvents {

    private static final int FRAME_MARGIN = 9;
    private static final int TOOLTIP_Z = 400;

    private FoodTooltipEvents() {}

    @SubscribeEvent
    public static void onGather(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (!CosmicFoodRegistry.isConsumable(stack)) return;
        FoodTooltipComponent block = CosmicFoodRegistry.isVile(stack.getItem()) ? FoodTooltips.buildVile() :
                FoodTooltips.build(stack, CosmicFoodRegistry.get(stack));
        event.getTooltipElements().add(Either.right(block));
    }

    @SubscribeEvent
    public static void onColor(RenderTooltipEvent.Color event) {
        if (!CosmicFoodRegistry.isConsumable(event.getItemStack())) return;
        event.setBackgroundStart(0);
        event.setBackgroundEnd(0);
        event.setBorderStart(0);
        event.setBorderEnd(0);
    }

    @SubscribeEvent
    public static void onPre(RenderTooltipEvent.Pre event) {
        drawFrame(event);
    }

    @SubscribeEvent
    public static void onMuiPre(RichTooltipEvent.Pre event) {
        drawFrame(event);
    }

    private static void drawFrame(RenderTooltipEvent.Pre event) {
        if (!CosmicFoodRegistry.isConsumable(event.getItemStack())) return;
        List<ClientTooltipComponent> comps = event.getComponents();
        if (comps.isEmpty()) return;

        Font font = event.getFont();
        int width = 0;
        int height = comps.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent c : comps) {
            int cw = c.getWidth(font);
            if (cw > width) width = cw;
            height += c.getHeight();
        }

        ClientTooltipPositioner positioner = event.getTooltipPositioner();
        Vector2ic pos = positioner.positionTooltip(event.getScreenWidth(), event.getScreenHeight(),
                event.getX(), event.getY(), width, height);

        int m = FRAME_MARGIN;
        var pose = event.getGraphics().pose();
        pose.pushPose();
        pose.translate(0, 0, TOOLTIP_Z);
        event.getGraphics().blitSprite(FoodTooltips.FRAME, pos.x() - m, pos.y() - m, width + 2 * m, height + 2 * m);
        pose.popPose();
    }
}
