package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyCapacitor;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;

public class WirelessDataSensor extends SensorPartMachine {

    private static Level serverLevel;
    private static UUID playerUUID;
    private static UUID wirelessUUID;
    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @SaveField
    @Getter
    @Setter
    public long minValue, maxValue;

    @SaveField
    @Getter
    private boolean usePercent;

    @SaveField
    @SyncToClient
    @Getter
    @Setter
    private boolean isInverted;

    @SaveField
    @SyncToClient
    @Getter
    @Setter
    private int signal;

    public WirelessDataSensor(BlockEntityCreationInfo info) {
        super(info, GTValues.EV);
        this.minValue = DEFAULT_MIN_PERCENT;
        this.maxValue = DEFAULT_MAX_PERCENT;
        this.usePercent = true;
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (serverLevel == null && !getLevel().isClientSide()) {
            serverLevel = getLevel();
        }
        // Get the UUID appended to 'us' (the machine)
        if (wirelessUUID == null) {
            var owner = this.getOwner();
            if (owner == null) return 0;
            var team = ((FTBOwner) MachineOwner.getOwner(owner.getPlayerUUID())).getTeam();
            wirelessUUID = team != null ? team.getTeamId() : playerUUID;
        }
        if (side == getFrontFacing().getOpposite()) {
            // Wireless Data collection
            var controllerPSS = getControllers().stream().filter(DimensionalEnergyCapacitor.class::isInstance)
                    .map(DimensionalEnergyCapacitor.class::cast)
                    .toList();
            if (controllerPSS.isEmpty()) {
                signal = 0;
                return signal;
            }
            var wirelessData = WirelessEnergySavedData.getOrCreate((ServerLevel) serverLevel);
            var percentStorage = (wirelessData.getEnergyStored(wirelessUUID).multiply(BigInteger.valueOf(10000))
                    .divide(wirelessData.getEnergyCapacity(wirelessUUID)).intValue() / 100.0F);
            var controller = controllerPSS.get(0);
            // If the PSS has too much energy, send a signal
            if (maxValue <= percentStorage) {
                return signal = isInverted() ? 0 : 15;
            }
            // If the PSS has too little energy, disable the signal.
            if (minValue >= percentStorage) {
                return signal = isInverted() ? 15 : 0;
            }
        }
        return signal;
    }

    @Override
    protected void updateSignal() {
        // No-op - signal is computed on demand in getOutputSignal
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        if (getControllers().isEmpty()) return false;
        return side == getFrontFacing();
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = ModularPanel.defaultPanel(getDefinition().getId().getPath(), 176, 120);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176));

        panel.child(Flow.column()
                .coverChildren()
                .padding(8)
                .top(14)
                .left(8)
                .childPadding(4)
                .child(new TextWidget<>(Text.lang("cover.advanced_energy_detector.label")))
                .child(new ToggleButton()
                        .value(new BoolValue.Dynamic(this::isInverted, this::setInverted))
                        .background(false, GTGuiTextures.BUTTON_REDSTONE_OFF)
                        .background(true, GTGuiTextures.BUTTON_REDSTONE_ON)
                        .tooltipAutoUpdate(true)
                        .tooltipBuilder(t -> t.addLine(Text.lang("cover.advanced_energy_detector.invert"))))
                .child(Flow.row().coverChildren().childPadding(4)
                        .child(new TextWidget<>(Text.lang("cover.advanced_energy_detector.min")))
                        .child(new TextFieldWidget()
                                .setNumbersLong(() -> 0L, () -> 100L)
                                .value(new LongSyncValue(this::getMinValue, this::setMinValue))
                                .size(100, 18)))
                .child(Flow.row().coverChildren().childPadding(4)
                        .child(new TextWidget<>(Text.lang("cover.advanced_energy_detector.max")))
                        .child(new TextFieldWidget()
                                .setNumbersLong(() -> 0L, () -> 100L)
                                .value(new LongSyncValue(this::getMaxValue, this::setMaxValue))
                                .size(100, 18))));

        return panel;
    }
}
