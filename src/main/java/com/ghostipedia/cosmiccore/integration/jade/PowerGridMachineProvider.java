package com.ghostipedia.cosmiccore.integration.jade;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.common.machine.electric.TransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PowerGridMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String DATA = "PowerGridTelemetry";
    private static final String INPUT_VOLTAGE = "InputVoltage";
    private static final String INPUT_AMPERAGE = "InputAmperage";
    private static final String OUTPUT_VOLTAGE = "OutputVoltage";
    private static final String OUTPUT_AMPERAGE = "OutputAmperage";
    private static final String INPUT_PER_SECOND = "InputPerSecond";
    private static final String OUTPUT_PER_SECOND = "OutputPerSecond";
    private static final String FACE_INPUT = "FaceInput";
    private static final String FACE_OUTPUT = "FaceOutput";
    private static final String NATIVE_FACE_RATINGS = "NativeFaceRatings";
    private static final String COMPACT_FACE_RATINGS = "CompactFaceRatings";

    @Override
    public ResourceLocation getUid() {
        return CosmicCoreJadePlugin.POWER_GRID_TELEMETRY;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlock() instanceof MetaMachineBlock && getMachine(accessor) != null;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        MetaMachine machine = getMachine(accessor);
        if (machine == null) {
            return;
        }
        IEnergyContainer container = GTCapabilityHelper.getEnergyContainer(
                accessor.getLevel(), accessor.getPosition(), null);
        if (container == null) {
            return;
        }
        long inputVoltage = container.getInputVoltage();
        long inputAmperage = container.getInputAmperage();
        long outputVoltage = container.getOutputVoltage();
        long outputAmperage = container.getOutputAmperage();
        if (inputVoltage <= 0 && outputVoltage <= 0) {
            return;
        }

        Direction side = accessor.getSide();
        IEnergyContainer faceContainer = side == null ? null : GTCapabilityHelper.getEnergyContainer(
                accessor.getLevel(), accessor.getPosition(), side);

        CompoundTag telemetry = new CompoundTag();
        telemetry.putLong(INPUT_VOLTAGE, inputVoltage);
        telemetry.putLong(INPUT_AMPERAGE, inputAmperage);
        telemetry.putLong(OUTPUT_VOLTAGE, outputVoltage);
        telemetry.putLong(OUTPUT_AMPERAGE, outputAmperage);
        telemetry.putLong(INPUT_PER_SECOND, container.getInputPerSec());
        telemetry.putLong(OUTPUT_PER_SECOND, container.getOutputPerSec());
        telemetry.putBoolean(FACE_INPUT, faceContainer != null && faceContainer.inputsEnergy(side));
        telemetry.putBoolean(FACE_OUTPUT, faceContainer != null && faceContainer.outputsEnergy(side));
        telemetry.putBoolean(NATIVE_FACE_RATINGS,
                machine instanceof DiodePartMachine || machine instanceof TransformerMachine);
        telemetry.putBoolean(COMPACT_FACE_RATINGS, machine instanceof BatteryBufferMachine);
        data.put(DATA, telemetry);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (!serverData.contains(DATA, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag telemetry = serverData.getCompound(DATA);
        long inputVoltage = telemetry.getLong(INPUT_VOLTAGE);
        long inputAmperage = telemetry.getLong(INPUT_AMPERAGE);
        long outputVoltage = telemetry.getLong(OUTPUT_VOLTAGE);
        long outputAmperage = telemetry.getLong(OUTPUT_AMPERAGE);
        long inputPerSecond = telemetry.getLong(INPUT_PER_SECOND);
        long outputPerSecond = telemetry.getLong(OUTPUT_PER_SECOND);
        boolean shift = GTUtil.isShiftDown();
        boolean nativeFaceRatings = telemetry.getBoolean(NATIVE_FACE_RATINGS);
        boolean compactFaceRatings = telemetry.getBoolean(COMPACT_FACE_RATINGS);

        if (!nativeFaceRatings) {
            if (telemetry.getBoolean(FACE_INPUT)) {
                tooltip.add(
                        compactFaceRatings ? compactRating("gtceu.top.transform_input", inputVoltage, inputAmperage) :
                                rating("cosmiccore.jade.power.input_rating", inputVoltage, inputAmperage));
            }
            if (telemetry.getBoolean(FACE_OUTPUT)) {
                tooltip.add(compactFaceRatings ?
                        compactRating("gtceu.top.transform_output", outputVoltage, outputAmperage) :
                        rating("cosmiccore.jade.power.output_rating", outputVoltage, outputAmperage));
            }
            if (shift && !telemetry.getBoolean(FACE_INPUT) && !telemetry.getBoolean(FACE_OUTPUT)) {
                tooltip.add(Component.translatable("cosmiccore.jade.power.face_disconnected")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        if (inputVoltage > 0 && inputAmperage > 0 && (shift || inputPerSecond > 0)) {
            tooltip.add(flow("cosmiccore.jade.power.input_flow", inputPerSecond, inputVoltage));
        }
        if (outputVoltage > 0 && outputAmperage > 0 && (shift || outputPerSecond > 0)) {
            tooltip.add(flow("cosmiccore.jade.power.output_flow", outputPerSecond, outputVoltage));
        }
    }

    private static MetaMachine getMachine(BlockAccessor accessor) {
        return MetaMachine.getMachine(accessor.getLevel(), accessor.getPosition());
    }

    private static Component rating(String key, long voltage, long amperage) {
        return Component.translatable(
                key,
                value(FormattingUtil.formatNumbers(voltage), ChatFormatting.GREEN),
                value(FormattingUtil.formatNumbers(amperage), ChatFormatting.YELLOW));
    }

    private static Component compactRating(String key, long voltage, long amperage) {
        int tier = Mth.clamp(GTUtil.getTierByVoltage(voltage), 0, GTValues.VNF.length - 1);
        return Component.translatable(key, GTValues.VNF[tier] + "\u00A7r (" + amperage + "A)");
    }

    private static Component flow(String key, long euPerSecond, long voltage) {
        double euPerTick = euPerSecond / 20.0;
        double amperage = voltage <= 0 ? 0 : euPerTick / voltage;
        return Component.translatable(
                key,
                value(CosmicJadeFormatting.fixedTwoDecimals(amperage), ChatFormatting.YELLOW),
                value(CosmicJadeFormatting.fixedTwoDecimals(euPerTick), ChatFormatting.RED));
    }

    private static Component value(String value, ChatFormatting color) {
        return Component.literal(value).withStyle(color);
    }
}
