package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StellarCommandConsoleWidget extends WidgetGroup {

    public static final int WIDTH = 360;
    public static final int HEIGHT = 220;

    private final Supplier<IrisMultiblockMachine> machineSupplier;

    private Stage lastSyncedStage = Stage.EMPTY;
    private float fuelLevel = 0f;
    private boolean canIgnite = false;
    private boolean debugPrimed = false;

    private int tickCounter = 0;

    public StellarCommandConsoleWidget(Supplier<IrisMultiblockMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    private void initWidgets() {
        addWidget(new StarfieldBackgroundWidget(0, 0, WIDTH, HEIGHT, this::getCurrentStage));

        addWidget(new HolographicScanlineWidget(0, 0, WIDTH, HEIGHT, this::getCurrentStage));

        addWidget(new EnergyConduitWidget(0, 0, WIDTH, HEIGHT, this::getCurrentStage));

        int coreSize = 130;
        int coreX = 20;
        int coreY = 30;
        addWidget(new StellarCoreWidget(coreX, coreY, coreSize, this::getCurrentStage));

        addWidget(new OrbitalRingsWidget(coreX - 10, coreY - 10, coreSize + 20, coreSize + 20, this::getCurrentStage));

        int telemetryX = coreX + coreSize + 25;
        int telemetryW = WIDTH - telemetryX - 15;
        int telemetryH = 130;
        addWidget(new TelemetryPanelWidget(telemetryX, 25, telemetryW, telemetryH,
                machineSupplier, this::getCurrentStage));

        int controlY = 160;
        int controlH = HEIGHT - controlY - 10;
        addWidget(new ControlPanelWidget(telemetryX, controlY, telemetryW, controlH,
                machineSupplier, this));

        addWidget(new WarningOverlayWidget(0, 0, WIDTH, HEIGHT, this::getCurrentStage));

        addWidget(new DebugPrimeButton(5, HEIGHT - 19, 50, 14, this::requestDebugPrime));
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

    public int getTickCounter() {
        return tickCounter;
    }

    public void requestDebugPrime() {
        writeClientAction(3, buf -> {});
    }

    public void requestIgnition() {
        writeClientAction(1, buf -> {});
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        Stage currentStage = machine.getStage();
        if (currentStage != lastSyncedStage) {
            lastSyncedStage = currentStage;
            writeUpdateInfo(203, buf -> buf.writeEnum(currentStage));
        }

        float newFuelLevel = calculateFuelLevel(machine);
        if (Math.abs(newFuelLevel - fuelLevel) > 0.01f) {
            fuelLevel = newFuelLevel;
            writeUpdateInfo(204, buf -> buf.writeFloat(fuelLevel));
        }

        boolean newCanIgnite = checkIgnitionRequirements(machine);
        if (newCanIgnite != canIgnite) {
            canIgnite = newCanIgnite;
            writeUpdateInfo(205, buf -> buf.writeBoolean(canIgnite));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 203) {
            lastSyncedStage = buffer.readEnum(Stage.class);
        } else if (id == 204) {
            fuelLevel = buffer.readFloat();
        } else if (id == 205) {
            canIgnite = buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    private float calculateFuelLevel(IrisMultiblockMachine machine) {
        if (debugPrimed) return 1f;

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
        if (debugPrimed) return true;
        if (machine.getStage() != Stage.EMPTY) return false;
        if (machine.getInventory().getStackInSlot(0).isEmpty()) return false;
        return fuelLevel >= 0.8f;
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

        drawConsoleFrame(graphics, x, y, w, h);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        drawConsoleTitle(graphics, x, y, w);
    }

    private void drawConsoleFrame(GuiGraphics graphics, int x, int y, int w, int h) {
        int frameColor = getStageFrameColor(lastSyncedStage);
        int frameGlow = (0x30 << 24) | (frameColor & 0x00FFFFFF);

        graphics.fill(x - 2, y - 2, x + w + 2, y, frameGlow);
        graphics.fill(x - 2, y + h, x + w + 2, y + h + 2, frameGlow);
        graphics.fill(x - 2, y, x, y + h, frameGlow);
        graphics.fill(x + w, y, x + w + 2, y + h, frameGlow);

        int cornerLen = 15;
        int cornerColor = (0xC0 << 24) | frameColor;

        graphics.fill(x, y, x + cornerLen, y + 2, cornerColor);
        graphics.fill(x, y, x + 2, y + cornerLen, cornerColor);

        graphics.fill(x + w - cornerLen, y, x + w, y + 2, cornerColor);
        graphics.fill(x + w - 2, y, x + w, y + cornerLen, cornerColor);

        graphics.fill(x, y + h - 2, x + cornerLen, y + h, cornerColor);
        graphics.fill(x, y + h - cornerLen, x + 2, y + h, cornerColor);

        graphics.fill(x + w - cornerLen, y + h - 2, x + w, y + h, cornerColor);
        graphics.fill(x + w - 2, y + h - cornerLen, x + w, y + h, cornerColor);
    }

    private void drawConsoleTitle(GuiGraphics graphics, int x, int y, int w) {
        var font = Minecraft.getInstance().font;

        String title = "STELLAR IRIS COMMAND CONSOLE";
        int titleW = font.width(title);
        int titleX = x + (w - titleW) / 2;
        int titleY = y + 8;

        int frameColor = getStageFrameColor(lastSyncedStage);

        int bgW = titleW + 20;
        int bgX = x + (w - bgW) / 2;
        graphics.fill(bgX, titleY - 3, bgX + bgW, titleY + 11, 0xE0080810);

        int borderColor = (0x80 << 24) | frameColor;
        graphics.fill(bgX, titleY - 3, bgX + bgW, titleY - 2, borderColor);
        graphics.fill(bgX, titleY + 10, bgX + bgW, titleY + 11, borderColor);
        graphics.fill(bgX, titleY - 2, bgX + 1, titleY + 10, borderColor);
        graphics.fill(bgX + bgW - 1, titleY - 2, bgX + bgW, titleY + 10, borderColor);

        int textColor = 0xFFFFFFFF;
        if (lastSyncedStage == Stage.DEATH && (tickCounter / 5) % 2 == 0) {
            textColor = 0xFFFF4444;
        }

        graphics.drawString(font, title, titleX, titleY, textColor, true);
    }

    private int getStageFrameColor(Stage stage) {
        return switch (stage) {
            case EMPTY -> 0x506080;
            case GROWING -> 0x6090FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF7722;
            case BLACK_HOLE -> 0xAA55FF;
            case DEATH -> 0xFF3030;
            case DEATH_GRACEFUL -> 0x664040;
        };
    }

    private static class ControlPanelWidget extends WidgetGroup {

        private final Supplier<IrisMultiblockMachine> machineSupplier;
        private final StellarCommandConsoleWidget parent;

        public ControlPanelWidget(int x, int y, int width, int height,
                                  Supplier<IrisMultiblockMachine> machineSupplier,
                                  StellarCommandConsoleWidget parent) {
            super(x, y, width, height);
            this.machineSupplier = machineSupplier;
            this.parent = parent;
            initWidgets();
        }

        private void initWidgets() {
            addWidget(new FuelGaugeWidget(5, 5, getSize().width - 75, 22, parent::getFuelLevel));

            addWidget(new IgnitionButtonWidget(
                    5, 30, getSize().width - 75, 22,
                    parent::canIgnite,
                    () -> parent.getCurrentStage() == Stage.EMPTY || parent.canIgnite(),
                    parent::requestIgnition));

            IrisMultiblockMachine machine = machineSupplier.get();
            if (machine != null) {
                int slotX = getSize().width - 65;
                SlotWidget starSeedSlot = new SlotWidget(machine.getInventory().storage, 0, slotX, 12, true, true);
                starSeedSlot.setBackground(GuiTextures.SLOT, GuiTextures.ATOMIC_OVERLAY_1);
                addWidget(starSeedSlot);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPosition().x;
            int y = getPosition().y;
            int w = getSize().width;
            int h = getSize().height;

            DrawerHelper.drawSolidRect(graphics, x, y, w, h, 0xCC0a0a14);

            Stage stage = parent.getCurrentStage();
            int accentColor = getStageAccentColor(stage);
            DrawerHelper.drawBorder(graphics, x, y, w, h, (0x80 << 24) | accentColor, 1);
            graphics.fill(x + 1, y + 1, x + w - 1, y + 3, (0x60 << 24) | accentColor);

            var font = Minecraft.getInstance().font;
            graphics.drawString(font, "IGNITION CONTROL", x + 5, y - 8, (0xA0 << 24) | accentColor, false);

            int slotX = x + w - 65;
            graphics.drawString(font, "SEED", slotX + 8, y + 32, 0xFF606080, false);

            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        }

        private int getStageAccentColor(Stage stage) {
            return switch (stage) {
                case EMPTY -> 0x506080;
                case GROWING -> 0x6090FF;
                case STAR -> 0xFFCC44;
                case SUPERSTAR -> 0xFF7722;
                case BLACK_HOLE -> 0xAA55FF;
                case DEATH -> 0xFF3030;
                case DEATH_GRACEFUL -> 0x664040;
            };
        }
    }
}
