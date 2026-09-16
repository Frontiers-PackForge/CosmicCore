package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.api.machine.multiblock.DroneStationMachine;
import com.ghostipedia.cosmiccore.api.misc.DroneStationServiceLogic.DroneTier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

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

public enum DroneStationProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String DATA = "CosmicCoreDroneStation";
    private static final String ONLINE = "Online";
    private static final String TIER = "Tier";
    private static final String USES = "Uses";
    private static final String CONNECTIONS = "Connections";

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.DRONE_STATION;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return getMachine(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        DroneStationMachine station = getMachine(accessor);
        if (station == null) return;
        CompoundTag telemetry = new CompoundTag();
        telemetry.putBoolean(ONLINE, station.isOnline());
        DroneTier tier = station.getActiveTier();
        telemetry.putInt(TIER, tier == null ? -1 : tier.ordinal());
        telemetry.putInt(USES, station.getActiveRemainingUses());
        telemetry.putInt(CONNECTIONS, station.getConnectedMachineCount());
        data.put(DATA, telemetry);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(DATA, Tag.TAG_COMPOUND)) return;
        CompoundTag telemetry = data.getCompound(DATA);
        boolean online = telemetry.getBoolean(ONLINE);
        tooltip.add(Component.translatable(online ?
                "cosmiccore.multiblock.drone_station.online" : "cosmiccore.multiblock.drone_station.offline")
                .withStyle(online ? ChatFormatting.GREEN : ChatFormatting.RED));
        int tierOrdinal = telemetry.getInt(TIER);
        if (tierOrdinal >= 0 && tierOrdinal < DroneTier.values().length) {
            DroneTier tier = DroneTier.values()[tierOrdinal];
            tooltip.add(Component.translatable("cosmiccore.multiblock.drone_station.tier_status",
                    Component.translatable(tier.translationKey()), FormattingUtil.formatNumbers(tier.range()),
                    FormattingUtil.formatNumbers(tier.voltage()), telemetry.getInt(USES)));
        }
        tooltip.add(Component.translatable("cosmiccore.multiblock.drone_station.connections",
                telemetry.getInt(CONNECTIONS)));
    }

    private static DroneStationMachine getMachine(BlockAccessor accessor) {
        MetaMachine machine = MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
        return machine instanceof DroneStationMachine station ? station : null;
    }
}
