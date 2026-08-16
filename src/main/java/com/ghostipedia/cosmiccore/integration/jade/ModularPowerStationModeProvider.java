package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.ModularPowerStationMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ModularPowerStationModeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String MODE = "CosmicCoreModularPowerStationMode";

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.MODULAR_POWER_STATION_MODE;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return getMachine(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        ModularPowerStationMachine machine = getMachine(accessor);
        if (machine == null || !machine.isAssemblyReady()) return;
        ModularPowerStationMachine.DriveType drive = machine.getDriveType();
        if (drive != ModularPowerStationMachine.DriveType.NONE) {
            data.putInt(MODE, drive.ordinal());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(MODE, Tag.TAG_INT)) return;
        ModularPowerStationMachine.DriveType drive = ModularPowerStationMachine.DriveType.byOrdinal(data.getInt(MODE));
        String modeKey = switch (drive) {
            case TURBINE -> "cosmiccore.jade.modular_power_station.mode.turbine";
            case COMBUSTION -> "cosmiccore.jade.modular_power_station.mode.combustion";
            default -> null;
        };
        if (modeKey != null) {
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.modular_power_station.mode",
                    Component.translatable(modeKey).withStyle(ChatFormatting.AQUA)));
        }
    }

    private static ModularPowerStationMachine getMachine(BlockAccessor accessor) {
        MetaMachine machine = MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
        return machine instanceof ModularPowerStationMachine station ? station : null;
    }
}
