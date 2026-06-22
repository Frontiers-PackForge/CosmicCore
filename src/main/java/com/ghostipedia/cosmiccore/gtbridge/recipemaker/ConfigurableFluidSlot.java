package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.widget.PhantomFluidWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * A phantom fluid slot for the recipe-maker. Clicking with a fluid container on the cursor sets the ghost fluid
 * (parent behaviour); with an empty cursor, left/right-click steps the amount by 250mB (clearing at zero) and
 * shift-click cycles through common amounts. Middle-click opens the per-slot options popout. Changes are applied
 * server-side via the client-action channel.
 */
public class ConfigurableFluidSlot extends PhantomFluidWidget {

    private static final int ADJUST = 100;
    private static final int CYCLE = 101;
    private static final int CONFIGURE = 102;
    private static final int STEP = 250;
    private static final int[] PRESETS = { 250, 500, 1000, 2000, 4000, 8000, 16000, 32000 };

    private final FluidTank tank;
    private Runnable onConfigure;

    public ConfigurableFluidSlot(FluidTank tank, int x, int y) {
        super(tank, 0, x, y, 18, 18, tank::getFluid, tank::setFluid);
        this.tank = tank;
        setBackground(GuiTextures.FLUID_SLOT);
    }

    public ConfigurableFluidSlot setOnConfigure(Runnable onConfigure) {
        this.onConfigure = onConfigure;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (onConfigure != null && button == 2) {
                writeClientAction(CONFIGURE, buffer -> {});
                onConfigure.run();
                return true;
            }
            if (Minecraft.getInstance().player.containerMenu.getCarried().isEmpty() &&
                    !getLastFluidInTank().isEmpty()) {
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
        FluidStack fluid = tank.getFluid();
        if (id == ADJUST && !fluid.isEmpty()) {
            setAmount(fluid, fluid.getAmount() + (buffer.readBoolean() ? STEP : -STEP));
        } else if (id == CYCLE && !fluid.isEmpty()) {
            setAmount(fluid, nextPreset(fluid.getAmount()));
        } else if (id == CONFIGURE) {
            if (onConfigure != null) onConfigure.run();
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    private void setAmount(FluidStack fluid, int amount) {
        if (amount <= 0) {
            tank.setFluid(FluidStack.EMPTY);
        } else {
            fluid.setAmount(Math.min(amount, tank.getCapacity()));
            tank.setFluid(fluid);
        }
    }

    private static int nextPreset(int current) {
        for (int preset : PRESETS) {
            if (preset > current) return preset;
        }
        return PRESETS[0];
    }
}
