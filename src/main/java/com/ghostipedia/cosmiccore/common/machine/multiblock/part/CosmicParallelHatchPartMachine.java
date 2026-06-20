package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.util.Mth;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class CosmicParallelHatchPartMachine extends TieredPartMachine implements IFancyUIMachine {

    private static final int MIN_PARALLEL = 1;

    private final int maxParallel;

    @Persisted
    private int currentParallel;

    public CosmicParallelHatchPartMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier);
        this.maxParallel = (int) Math.pow(4, tier - GTValues.EV);
    }

    public int getCurrentParallel() {
        return currentParallel;
    }

    public void setCurrentParallel(int parallelAmount) {
        this.currentParallel = Mth.clamp(parallelAmount, MIN_PARALLEL, this.maxParallel);
        for (MultiblockControllerMachine controller : this.getControllers()) {
            if (controller instanceof IRecipeLogicMachine rlm) {
                rlm.getRecipeLogic().markLastRecipeDirty();
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup parallelAmountGroup = new WidgetGroup(0, 0, 100, 20);
        parallelAmountGroup.addWidget(new IntInputWidget(this::getCurrentParallel, this::setCurrentParallel)
                .setMin(MIN_PARALLEL)
                .setMax(maxParallel));

        return parallelAmountGroup;
    }

}
