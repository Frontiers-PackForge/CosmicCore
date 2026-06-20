package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * A phantom item slot for the recipe-maker. Clicking with something on the cursor sets the ghost item (parent
 * behaviour); with an empty cursor, left/right-click adjusts the stack count by one (clearing at zero) and
 * middle-click cycles through common counts. Shift-click opens the per-slot options popout (chance/boost for
 * outputs, not-consumed for inputs). All changes are applied server-side via the client-action channel.
 */
public class ConfigurableItemSlot extends PhantomSlotWidget {

    private static final int ADJUST = 100;
    private static final int CYCLE = 101;
    private static final int CONFIGURE = 102;
    private static final int[] PRESETS = { 1, 2, 4, 8, 16, 32, 64 };

    private final IItemHandlerModifiable handler;
    private final int index;
    private Runnable onConfigure;

    public ConfigurableItemSlot(IItemHandlerModifiable handler, int index, int x, int y) {
        super(handler, index, x, y);
        this.handler = handler;
        this.index = index;
        setClearSlotOnRightClick(false);
        setBackgroundTexture(GuiTextures.SLOT);
    }

    public ConfigurableItemSlot setOnConfigure(Runnable onConfigure) {
        this.onConfigure = onConfigure;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (onConfigure != null && button == 2) { //MMB for configs - SHIFT+CLICK for other bullshit that isn't configs man.
                writeClientAction(CONFIGURE, buffer -> {});
                onConfigure.run();
                return true;
            }
            if (Minecraft.getInstance().player.containerMenu.getCarried().isEmpty()
                    && !handler.getStackInSlot(index).isEmpty()) {
                if (Screen.hasShiftDown()) {
                    writeClientAction(CYCLE, buffer -> {});
                    return true;
                }
                if (button == 0) {
                    writeClientAction(ADJUST, buffer -> buffer.writeBoolean(true));
                    return true;
                }
                if (button == 1) {
                    writeClientAction(ADJUST, buffer -> buffer.writeBoolean(false));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        ItemStack stack = handler.getStackInSlot(index);
        if (id == ADJUST && !stack.isEmpty()) {
            setCount(stack, stack.getCount() + (buffer.readBoolean() ? 1 : -1));
        } else if (id == CYCLE && !stack.isEmpty()) {
            setCount(stack, nextPreset(stack.getCount()));
        } else if (id == CONFIGURE) {
            if (onConfigure != null) onConfigure.run();
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    private void setCount(ItemStack stack, int count) {
        if (count <= 0) {
            handler.setStackInSlot(index, ItemStack.EMPTY);
        } else {
            stack.setCount(count);
            handler.setStackInSlot(index, stack);
        }
    }

    private static int nextPreset(int current) {
        for (int preset : PRESETS) {
            if (preset > current) return preset;
        }
        return PRESETS[0];
    }
}
