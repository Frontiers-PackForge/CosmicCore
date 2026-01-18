package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.GTValues;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StellarModuleContentWidget extends WidgetGroup {

    public static final int WIDTH = 186;
    public static final int HEIGHT = 100;

    private static final int GEAR_BUTTON_SIZE = 20;
    private static final ResourceLocation GEAR_TEXTURE = new ResourceLocation("gtceu",
            "textures/item/material_sets/dull/gear_small.png");

    private final Supplier<StellarBaseModule> moduleSupplier;

    private boolean isConnected = false;
    private boolean canProcess = false;
    private boolean isWorking = false;
    private Stage irisStage = Stage.EMPTY;

    private long maxEUt = 0;
    private long currentEUt = 0;
    private int effectiveParallel = 1;
    private int configuredParallel = 1;
    private int overclockTier = 0;
    private double speedBonus = 1.0;
    private int irisParallelLimit = 1;
    private boolean wirelessAvailable = false;
    private boolean powerFailure = false;
    private long configuredVoltage = 32;

    private PowerControlPopup powerPopup;
    private boolean showingPowerPopup = false;
    private BiConsumer<Integer, Long> onPowerSettingsChanged;

    public StellarModuleContentWidget(Supplier<StellarBaseModule> moduleSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.moduleSupplier = moduleSupplier;
        initPowerPopup();
    }

    private void initPowerPopup() {
        powerPopup = new PowerControlPopup(WIDTH + 4, 0, this::hidePowerPopup, this::onPowerSettingsApplied);
        addWidget(powerPopup);
    }

    public void setOnPowerSettingsChanged(BiConsumer<Integer, Long> callback) {
        this.onPowerSettingsChanged = callback;
    }

    private void showPowerPopup() {
        showingPowerPopup = true;
        powerPopup.show(configuredParallel, configuredVoltage);
    }

    private void hidePowerPopup() {
        showingPowerPopup = false;
        powerPopup.hide();
    }

    private void onPowerSettingsApplied(PowerControlPopup.PowerSettings settings) {
        this.configuredParallel = settings.maxParallel();
        this.configuredVoltage = settings.voltagePerParallel();

        if (onPowerSettingsChanged != null) {
            onPowerSettingsChanged.accept(configuredParallel, configuredVoltage);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        StellarBaseModule module = moduleSupplier.get();
        if (module == null) return;

        IStellarIrisProvider iris = module.getStellarIris();
        boolean newConnected = iris != null && iris.isFormed();
        boolean newCanProcess = newConnected && iris.canProcess();
        boolean newWorking = module.getRecipeLogic() != null && module.getRecipeLogic().isWorking();
        Stage newStage = iris != null ? iris.getStage() : Stage.EMPTY;

        long newMaxEUt = module.getMaxEUt();
        long newCurrentEUt = module.getEnergyConsumedPerTick();
        int newEffectiveParallel = module.getEffectiveParallelLimit();
        int newConfiguredParallel = module.getConfiguredMaxParallel();
        int newOverclockTier = module.getOverclockTier();
        double newSpeedBonus = (iris != null && iris.canProcess()) ? iris.getSpeedBonus() : 1.0;
        int newIrisLimit = module.getIrisParallelLimit();
        boolean newWirelessAvailable = module.isWirelessEnergyAvailable();
        boolean newPowerFailure = module.isPowerFailure();
        long newConfiguredVoltage = module.getConfiguredVoltagePerParallel();

        if (newConnected != isConnected || newCanProcess != canProcess || newWorking != isWorking ||
                newStage != irisStage || newMaxEUt != maxEUt || newCurrentEUt != currentEUt ||
                newEffectiveParallel != effectiveParallel || newConfiguredParallel != configuredParallel ||
                newOverclockTier != overclockTier || newSpeedBonus != speedBonus ||
                newIrisLimit != irisParallelLimit || newWirelessAvailable != wirelessAvailable ||
                newPowerFailure != powerFailure || newConfiguredVoltage != configuredVoltage) {

            isConnected = newConnected;
            canProcess = newCanProcess;
            isWorking = newWorking;
            irisStage = newStage;
            maxEUt = newMaxEUt;
            currentEUt = newCurrentEUt;
            effectiveParallel = newEffectiveParallel;
            configuredParallel = newConfiguredParallel;
            overclockTier = newOverclockTier;
            speedBonus = newSpeedBonus;
            irisParallelLimit = newIrisLimit;
            wirelessAvailable = newWirelessAvailable;
            powerFailure = newPowerFailure;
            configuredVoltage = newConfiguredVoltage;

            writeUpdateInfo(202, buf -> {
                buf.writeBoolean(isConnected);
                buf.writeBoolean(canProcess);
                buf.writeBoolean(isWorking);
                buf.writeEnum(irisStage);
                buf.writeLong(maxEUt);
                buf.writeLong(currentEUt);
                buf.writeInt(effectiveParallel);
                buf.writeInt(configuredParallel);
                buf.writeInt(overclockTier);
                buf.writeDouble(speedBonus);
                buf.writeInt(irisParallelLimit);
                buf.writeBoolean(wirelessAvailable);
                buf.writeBoolean(powerFailure);
                buf.writeLong(configuredVoltage);
            });
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        StellarBaseModule module = moduleSupplier.get();
        if (module != null) {
            IStellarIrisProvider iris = module.getStellarIris();
            buffer.writeBoolean(iris != null && iris.isFormed());
            buffer.writeBoolean(iris != null && iris.isFormed() && iris.canProcess());
            buffer.writeBoolean(module.getRecipeLogic() != null && module.getRecipeLogic().isWorking());
            buffer.writeEnum(iris != null ? iris.getStage() : Stage.EMPTY);
            buffer.writeLong(module.getMaxEUt());
            buffer.writeLong(module.getEnergyConsumedPerTick());
            buffer.writeInt(module.getEffectiveParallelLimit());
            buffer.writeInt(module.getConfiguredMaxParallel());
            buffer.writeInt(module.getOverclockTier());
            buffer.writeDouble((iris != null && iris.canProcess()) ? iris.getSpeedBonus() : 1.0);
            buffer.writeInt(module.getIrisParallelLimit());
            buffer.writeBoolean(module.isWirelessEnergyAvailable());
            buffer.writeBoolean(module.isPowerFailure());
            buffer.writeLong(module.getConfiguredVoltagePerParallel());
        } else {
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
            buffer.writeEnum(Stage.EMPTY);
            buffer.writeLong(0);
            buffer.writeLong(0);
            buffer.writeInt(1);
            buffer.writeInt(1);
            buffer.writeInt(0);
            buffer.writeDouble(1.0);
            buffer.writeInt(1);
            buffer.writeBoolean(false);
            buffer.writeBoolean(false);
            buffer.writeLong(32);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        isConnected = buffer.readBoolean();
        canProcess = buffer.readBoolean();
        isWorking = buffer.readBoolean();
        irisStage = buffer.readEnum(Stage.class);
        maxEUt = buffer.readLong();
        currentEUt = buffer.readLong();
        effectiveParallel = buffer.readInt();
        configuredParallel = buffer.readInt();
        overclockTier = buffer.readInt();
        speedBonus = buffer.readDouble();
        irisParallelLimit = buffer.readInt();
        wirelessAvailable = buffer.readBoolean();
        powerFailure = buffer.readBoolean();
        configuredVoltage = buffer.readLong();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 202) {
            isConnected = buffer.readBoolean();
            canProcess = buffer.readBoolean();
            isWorking = buffer.readBoolean();
            irisStage = buffer.readEnum(Stage.class);
            maxEUt = buffer.readLong();
            currentEUt = buffer.readLong();
            effectiveParallel = buffer.readInt();
            configuredParallel = buffer.readInt();
            overclockTier = buffer.readInt();
            speedBonus = buffer.readDouble();
            irisParallelLimit = buffer.readInt();
            wirelessAvailable = buffer.readBoolean();
            powerFailure = buffer.readBoolean();
            configuredVoltage = buffer.readLong();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = getPosition().x;
        int y = getPosition().y;
        int w = getSize().width;
        int h = getSize().height;

        DrawerHelper.drawGradientRect(graphics, x, y, w, h, 0xE00c0c14, 0xE0080810, false);

        int gridColor = 0x08FFFFFF;
        for (int gx = x + 16; gx < x + w; gx += 16) {
            graphics.fill(gx, y, gx + 1, y + h, gridColor);
        }
        for (int gy = y + 16; gy < y + h; gy += 16) {
            graphics.fill(x, gy, x + w, gy + 1, gridColor);
        }

        int accentColor = getAccentColor();
        DrawerHelper.drawBorder(graphics, x, y, w, h, accentColor & 0x60FFFFFF, 1);

        int cornerLen = 12;
        int cornerColor = accentColor & 0x80FFFFFF;
        graphics.fill(x, y, x + cornerLen, y + 2, cornerColor);
        graphics.fill(x, y, x + 2, y + cornerLen, cornerColor);
        graphics.fill(x + w - cornerLen, y, x + w, y + 2, cornerColor);
        graphics.fill(x + w - 2, y, x + w, y + cornerLen, cornerColor);

        drawContent(graphics, x + 6, y + 6, w - 12 - GEAR_BUTTON_SIZE - 4);
        drawGearButton(graphics, x + w - GEAR_BUTTON_SIZE - 4, y + h - GEAR_BUTTON_SIZE - 4, mouseX, mouseY);

        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawContent(GuiGraphics graphics, int x, int y, int contentWidth) {
        var font = Minecraft.getInstance().font;
        int labelColor = 0xFF808090;
        int valueColor = 0xFFDDDDDD;
        int accentColor = 0xFF80C0FF;

        int lineHeight = 11;
        int valueX = x + 70;
        int currentY = y;

        String statusValue;
        int statusColor;
        if (powerFailure) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.power_fail").getString();
            statusColor = 0xFFFF4444;
        } else if (!wirelessAvailable) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.no_wireless").getString();
            statusColor = 0xFFFF5555;
        } else if (isWorking) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.processing").getString();
            statusColor = 0xFF44FF44;
        } else if (isConnected && canProcess) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.ready").getString();
            statusColor = 0xFF6090CC;
        } else if (isConnected) {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.iris_inactive").getString();
            statusColor = 0xFFCC8844;
        } else {
            statusValue = Component.translatable("cosmiccore.stellar.module.status.disconnected").getString();
            statusColor = 0xFFFF5555;
        }

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.status").getString(), x, currentY,
                labelColor, false);
        graphics.drawString(font, statusValue, valueX, currentY, statusColor, false);
        currentY += lineHeight;

        int sepColor = 0x304080FF;
        graphics.fill(x, currentY, x + contentWidth, currentY + 1, sepColor);
        currentY += 4;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.max_eut").getString(), x, currentY,
                labelColor, false);
        String maxEUtStr = formatEnergy(maxEUt);
        graphics.drawString(font, maxEUtStr, valueX, currentY, valueColor, false);

        String tierName = overclockTier < GTValues.VNF.length ? GTValues.VNF[overclockTier] : "MAX";
        int tierColor = getTierColor(overclockTier);
        int badgeX = x + contentWidth - font.width(tierName) - 4;
        graphics.fill(badgeX - 2, currentY - 1, badgeX + font.width(tierName) + 2, currentY + font.lineHeight,
                0x90000000 | (tierColor & 0x00333333));
        graphics.drawString(font, tierName, badgeX, currentY, 0xFF000000 | tierColor, false);
        currentY += lineHeight;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.parallel").getString(), x, currentY,
                labelColor, false);
        String parallelStr = effectiveParallel + "x";
        if (effectiveParallel < configuredParallel) {
            parallelStr = Component
                    .translatable("cosmiccore.stellar.module.parallel_max", effectiveParallel, configuredParallel)
                    .getString();
        }
        graphics.drawString(font, parallelStr, valueX, currentY, accentColor, false);
        currentY += lineHeight;

        graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.current").getString(), x, currentY,
                labelColor, false);
        if (currentEUt > 0) {
            String currentEUStr = formatEnergy(currentEUt);
            graphics.drawString(font, currentEUStr, valueX, currentY, 0xFFFFCC44, false);
        } else {
            graphics.drawString(font, "---", valueX, currentY, 0x80606060, false);
        }
        currentY += lineHeight;

        graphics.fill(x, currentY, x + contentWidth, currentY + 1, sepColor);
        currentY += 4;

        if (isConnected && canProcess) {
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.speed_bonus").getString(), x,
                    currentY, labelColor, false);
            String speedStr = String.format("%.1fx", speedBonus);
            int speedColor = speedBonus > 1.0 ? 0xFF66FF66 : 0xFFCCCCCC;
            graphics.drawString(font, speedStr, valueX, currentY, speedColor, false);
            currentY += lineHeight;

            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.iris_limit").getString(), x,
                    currentY, labelColor, false);
            graphics.drawString(font, irisParallelLimit + "x", valueX, currentY, valueColor, false);
        } else if (isConnected) {
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.stage").getString(), x,
                    currentY, labelColor, false);
            int stageColor = getStageTextColor(irisStage);
            graphics.drawString(font, irisStage.toString(), valueX, currentY, stageColor, false);
            currentY += lineHeight;
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.waiting_iris").getString(), x,
                    currentY, 0x80AAAAAA, false);
        } else {
            graphics.drawString(font, Component.translatable("cosmiccore.stellar.module.not_linked").getString(), x,
                    currentY, 0x80808080, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawGearButton(GuiGraphics graphics, int btnX, int btnY, int mouseX, int mouseY) {
        boolean hovered = mouseX >= btnX && mouseX < btnX + GEAR_BUTTON_SIZE &&
                mouseY >= btnY && mouseY < btnY + GEAR_BUTTON_SIZE;

        int bgColor = hovered ? 0xC04080FF : 0x80404060;
        graphics.fill(btnX, btnY, btnX + GEAR_BUTTON_SIZE, btnY + GEAR_BUTTON_SIZE, bgColor);

        int borderColor = hovered ? 0xFF6090FF : 0xFF505070;
        DrawerHelper.drawBorder(graphics, btnX, btnY, GEAR_BUTTON_SIZE, GEAR_BUTTON_SIZE, borderColor, 1);

        int gearSize = GEAR_BUTTON_SIZE - 4;
        int gearX = btnX + 2;
        int gearY = btnY + 2;
        graphics.blit(GEAR_TEXTURE, gearX, gearY, 0, 0, gearSize, gearSize, gearSize, gearSize);
    }

    private int getGearButtonX() {
        return getPosition().x + WIDTH - GEAR_BUTTON_SIZE - 4;
    }

    private int getGearButtonY() {
        return getPosition().y + HEIGHT - GEAR_BUTTON_SIZE - 4;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showingPowerPopup && powerPopup.isVisible()) {
            if (powerPopup.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (!powerPopup.isMouseOverElement(mouseX, mouseY)) {
                hidePowerPopup();
                return true;
            }
        }

        int btnX = getGearButtonX();
        int btnY = getGearButtonY();
        if (mouseX >= btnX && mouseX < btnX + GEAR_BUTTON_SIZE &&
                mouseY >= btnY && mouseY < btnY + GEAR_BUTTON_SIZE) {
            if (showingPowerPopup) {
                hidePowerPopup();
            } else {
                showPowerPopup();
            }
            playButtonClickSound();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG EU/t", eu / 1_000_000_000.0);
        if (eu >= 1_000_000) return String.format("%.1fM EU/t", eu / 1_000_000.0);
        if (eu >= 1000) return String.format("%.1fk EU/t", eu / 1000.0);
        return String.format("%d EU/t", eu);
    }

    private int getTierColor(int tier) {
        return switch (tier) {
            case 0 -> 0x808080;
            case 1 -> 0xC0C0C0;
            case 2 -> 0x00FFFF;
            case 3 -> 0xFFFF00;
            case 4 -> 0x0080FF;
            case 5 -> 0x8000FF;
            case 6 -> 0xFF0080;
            case 7 -> 0xFF00FF;
            case 8 -> 0x00FF00;
            default -> 0xFF4040;
        };
    }

    private int getStageTextColor(Stage stage) {
        return 0xFF000000 | switch (stage) {
            case EMPTY -> 0x606060;
            case GROWING -> 0x66AAFF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF8844;
            case BLACK_HOLE -> 0xAA66FF;
            case DEATH -> 0xFF4444;
            case DEATH_GRACEFUL -> 0x886666;
        };
    }

    private int getAccentColor() {
        if (isConnected && canProcess) {
            return getStageAccentColor(irisStage);
        }
        return 0xFF4080AA;
    }

    private int getStageAccentColor(Stage stage) {
        return 0xFF000000 | switch (stage) {
            case EMPTY -> 0x404060;
            case GROWING -> 0x6080FF;
            case STAR -> 0xFFCC44;
            case SUPERSTAR -> 0xFF8844;
            case BLACK_HOLE -> 0x8040FF;
            case DEATH -> 0xFF2020;
            case DEATH_GRACEFUL -> 0x804040;
        };
    }
}
