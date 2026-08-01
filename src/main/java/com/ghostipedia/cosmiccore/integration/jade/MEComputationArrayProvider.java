package com.ghostipedia.cosmiccore.integration.jade;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MEComputationArrayTuning;

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

public enum MEComputationArrayProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String DATA = "MEComputationArrayData";
    private static final String CURRENT_CWUT = "CurrentCwut";
    private static final String MAXIMUM_CWUT = "MaximumCwut";
    private static final String EU_DEMAND = "EuDemand";
    private static final String RELAY_EUT = "RelayEut";
    private static final String CORES = "Cores";
    private static final String RELAYS = "Relays";
    private static final String STORED_POWER_EU = "StoredPowerEu";
    private static final String MAXIMUM_STORED_POWER_EU = "MaximumStoredPowerEu";
    private static final String UPLINK_ONLINE = "UplinkOnline";

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.ME_COMPUTATION_ARRAY_DETAILS;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return getMachine(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        MEComputationArrayMachine machine = getMachine(accessor);
        if (machine == null || !machine.isFormed()) {
            return;
        }
        CompoundTag telemetry = new CompoundTag();
        telemetry.putLong(CURRENT_CWUT, machine.getAvailableCwut());
        telemetry.putLong(MAXIMUM_CWUT, machine.getMaximumCwut());
        telemetry.putLong(EU_DEMAND, machine.getEuDemandPerTick());
        telemetry.putLong(RELAY_EUT, machine.getCurrentRelayEuPerTick());
        telemetry.putInt(CORES, machine.getCoreCount());
        telemetry.putInt(RELAYS, machine.getRelayCount());
        telemetry.putDouble(STORED_POWER_EU, machine.getStoredPowerEu());
        telemetry.putDouble(MAXIMUM_STORED_POWER_EU, machine.getMaximumStoredPowerEu());
        telemetry.putBoolean(UPLINK_ONLINE, machine.isUplinkOnline());
        data.put(DATA, telemetry);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (!serverData.contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag telemetry = serverData.getCompound(DATA);
        tooltip.add(Component.translatable(
                "cosmiccore.jade.me_computation_array.components",
                value(telemetry.getInt(CORES), ChatFormatting.AQUA),
                value(telemetry.getInt(RELAYS), ChatFormatting.GOLD)));
        tooltip.add(Component.translatable(
                "cosmiccore.jade.me_computation_array.compute",
                value(FormattingUtil.formatNumbers(telemetry.getLong(CURRENT_CWUT)), ChatFormatting.GREEN),
                value(FormattingUtil.formatNumbers(telemetry.getLong(MAXIMUM_CWUT)), ChatFormatting.AQUA)));
        tooltip.add(Component.translatable(
                "cosmiccore.jade.me_computation_array.energy",
                value(FormattingUtil.formatNumbers(telemetry.getLong(EU_DEMAND)), ChatFormatting.YELLOW)));
        tooltip.add(Component.translatable(
                "cosmiccore.jade.me_computation_array.relay",
                value(FormattingUtil.formatNumbers(telemetry.getLong(RELAY_EUT)), ChatFormatting.YELLOW),
                value(FormattingUtil.formatNumbers(
                        (long) telemetry.getInt(RELAYS) * MEComputationArrayTuning.RELAY_EU_PER_TICK),
                        ChatFormatting.GOLD)));
        Component uplinkStatus = Component.translatable(
                telemetry.getBoolean(UPLINK_ONLINE) ?
                        "cosmiccore.jade.me_computation_array.uplink.online" :
                        "cosmiccore.jade.me_computation_array.uplink.offline")
                .withStyle(telemetry.getBoolean(UPLINK_ONLINE) ? ChatFormatting.GREEN : ChatFormatting.RED);
        tooltip.add(Component.translatable(
                "cosmiccore.jade.me_computation_array.buffer",
                value(FormattingUtil.formatNumber2Places(telemetry.getDouble(STORED_POWER_EU)), ChatFormatting.AQUA),
                value(FormattingUtil.formatNumber2Places(telemetry.getDouble(MAXIMUM_STORED_POWER_EU)),
                        ChatFormatting.DARK_AQUA),
                uplinkStatus));
    }

    private static MEComputationArrayMachine getMachine(BlockAccessor accessor) {
        MetaMachine machine = MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
        return machine instanceof MEComputationArrayMachine computationArray ? computationArray : null;
    }

    private static Component value(Object value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }
}
