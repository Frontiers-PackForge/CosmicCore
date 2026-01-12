package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class StellarIrisWidget extends WidgetGroup {

    public static final int WIDTH = 310;
    public static final int HEIGHT = 160;

    private final Supplier<IrisMultiblockMachine> machineSupplier;

    private Stage lastSyncedStage = Stage.EMPTY;
    private float fuelLevel = 0f;
    private boolean canIgnite = false;

    private StellarCoreWidget coreWidget;
    private StageContextPanel contextPanel;

    private int tickCounter = 0;
    private boolean debugPrimed = false;

    public StellarIrisWidget(Supplier<IrisMultiblockMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new StarfieldBackgroundWidget(0, 0, WIDTH, HEIGHT, this::getCurrentStage));

        int margin = 5;
        int gap = 5;

        int coreSize = 130;
        int panelWidth = 135;

        int panelX = WIDTH - margin - panelWidth;
        int panelY = margin;
        int panelH = HEIGHT - (margin * 2);

        int coreX = panelX - gap - coreSize;
        int coreY = (HEIGHT - coreSize) / 2;

        addWidget(new OrbitalRingsWidget(coreX - 5, coreY - 5, coreSize + 10, coreSize + 10, this::getCurrentStage));

        coreWidget = new StellarCoreWidget(coreX, coreY, coreSize, this::getCurrentStage);
        addWidget(coreWidget);

        contextPanel = new StageContextPanel(panelX, panelY, panelWidth, panelH, machineSupplier, this);
        addWidget(contextPanel);

        initDebugButtons();
    }

    private void initDebugButtons() {
        int btnWidth = 50;
        int btnHeight = 14;
        int btnX = 5;
        int btnY = HEIGHT - btnHeight - 5;

        addWidget(new DebugPrimeButton(btnX, btnY, btnWidth, btnHeight, this::requestDebugPrime));
    }

    public void requestDebugPrime() {
        writeClientAction(3, buf -> {});
    }

    public Stage getCurrentStage() {
        return lastSyncedStage;
    }

    public float getFuelLevel() {
        return fuelLevel;
    }

    public boolean canIgnite() {
        return canIgnite;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        Stage currentStage = machine.getStage();
        if (currentStage != lastSyncedStage) {
            lastSyncedStage = currentStage;
            writeUpdateInfo(1, buf -> buf.writeEnum(currentStage));
        }

        float newFuelLevel = calculateFuelLevel(machine);
        if (Math.abs(newFuelLevel - fuelLevel) > 0.01f) {
            fuelLevel = newFuelLevel;
            writeUpdateInfo(2, buf -> buf.writeFloat(fuelLevel));
        }

        boolean newCanIgnite = checkIgnitionRequirements(machine);
        if (newCanIgnite != canIgnite) {
            canIgnite = newCanIgnite;
            writeUpdateInfo(3, buf -> buf.writeBoolean(canIgnite));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            lastSyncedStage = buffer.readEnum(Stage.class);
        } else if (id == 2) {
            fuelLevel = buffer.readFloat();
        } else if (id == 3) {
            canIgnite = buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    private float calculateFuelLevel(IrisMultiblockMachine machine) {
        if (debugPrimed) {
            return 1f;
        }

        if (machine.getStage() == Stage.EMPTY) {
            return machine.getInventory().getStackInSlot(0).isEmpty() ? 0f : 1f;
        }
        return switch (machine.getStage()) {
            case EMPTY -> 0f;
            case GROWING -> 0.5f;
            case STAR, SUPERSTAR, BLACK_HOLE -> 1f;
            case DEATH, DEATH_GRACEFUL -> 0.2f;
        };
    }

    private boolean checkIgnitionRequirements(IrisMultiblockMachine machine) {
        if (debugPrimed) {
            return true;
        }
        if (machine.getStage() != Stage.EMPTY) return false;
        if (machine.getInventory().getStackInSlot(0).isEmpty()) return false;
        return fuelLevel >= 0.8f;
    }

    public void debugPrime() {
        debugPrimed = true;
    }

    public void requestIgnition() {
        writeClientAction(1, buf -> {});
    }

    public void requestStageAdvance() {
        writeClientAction(2, buf -> {});
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        if (id == 1) {
            if (canIgnite || debugPrimed) {
                machine.setStarStage();
                debugPrimed = false;
            }
        } else if (id == 2) {
            machine.setStarStage();
        } else if (id == 3) {
            debugPrimed = true;
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        tickCounter++;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        int borderColor = getStageAccentColor(lastSyncedStage, 0.4f);
        DrawerHelper.drawBorder(graphics, x, y, w, h, borderColor, 1);

        if (lastSyncedStage != Stage.EMPTY) {
            int glowColor = getStageAccentColor(lastSyncedStage, 0.15f);
            DrawerHelper.drawGradientRect(graphics, x + 1, y + 1, w - 2, 20, glowColor, 0x00000000, false);
        }

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private int getStageAccentColor(Stage stage, float alpha) {
        int a = (int) (alpha * 255) << 24;
        return switch (stage) {
            case EMPTY -> a | 0x404050;
            case GROWING -> a | 0x6080FF;
            case STAR -> a | 0xFFCC44;
            case SUPERSTAR -> a | 0xFF8844;
            case BLACK_HOLE -> a | 0x8040FF;
            case DEATH -> a | 0xFF2020;
            case DEATH_GRACEFUL -> a | 0x804040;
        };
    }

    public int getTickCounter() {
        return tickCounter;
    }
}
