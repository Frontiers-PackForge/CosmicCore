package com.ghostipedia.cosmiccore.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.ITooltip;
import com.ghostipedia.cosmiccore.CosmicCore;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class ModifyStackButton extends Button implements ITooltip {

    private final MultiplierIcons icon;
    private final Component displayName;
    private final Component displayValue;



    public ModifyStackButton(Button.OnPress onPress, MultiplierIcons icon, Component displayName, Component displayValue) {
        super(0, 0, 8, 8, Component.empty(), onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.displayName = displayName;
        this.displayValue = displayValue;
    }

    public void setVisibility(boolean vis) {
        visible = vis;
        active = vis;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (visible){
            Blitter blitter = icon.getBlitter();
            if (!visible) {
                blitter.opacity(0.5F);
            }
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();

            if (isFocused()) {
                guiGraphics.fill(getX() - 1, getY() - 1, getX() + width + 1, getY(), -1);
                guiGraphics.fill(getX() - 1, getY(), getX(), getY() + height, -1);
                guiGraphics.fill(getX() + width, getY(), getX() + width + 1, getY() + height, -1);
                guiGraphics.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + 1, -1);
            }
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(getX(),getY(),0);
            poseStack.scale(0.5F,0.5F,1F);
            Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(0,0).blit(guiGraphics);
            blitter.dest(0,0).blit(guiGraphics);
            poseStack.popPose();
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), 8, 8);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    @Override
    public List<Component> getTooltipMessage() {
        return Collections.singletonList(Component.empty().append(displayName).append("\n").append(displayValue));
    }

    //Enum junk for UI
    public enum MultiplierIcons {
        MULT_2(0, 0),
        MULT_3(16, 0),
        MULT_8(32, 0),
        DIV_2(0, 16),
        DIV_3(16, 16),
        DIV_8(32, 16);

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private static final ResourceLocation TEXTURE = CosmicCore.id("textures/gui/states.png");
        private static final int TEXTURE_WIDTH = 48;
        private static final int TEXTURE_HEIGHT = 48;

        MultiplierIcons(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 16;
            this.height = 16;
        }

        public Blitter getBlitter() {
            return Blitter.texture(TEXTURE,TEXTURE_WIDTH,TEXTURE_HEIGHT).src(x,y,width,height);
        }
    }



}
