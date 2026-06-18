package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.PCBFoundryMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public class PCBParallelProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final ResourceLocation UID = CosmicCore.id("pcb_parallel");
    private static final String DATA_KEY = UID.toString();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachine be)) return;
        var machine = be;

        if (machine instanceof IParallelHatch hatch &&
                hatch instanceof ParallelHatchPartMachine part &&
                part.getControllers().size() == 1 &&
                part.getControllers().first() instanceof PCBFoundryMachine) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("parallel", hatch.getCurrentParallel() * 4);
            data.put(DATA_KEY, tag);
            return;
        }

        if (machine instanceof PCBFoundryMachine foundry && machine instanceof MultiblockControllerMachine controller) {
            CompoundTag tag = new CompoundTag();
            var logic = foundry.getRecipeLogic();
            if (logic.isActive() && logic.getLastRecipe() != null) {
                tag.putInt("parallel", logic.getLastRecipe().parallels);
                tag.putBoolean("exact", true);
            } else {
                controller.getParallelHatch().ifPresent(h -> tag.putInt("parallel", h.getCurrentParallel() * 4));
            }
            if (tag.contains("parallel")) data.put(DATA_KEY, tag);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag tag = accessor.getServerData().getCompound(DATA_KEY);
        if (tag.isEmpty() || !tag.contains("parallel")) return;

        int parallel = tag.getInt("parallel");
        if (parallel <= 1) return;

        String key = tag.getBoolean("exact") ? "gtceu.multiblock.parallel.exact" : "gtceu.multiblock.parallel";
        tooltip.add(Component.translatable(key,
                Component.literal(FormattingUtil.formatNumbers(parallel)).withStyle(ChatFormatting.DARK_PURPLE)));
    }
}
