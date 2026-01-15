package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine.Stage;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;
import com.ghostipedia.cosmiccore.client.gui.screen.StellarConvergenceScreen;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class StellarIrisWidget extends WidgetGroup {

    public static final int WIDTH = 310;
    public static final int HEIGHT = 160;

    private final Supplier<IrisMultiblockMachine> machineSupplier;

    private Stage lastSyncedStage = Stage.EMPTY;
    private boolean hasReceivedInitialSync = false;
    private float fuelLevel = 0f;
    private boolean canIgnite = false;
    private int lastSyncedStarColor = -1;

    private StellarCoreWidget coreWidget;
    private OrbitalRingsWidget orbitalRings;
    private ModuleSelectorWidget moduleSelectorWidget;
    private ModuleToggleButton moduleToggleButton;
    private ModuleConfigPopout moduleConfigPopout;
    private StageContextPanel contextPanel;
    private StarColorButton starColorButton;
    private StarColorPickerPopup starColorPicker;
    private boolean showingColorPicker = false;

    private PrestigeAnimationOverlay prestigeAnimationOverlay;
    private PrestigeWindow prestigeWindow;
    private UpgradeTreeWidget upgradeTreeWidget;
    private UpgradeTreeButton upgradeTreeButton;
    private boolean prestigeAnimationTriggered = false;
    private Stage stageBeforePrestige = Stage.EMPTY;

    private int tickCounter = 0;
    private boolean debugPrimed = false;
    private boolean showingModuleSelector = false;

    private int selectedModuleIndex = -1;
    private String lastSyncedModuleName = "";
    private boolean lastSyncedModuleConnected = false;
    private boolean lastSyncedModuleWorking = false;
    private long lastSyncedModuleEnergy = 0;
    private double lastSyncedModuleSpeed = 0;
    private Stage lastSyncedModuleStage = Stage.EMPTY;
    private int lastSyncedModuleParallel = 0;
    private long lastSyncedModuleVoltage = 0;
    private int lastSyncedIrisParallelLimit = 0;
    private long lastSyncedMaxEUt = 0;
    private int lastSyncedEffectiveParallel = 0;
    private int lastSyncedOverclockTier = 0;

    public StellarIrisWidget(Supplier<IrisMultiblockMachine> machineSupplier) {
        super(0, 0, WIDTH, HEIGHT);
        this.machineSupplier = machineSupplier;
        initWidgets();
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            Stage serverStage = machine.getStage();
            // Debug: log what stage the SERVER is sending
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                    "[StellarIrisWidget] SERVER writeInitialData: stage={}, color={}",
                    serverStage, machine.getCustomStarColor());
            buffer.writeEnum(serverStage);
            buffer.writeInt(machine.getCustomStarColor());
        } else {
            buffer.writeEnum(Stage.EMPTY);
            buffer.writeInt(-1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        lastSyncedStage = buffer.readEnum(Stage.class);
        lastSyncedStarColor = buffer.readInt();
        hasReceivedInitialSync = true;
        // Debug: log what stage the CLIENT received
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                "[StellarIrisWidget] CLIENT readInitialData: stage={}, color={}",
                lastSyncedStage, lastSyncedStarColor);
    }

    private void initWidgets() {
        // Debug: verify the stage supplier returns EMPTY before sync
        // Version marker: V3 - 2026-01-13
        Stage initialStage = getCurrentStage();
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                "[StellarIrisWidget V3] initWidgets: getCurrentStage()={}, hasReceivedInitialSync={}",
                initialStage, hasReceivedInitialSync);

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

        orbitalRings = new OrbitalRingsWidget(coreX - 5, coreY - 5, coreSize + 10, coreSize + 10,
                this::getCurrentStage);
        addWidget(orbitalRings);

        coreWidget = new StellarCoreWidget(coreX, coreY, coreSize, this::getCurrentStage, this::getCurrentStarColor);
        addWidget(coreWidget);

        int moduleSelectorSize = HEIGHT - margin * 2;
        int moduleSelectorX = (WIDTH - moduleSelectorSize) / 2;
        int moduleSelectorY = margin;
        moduleSelectorWidget = new ModuleSelectorWidget(moduleSelectorX, moduleSelectorY, moduleSelectorSize,
                machineSupplier, this::onModuleSelected);
        moduleSelectorWidget.setVisible(false);
        moduleSelectorWidget.setActive(false);
        addWidget(moduleSelectorWidget);

        contextPanel = new StageContextPanel(panelX, panelY, panelWidth, panelH, machineSupplier, this);
        addWidget(contextPanel);

        int popoutX = moduleSelectorX + moduleSelectorSize + 10;
        int popoutY = moduleSelectorY + 20;
        moduleConfigPopout = new ModuleConfigPopout(popoutX, popoutY, machineSupplier, this::onModulePopoutClosed);
        moduleConfigPopout.setOnPowerSettingsChanged(this::onPowerSettingsChanged);
        addWidget(moduleConfigPopout);

        initDebugButtons();
        initModuleToggle();
        initStarColorButton();
        initPrestigeWidgets();
        initUpgradeTreeWidgets();
    }

    private void initPrestigeWidgets() {
        int windowW = 200;
        int windowH = 160;
        int windowX = (WIDTH - windowW) / 2;
        int windowY = (HEIGHT - windowH) / 2;

        prestigeWindow = new PrestigeWindow(windowX, windowY, windowW, windowH,
                machineSupplier, this::onPrestigeWindowClosed);
        addWidget(prestigeWindow);

        prestigeAnimationOverlay = new PrestigeAnimationOverlay(0, 0, WIDTH, HEIGHT,
                machineSupplier, this::onPrestigeAnimationComplete, this::onShowPrestigeWindow);
        prestigeAnimationOverlay.setCoreWidget(coreWidget);
        addWidget(prestigeAnimationOverlay);
    }

    private void initUpgradeTreeWidgets() {
        // Upgrade tree button - positioned next to other buttons
        int btnSize = 18;
        int btnX = 5 + 18 + 4 + 18 + 4; // After module toggle and color button
        int btnY = HEIGHT - btnSize - 5;

        upgradeTreeButton = new UpgradeTreeButton(btnX, btnY, btnSize, btnSize,
                this::onUpgradeTreeButtonClicked, machineSupplier);
        addWidget(upgradeTreeButton);

        // Note: Widget is no longer used - we open a full screen instead
        upgradeTreeWidget = null;
    }

    @OnlyIn(Dist.CLIENT)
    private void onUpgradeTreeButtonClicked(boolean ignored) {
        // Open the full-screen upgrade tree
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            StellarConvergenceScreen.open(machine);
        }
    }

    private void initDebugButtons() {
        int btnWidth = 50;
        int btnHeight = 14;
        int btnX = 5;
        int btnY = HEIGHT - btnHeight - 5 - 20;

        addWidget(new DebugPrimeButton(btnX, btnY, btnWidth, btnHeight, this::requestDebugPrime));
    }

    private void initModuleToggle() {
        int btnSize = 18;
        int btnX = 5;
        int btnY = HEIGHT - btnSize - 5;

        moduleToggleButton = new ModuleToggleButton(btnX, btnY, btnSize, btnSize, this::onModuleToggle,
                this::getCurrentStage);
        addWidget(moduleToggleButton);
    }

    private void initStarColorButton() {
        int btnSize = 18;
        int btnX = 5 + 18 + 4;
        int btnY = HEIGHT - btnSize - 5;

        starColorButton = new StarColorButton(btnX, btnY, btnSize, btnSize, this::onColorButtonClicked,
                this::getCurrentStarColor);
        addWidget(starColorButton);

        int pickerX = btnX;
        int pickerY = btnY - StarColorPickerPopup.HEIGHT - 5;
        starColorPicker = new StarColorPickerPopup(pickerX, pickerY, this::hideColorPicker, this::onStarColorChanged);
        addWidget(starColorPicker);
    }

    private void onColorButtonClicked(boolean ignored) {
        if (showingColorPicker) {
            hideColorPicker();
        } else {
            showColorPicker();
        }
    }

    private void showColorPicker() {
        showingColorPicker = true;
        starColorPicker.show(lastSyncedStarColor);
    }

    private void hideColorPicker() {
        showingColorPicker = false;
        starColorPicker.hide();
    }

    private void onStarColorChanged(int newColor) {
        writeClientAction(6, buf -> buf.writeInt(newColor));
    }

    public int getCurrentStarColor() {
        return lastSyncedStarColor;
    }

    private void onModuleToggle(boolean showModules) {
        showingModuleSelector = showModules;

        coreWidget.setVisible(!showModules);
        coreWidget.setActive(!showModules);
        orbitalRings.setVisible(!showModules);
        orbitalRings.setActive(!showModules);
        contextPanel.setVisible(!showModules);
        contextPanel.setActive(!showModules);

        moduleSelectorWidget.setVisible(showModules);
        moduleSelectorWidget.setActive(showModules);

        if (!showModules) {
            moduleConfigPopout.hide();
            moduleSelectorWidget.clearSelection();
            writeClientAction(4, buf -> buf.writeInt(-1));
        }
    }

    private void onModuleSelected(int moduleIndex) {
        this.selectedModuleIndex = moduleIndex;
        moduleConfigPopout.showForModule(moduleIndex);
        writeClientAction(4, buf -> buf.writeInt(moduleIndex));
    }

    private void onModulePopoutClosed() {
        moduleSelectorWidget.clearSelection();
    }

    private void onPowerSettingsChanged(int maxParallel, long voltagePerParallel) {
        if (selectedModuleIndex < 0) return;

        writeClientAction(5, buf -> {
            buf.writeInt(selectedModuleIndex);
            buf.writeInt(maxParallel);
            buf.writeLong(voltagePerParallel);
        });

        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                "[StellarIrisWidget] CLIENT sending power settings: module={}, parallel={}, voltage={}",
                selectedModuleIndex, maxParallel, voltagePerParallel);
    }

    public void requestDebugPrime() {
        writeClientAction(3, buf -> {});
    }

    public Stage getCurrentStage() {
        // Before we receive initial sync, return EMPTY as safe default
        // The client-side machine may have corrupted stage data before sync completes
        if (!hasReceivedInitialSync) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER
                    .warn("[StellarIrisWidget] getCurrentStage: No initial sync yet, returning EMPTY");
            return Stage.EMPTY;
        }
        // Safety check: if lastSyncedStage is somehow null, return EMPTY
        if (lastSyncedStage == null) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER
                    .warn("[StellarIrisWidget] getCurrentStage: lastSyncedStage is null, returning EMPTY");
            return Stage.EMPTY;
        }
        // Debug: log non-empty/non-star stages
        if (lastSyncedStage != Stage.EMPTY && lastSyncedStage != Stage.STAR) {
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn("[StellarIrisWidget] getCurrentStage: returning {}",
                    lastSyncedStage);
        }
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
            writeUpdateInfo(301, buf -> buf.writeEnum(currentStage));
        }

        float newFuelLevel = calculateFuelLevel(machine);
        if (Math.abs(newFuelLevel - fuelLevel) > 0.01f) {
            fuelLevel = newFuelLevel;
            writeUpdateInfo(302, buf -> buf.writeFloat(fuelLevel));
        }

        boolean newCanIgnite = checkIgnitionRequirements(machine);
        if (newCanIgnite != canIgnite) {
            canIgnite = newCanIgnite;
            writeUpdateInfo(303, buf -> buf.writeBoolean(canIgnite));
        }

        int newStarColor = machine.getCustomStarColor();
        if (newStarColor != lastSyncedStarColor) {
            lastSyncedStarColor = newStarColor;
            writeUpdateInfo(306, buf -> buf.writeInt(newStarColor));
        }

        syncSelectedModuleData(machine);
    }

    private void syncSelectedModuleData(IrisMultiblockMachine machine) {
        if (selectedModuleIndex < 0) return;

        List<IStellarModuleReceiver> modules = new ArrayList<>(machine.getConnectedModules());
        if (selectedModuleIndex >= modules.size()) {
            selectedModuleIndex = -1;
            writeUpdateInfo(305, buf -> {});
            return;
        }

        IStellarModuleReceiver receiver = modules.get(selectedModuleIndex);

        String newName = "";
        boolean newConnected = false;
        boolean newWorking = false;
        long newEnergy = 0;
        double newSpeed = 0;
        Stage newStage = Stage.EMPTY;
        int newParallel = 1;
        long newVoltage = 32;
        int newIrisLimit = 1;
        long newMaxEUt = 0;
        int newEffectiveParallel = 1;
        int newOverclockTier = 0;

        if (receiver instanceof StellarBaseModule module) {
            newName = module.getBlockState().getBlock().getDescriptionId();
            IStellarIrisProvider iris = module.getStellarIris();
            if (iris == null) {
                iris = machine; // Use Iris as fallback
            }

            newConnected = iris != null && iris.isFormed();
            newWorking = module.getRecipeLogic().isWorking();
            newEnergy = module.getEnergyConsumedPerTick();

            newParallel = module.getConfiguredMaxParallel();
            newVoltage = module.getConfiguredVoltagePerParallel();
            newIrisLimit = module.getIrisParallelLimit();

            newMaxEUt = module.getMaxEUt();
            newEffectiveParallel = module.getEffectiveParallelLimit();
            newOverclockTier = module.getOverclockTier();

            if (iris != null && iris.canProcess()) {
                newSpeed = iris.getSpeedBonus();
                newStage = iris.getStage();
            }
        }

        boolean changed = !newName.equals(lastSyncedModuleName) || newConnected != lastSyncedModuleConnected ||
                newWorking != lastSyncedModuleWorking || newEnergy != lastSyncedModuleEnergy ||
                newSpeed != lastSyncedModuleSpeed || newStage != lastSyncedModuleStage ||
                newParallel != lastSyncedModuleParallel || newVoltage != lastSyncedModuleVoltage ||
                newIrisLimit != lastSyncedIrisParallelLimit || newMaxEUt != lastSyncedMaxEUt ||
                newEffectiveParallel != lastSyncedEffectiveParallel || newOverclockTier != lastSyncedOverclockTier;

        if (changed) {
            lastSyncedModuleName = newName;
            lastSyncedModuleConnected = newConnected;
            lastSyncedModuleWorking = newWorking;
            lastSyncedModuleEnergy = newEnergy;
            lastSyncedModuleSpeed = newSpeed;
            lastSyncedModuleStage = newStage;
            lastSyncedModuleParallel = newParallel;
            lastSyncedModuleVoltage = newVoltage;
            lastSyncedIrisParallelLimit = newIrisLimit;
            lastSyncedMaxEUt = newMaxEUt;
            lastSyncedEffectiveParallel = newEffectiveParallel;
            lastSyncedOverclockTier = newOverclockTier;

            final String syncName = newName;
            final boolean syncConnected = newConnected;
            final boolean syncWorking = newWorking;
            final long syncEnergy = newEnergy;
            final double syncSpeed = newSpeed;
            final Stage syncStage = newStage;
            final int syncParallel = newParallel;
            final long syncVoltage = newVoltage;
            final int syncIrisLimit = newIrisLimit;
            final long syncMaxEUt = newMaxEUt;
            final int syncEffectiveParallel = newEffectiveParallel;
            final int syncOverclockTier = newOverclockTier;

            writeUpdateInfo(304, buf -> {
                buf.writeUtf(syncName);
                buf.writeBoolean(syncConnected);
                buf.writeBoolean(syncWorking);
                buf.writeLong(syncEnergy);
                buf.writeDouble(syncSpeed);
                buf.writeEnum(syncStage);
                buf.writeInt(syncParallel);
                buf.writeLong(syncVoltage);
                buf.writeInt(syncIrisLimit);
                buf.writeLong(syncMaxEUt);
                buf.writeInt(syncEffectiveParallel);
                buf.writeInt(syncOverclockTier);
            });

            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] Syncing module data: name={}, connected={}, parallel={}, voltage={}, maxEUt={}, tier={}",
                    syncName, syncConnected, syncParallel, syncVoltage, syncMaxEUt, syncOverclockTier);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        // Debug: log ALL update info calls to trace unexpected stage changes
        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                "[StellarIrisWidget] readUpdateInfo: id={}, buffer remaining={}",
                id, buffer.readableBytes());

        if (id == 301) {
            Stage newStage = buffer.readEnum(Stage.class);
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.warn(
                    "[StellarIrisWidget] readUpdateInfo(301): stage change {} -> {}",
                    lastSyncedStage, newStage);
            lastSyncedStage = newStage;
        } else if (id == 302) {
            fuelLevel = buffer.readFloat();
        } else if (id == 303) {
            canIgnite = buffer.readBoolean();
        } else if (id == 304) {
            String name = buffer.readUtf();
            boolean connected = buffer.readBoolean();
            boolean working = buffer.readBoolean();
            long energy = buffer.readLong();
            double speed = buffer.readDouble();
            Stage stage = buffer.readEnum(Stage.class);
            int parallel = buffer.readInt();
            long voltage = buffer.readLong();
            int irisLimit = buffer.readInt();
            long maxEUt = buffer.readLong();
            int effectiveParallel = buffer.readInt();
            int overclockTier = buffer.readInt();

            // Update popout with synced data
            moduleConfigPopout.updateModuleData(name, connected, working, energy, speed, stage,
                    parallel, voltage, irisLimit, maxEUt, effectiveParallel, overclockTier);

            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] CLIENT readUpdateInfo: module name={}, parallel={}, voltage={}, maxEUt={}, tier={}",
                    name, parallel, voltage, maxEUt, overclockTier);
        } else if (id == 305) {
            moduleConfigPopout.hide();
            moduleSelectorWidget.clearSelection();
        } else if (id == 306) {
            lastSyncedStarColor = buffer.readInt();
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

    public void triggerPrestigeAnimation() {
        if (prestigeAnimationTriggered) return;

        prestigeAnimationTriggered = true;
        stageBeforePrestige = lastSyncedStage;

        prestigeAnimationOverlay.startAnimation(stageBeforePrestige, lastSyncedStarColor, 50);
        writeClientAction(7, buf -> {});

        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                "[StellarIrisWidget] Prestige animation triggered! Stage before: {}", stageBeforePrestige);
    }

    private void onPrestigeAnimationComplete() {
        writeClientAction(8, buf -> {});

        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                "[StellarIrisWidget] Prestige animation complete, requesting completion from server");
    }

    private void onShowPrestigeWindow() {
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            int earned = machine.getLastPrestigePointsEarned();
            int total = machine.getSpendablePoints() + earned;
            int tier = machine.getPrestigeTier();
            int prevTier = tier;

            prestigeWindow.show(earned, total, tier, prevTier);
        } else {
            prestigeWindow.show(50, 50, 1, 0);
        }
    }

    private void onPrestigeWindowClosed() {
        prestigeAnimationTriggered = false;
        stageBeforePrestige = Stage.EMPTY;

        com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                "[StellarIrisWidget] Prestige window closed");
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
        } else if (id == 4) {
            int newSelectedModule = buffer.readInt();
            this.selectedModuleIndex = newSelectedModule;
            this.lastSyncedModuleName = "";
            this.lastSyncedModuleConnected = false;
            this.lastSyncedModuleWorking = false;
            this.lastSyncedModuleEnergy = -1;
            this.lastSyncedModuleSpeed = -1;
            this.lastSyncedModuleStage = null;
            this.lastSyncedModuleParallel = -1;
            this.lastSyncedModuleVoltage = -1;
            this.lastSyncedIrisParallelLimit = -1;
            this.lastSyncedMaxEUt = -1;
            this.lastSyncedEffectiveParallel = -1;
            this.lastSyncedOverclockTier = -1;
            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] handleClientAction: module selection = {}", newSelectedModule);
        } else if (id == 5) {
            int moduleIndex = buffer.readInt();
            int newParallel = buffer.readInt();
            long newVoltage = buffer.readLong();

            List<IStellarModuleReceiver> modules = new ArrayList<>(machine.getConnectedModules());
            if (moduleIndex >= 0 && moduleIndex < modules.size()) {
                IStellarModuleReceiver receiver = modules.get(moduleIndex);
                if (receiver instanceof StellarBaseModule stellarModule) {
                    stellarModule.setConfiguredMaxParallel(newParallel);
                    stellarModule.setConfiguredVoltagePerParallel(newVoltage);

                    stellarModule.markDirty();

                    this.lastSyncedModuleParallel = -1;
                    this.lastSyncedModuleVoltage = -1;

                    com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                            "[StellarIrisWidget] SERVER updated module {} power: parallel={}, voltage={}",
                            moduleIndex, newParallel, newVoltage);
                }
            }
        } else if (id == 6) {
            int newColor = buffer.readInt();
            machine.setCustomStarColor(newColor);
            machine.markDirty();
            lastSyncedStarColor = newColor - 1;

            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] SERVER updated star color: {}",
                    newColor == -1 ? "default" : String.format("#%06X", newColor));
        } else if (id == 7) {
            machine.triggerPrestige();

            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] SERVER prestige triggered");
        } else if (id == 8) {
            machine.completePrestige();

            com.ghostipedia.cosmiccore.CosmicCore.LOGGER.info(
                    "[StellarIrisWidget] SERVER prestige completed. Points: {}, Tier: {}",
                    machine.getSpendablePoints(), machine.getPrestigeTier());
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
