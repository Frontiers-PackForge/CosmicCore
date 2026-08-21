package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.common.power.telemetry.CablePowerTelemetry;
import com.ghostipedia.cosmiccore.integration.jade.CosmicJadeFormatting;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.block.CableBlock;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.integration.jade.provider.CableBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

@Mixin(value = CableBlockProvider.class, remap = false)
public abstract class CableBlockProviderTelemetryMixin {

    private static final String UID = "gtceu:cable_info";
    private static final String CABLE_DATA = "cableData";

    @Inject(
            method = "appendServerData(Lnet/minecraft/nbt/CompoundTag;Lsnownee/jade/api/BlockAccessor;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$writePowerTelemetry(CompoundTag data, BlockAccessor accessor, CallbackInfo ci) {
        CompoundTag providerData = new CompoundTag();
        if (accessor.getBlock() instanceof CableBlock cableBlock) {
            CableBlockEntity cable = (CableBlockEntity) cableBlock.getPipeTile(
                    accessor.getLevel(), accessor.getPosition());
            if (cable != null) {
                CablePowerTelemetry telemetry = (CablePowerTelemetry) cable;
                CompoundTag cableData = new CompoundTag();
                cableData.putLong("maxVoltage", cable.getMaxVoltage());
                cableData.putLong("currentVoltage", cable.getCurrentMaxVoltage());
                cableData.putLong("maxAmperage", cable.getMaxAmperage());
                cableData.putDouble("currentAmperage", cable.getAverageAmperage());
                cableData.putDouble("averageEuPerTick", cable.getAverageVoltage());
                cableData.putInt("temperature", cable.getTemperature());
                cableData.putInt("overloadCause", telemetry.cosmiccore$getOverloadCause());
                providerData.put(CABLE_DATA, cableData);
            }
        }
        data.put(UID, providerData);
        ci.cancel();
    }

    @Inject(
            method = "appendTooltip(Lsnownee/jade/api/ITooltip;Lsnownee/jade/api/BlockAccessor;Lsnownee/jade/api/config/IPluginConfig;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$showPowerTelemetry(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config,
                                               CallbackInfo ci) {
        CompoundTag providerData = accessor.getServerData().getCompound(UID);
        if (!providerData.contains(CABLE_DATA, Tag.TAG_COMPOUND)) {
            ci.cancel();
            return;
        }

        CompoundTag data = providerData.getCompound(CABLE_DATA);
        long maxVoltage = data.getLong("maxVoltage");
        long maxAmperage = data.getLong("maxAmperage");
        long currentVoltage = data.getLong("currentVoltage");
        double currentAmperage = data.getDouble("currentAmperage");
        double averageEuPerTick = data.getDouble("averageEuPerTick");
        int temperature = data.getInt("temperature");
        int overloadCause = data.getInt("overloadCause");
        boolean shift = GTUtil.isShiftDown();

        tooltip.add(Component.translatable(
                "cosmiccore.jade.power.cable_rating",
                value(FormattingUtil.formatNumbers(maxVoltage), ChatFormatting.GREEN),
                Component.literal(tierName(maxVoltage)),
                value(FormattingUtil.formatNumbers(maxAmperage), ChatFormatting.YELLOW)));

        if (currentVoltage > 0) {
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.power.cable_voltage",
                    value(FormattingUtil.formatNumbers(currentVoltage), ChatFormatting.GREEN),
                    Component.literal(tierName(currentVoltage))));
        } else if (shift) {
            tooltip.add(Component.translatable("cosmiccore.jade.power.cable_voltage_idle")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        if (shift || currentAmperage > 0) {
            double load = maxAmperage <= 0 ? 0 : currentAmperage * 100.0 / maxAmperage;
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.power.cable_load",
                    value(CosmicJadeFormatting.fixedTwoDecimals(currentAmperage), ChatFormatting.YELLOW),
                    value(CosmicJadeFormatting.fixedTwoDecimals(averageEuPerTick), ChatFormatting.YELLOW),
                    value(CosmicJadeFormatting.fixedTwoDecimals(load), ChatFormatting.GREEN)));
        }
        if (shift || temperature > CableBlockEntity.getDefaultTemp()) {
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.power.cable_temperature",
                    value(FormattingUtil.formatNumbers(temperature),
                            temperature > CableBlockEntity.getDefaultTemp() ? ChatFormatting.RED :
                                    ChatFormatting.AQUA)));
        }
        if (temperature > CableBlockEntity.getDefaultTemp()) {
            int progress = Mth.clamp(
                    (100 * (temperature - CableBlockEntity.getDefaultTemp())) /
                            (CableBlockEntity.getMeltTemp() - CableBlockEntity.getDefaultTemp()),
                    0, 100);
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.power.cable_overload",
                    value(FormattingUtil.formatNumbers(progress), ChatFormatting.RED)));
            tooltip.add(Component.translatable(
                    "cosmiccore.jade.power.cable_cause",
                    Component.translatable(causeKey(overloadCause)).withStyle(ChatFormatting.RED)));
        }
        ci.cancel();
    }

    private static String causeKey(int cause) {
        if (cause == (CablePowerTelemetry.OVERAMPERAGE | CablePowerTelemetry.OVERVOLTAGE)) {
            return "cosmiccore.jade.power.cable_cause.both";
        }
        if ((cause & CablePowerTelemetry.OVERAMPERAGE) != 0) {
            return "cosmiccore.jade.power.cable_cause.overamperage";
        }
        if ((cause & CablePowerTelemetry.OVERVOLTAGE) != 0) {
            return "cosmiccore.jade.power.cable_cause.overvoltage";
        }
        return "cosmiccore.jade.power.cable_cause.residual";
    }

    private static Component value(String value, ChatFormatting color) {
        return Component.literal(value).withStyle(color);
    }

    private static String tierName(long voltage) {
        int tier = Mth.clamp(GTUtil.getTierByVoltage(voltage), 0, GTValues.VNF.length - 1);
        return GTValues.VNF[tier];
    }
}
