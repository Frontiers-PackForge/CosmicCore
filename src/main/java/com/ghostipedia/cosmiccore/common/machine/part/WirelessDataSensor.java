package com.ghostipedia.cosmiccore.common.machine.part;

import com.ghostipedia.cosmiccore.api.data.wireless.WirelessEnergySavedData;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyCapacitor;
import com.ghostipedia.cosmiccore.api.machine.multiblock.DimensionalEnergyInterface;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.LongInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.cover.detector.AdvancedEnergyDetectorCover;
import com.gregtechceu.gtceu.common.cover.detector.DetectorCover;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;
import com.gregtechceu.gtceu.common.machine.owner.FTBOwner;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static com.gregtechceu.gtceu.api.capability.GTCapabilityHelper.getEnergyInfoProvider;

public class WirelessDataSensor extends SensorPartMachine{
    private static Level serverLevel;
    private static UUID playerUUID;
    private static UUID wirelessUUID;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessDataSensor.class, SensorPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private static final int DEFAULT_MIN_PERCENT = 33;
    private static final int DEFAULT_MAX_PERCENT = 66;

    @Persisted
    @Getter
    @Setter
    public long minValue, maxValue;

    @Persisted
    @Getter
    private boolean usePercent;

    @Persisted
    @DescSynced
    @Getter
    @Setter
    private boolean isInverted;

    private LongInputWidget minValueInput;
    private LongInputWidget maxValueInput;

    public WirelessDataSensor(IMachineBlockEntity holder) {
        super(holder, GTValues.EV);
        this.minValue = DEFAULT_MIN_PERCENT;
        this.maxValue = DEFAULT_MAX_PERCENT;
        this.usePercent = true;
    }


    public static void setOwner(Player player) {
        playerUUID = player.getUUID();
        var team = ((FTBOwner) MachineOwner.getOwner(playerUUID)).getTeam();
        wirelessUUID = team != null ? team.getTeamId() : playerUUID;
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        Minecraft mc = Minecraft.getInstance();
        var wirelessData = WirelessEnergySavedData.getOrCreate((ServerLevel) serverLevel);
        var percentStorage = (wirelessData.getEnergyStored(wirelessUUID).multiply(BigInteger.valueOf(10000))
                .divide(wirelessData.getEnergyCapacity(wirelessUUID)).intValue() / 100.0F);
        if (side == getFrontFacing()) {
            var controllerPSS = getControllers().stream().filter(DimensionalEnergyCapacitor.class::isInstance)
                    .map(DimensionalEnergyCapacitor.class::cast)
                    .toList();
            if (controllerPSS.isEmpty()) {
                return 0;
                //Assert that we're always working with big int.
            } else {
               var controller = controllerPSS.get(0);
                //If the PSS has too much energy, send a signal
               if (maxValue <= percentStorage) {
                   return isInverted() ? 0 : 15;
               }
               //If the PSS has too little energy, disable the signal.
               if (minValue >= percentStorage) {
                   return isInverted() ? 15 : 0;
               }
            }





        }



        return super.getOutputSignal(side);
    }




}
