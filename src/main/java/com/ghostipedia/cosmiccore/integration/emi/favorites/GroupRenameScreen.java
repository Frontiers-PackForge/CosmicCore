package com.ghostipedia.cosmiccore.integration.emi.favorites;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import dev.emi.emi.config.SidebarType;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.Nullable;

public class GroupRenameScreen extends Screen {

    private final Screen parent;
    private final int groupIndex;
    private final String originalName;

    private EditBox nameField;

    public GroupRenameScreen(@Nullable Screen parent, int groupIndex, String originalName) {
        super(Component.literal("Rename Bookmark Group"));
        this.parent = parent;
        this.groupIndex = groupIndex;
        this.originalName = originalName;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.nameField = new EditBox(this.font, centerX - 100, centerY - 10, 200, 20,
                Component.literal("Group Name"));
        this.nameField.setMaxLength(32);
        this.nameField.setValue(originalName);
        this.nameField.setFocused(true);
        this.addRenderableWidget(this.nameField);

        this.addRenderableWidget(Button.builder(Component.literal("Rename"), button -> {
            saveAndClose();
        }).bounds(centerX - 102, centerY + 20, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            this.onClose();
        }).bounds(centerX + 2, centerY + 20, 100, 20).build());
    }

    private void saveAndClose() {
        String newName = this.nameField.getValue().trim();
        if (!newName.isEmpty() && !newName.equals(originalName)) {
            CosmicBookmarkManager.getInstance().renameGroup(groupIndex, newName);
            EmiScreenManager.repopulatePanels(SidebarType.FAVORITES);
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        graphics.drawString(this.font, "Group Name:", this.width / 2 - 100, this.height / 2 - 25, 0xA0A0A0);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            saveAndClose();
            return true;
        }
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
