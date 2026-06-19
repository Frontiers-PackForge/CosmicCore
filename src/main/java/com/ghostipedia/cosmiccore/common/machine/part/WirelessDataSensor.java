package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyCapacitor;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class WirelessDataSensor extends SensorPartMachine {

    private static Level serverLevel;
    private static UUID playerUUID;
    private static UUID wirelessUUID;



    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @Persisted
    public long minValue, maxValue;

    @Persisted
    private boolean usePercent;

    @Persisted
    @DescSynced
    private boolean isInverted;

    @Persisted
    @DescSynced
    private int signal;

    private LongInputWidget minValueInput;
    private LongInputWidget maxValueInput;

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    public boolean isUsePercent() {
        return usePercent;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public void setInverted(boolean inverted) {
        this.isInverted = inverted;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public WirelessDataSensor(BlockEntityCreationInfo holder) {
        super(holder, GTValues.EV);
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
    public void updateSignal() {
        super.updateSignal();
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        if (getControllers().isEmpty()) return false;
        return side == getFrontFacing();
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 105);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_energy_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 25,
                List.of(LocalizationUtils.format("cover.advanced_energy_detector.max"))));

        minValueInput = new LongInputWidget(40, 50, 176 - 40 - 10, 20, this::getMinValue, this::setMinValue);
        maxValueInput = new LongInputWidget(40, 75, 176 - 40 - 10, 20, this::getMaxValue, this::setMaxValue);
        minValueInput.setMin(0L).setMax(100L);
        maxValueInput.setMin(0L).setMax(100L);
        group.addWidget(minValueInput);
        group.addWidget(maxValueInput);

        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted)
                .isMultiLang()
                .setTooltipText("cover.advanced_energy_detector.invert"));
        return group;
    }
}
