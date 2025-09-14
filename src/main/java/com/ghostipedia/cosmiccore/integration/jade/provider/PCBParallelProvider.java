package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class PCBParallelProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
        if (blockAccessor.getServerData().contains("parallelCosmic")) {
            int parallel = blockAccessor.getServerData().getInt("parallelCosmic");
            if (parallel > 1) {
                Component parallels = Component.literal(FormattingUtil.formatNumbers(parallel))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                String key = "gtceu.multiblock.parallel";
                if (blockAccessor.getServerData().getBoolean("exact")) key += ".exact";
                iTooltip.add(Component.translatable(key, parallels));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getMetaMachine() instanceof IParallelHatch parallelHatch) {
                if (parallelHatch instanceof ParallelHatchPartMachine multiParallelHatch) {
                    if (multiParallelHatch.getControllers().size() == 1) {
                        if (multiParallelHatch.getControllers().first() instanceof PCBFoundryMachine multiContoller) {
                            compoundTag.putInt("parallelCosmic", parallelHatch.getCurrentParallel() * 4);
                        }
                    }
                }
            } else if (blockEntity.getMetaMachine() instanceof IMultiController controller &&
                    controller instanceof PCBFoundryMachine foundryMachine) {
                        if (foundryMachine.getRecipeLogic().isActive() &&
                                foundryMachine.getRecipeLogic().getLastRecipe() != null) {
                            compoundTag.putInt("parallelCosmic",
                                    foundryMachine.getRecipeLogic().getLastRecipe().parallels);
                            compoundTag.putBoolean("exact", true);
                        } else {
                            controller.getParallelHatch()
                                    .ifPresent(parallelHatch -> compoundTag.putInt("parallelCosmic",
                                            parallelHatch.getCurrentParallel() * 4));
                        }
                    }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return CosmicCore.id("parallel_info_cc");
    }
}
