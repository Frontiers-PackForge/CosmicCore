package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PCBFoundryMachine extends WorkableElectricMultiblockMachine implements IDisplayUIMachine {

    public PCBFoundryMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        int numParallels;
        int batchParallels;
        boolean exact;
        if (recipeLogic.isActive() && recipeLogic.getLastRecipe() != null) {
            numParallels = recipeLogic.getLastRecipe().parallels;
            batchParallels = recipeLogic.getLastRecipe().batchParallels;
            exact = true;
        } else {
            exact = false;
            numParallels = getParallelHatch()
                    .map(c -> c.getCurrentParallel() * 4)
                    .orElse(0);
            batchParallels = 0;
        }
        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addMachineModeLine(getRecipeType(), getRecipeTypes().length > 1);
        builder.addCustom((components) -> {
            if (numParallels > 1) {
                Component parallels = Component.literal(FormattingUtil.formatNumbers(numParallels))
                        .withStyle(ChatFormatting.GOLD);
                Component parallelsClassic = Component.literal(FormattingUtil.formatNumbers(numParallels / 4))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                String key = "cosmic.multiblock.parallel";
                if (exact) key += ".exact";
                textList.add(Component.translatable(key, parallels, parallelsClassic)
                        .withStyle(ChatFormatting.GRAY));
            }
        })
                .addBatchModeLine(isBatchEnabled(), batchParallels)
                .addWorkingStatusLine()
                .addProgressLine(recipeLogic.getProgress(), recipeLogic.getMaxProgress(),
                        recipeLogic.getProgressPercent())
                .addOutputLines(recipeLogic.getLastRecipe());
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }
}
