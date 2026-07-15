package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SterilizationHatchPartMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.neoforged.neoforge.fluids.FluidStack;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class VoraxReactorMachine extends WorkableElectricMultiblockMachine {

    @DescSynced
    @Getter
    @Persisted
    private float contagionDelta = 30;

    @DescSynced
    @Getter
    @Persisted
    private float contagionStrength = 0;

    @DescSynced
    @Getter
    @Persisted
    private boolean isCleaning = true;

    private SterilizationHatchPartMachine sterileHatch = null;

    @Nullable
    protected TickableSubscription contagionSubscription;
    @Nullable
    protected EnergyContainerList outputEnergyContainers;

    public VoraxReactorMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }

    @Override
    public void formStructure(@org.jetbrains.annotations.NotNull String substructureName) {
        super.formStructure(substructureName);

        List<IEnergyContainer> outputEnergyContainers = new ArrayList<>();
        // 8.0.0: getMatchContext()/ioMap removed; the handler.getHandlerIO() check below already filters IO.
        for (MultiblockPartMachine part : getParts()) {
            if (part instanceof SterilizationHatchPartMachine) {
                sterileHatch = (SterilizationHatchPartMachine) part;
            }
            var handlers = part.getRecipeHandlers();
            for (var handler : handlers) {
                IO handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.IN) {
                    var containers = handler.getCapability(EURecipeCapability.CAP).stream()
                            .filter(IEnergyContainer.class::isInstance)
                            .map(IEnergyContainer.class::cast)
                            .toList();
                    outputEnergyContainers.addAll(containers);
                    traitSubscriptions.add(handler.subscribe(this::updateContagionSubs));

                }
            }
        }
        this.outputEnergyContainers = new EnergyContainerList(outputEnergyContainers);
        updateContagionSubs();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && isFormed()) {
            updateContagionSubs();
        }
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        this.outputEnergyContainers = null;
        contagionStrength = 0;
        updateContagionSubs();
    }

    protected void updateContagionSubs() {
        if ((outputEnergyContainers != null)) {
            contagionSubscription = subscribeServerTick(contagionSubscription, this::updateContagion);
        } else if (contagionSubscription != null) {
            contagionSubscription.unsubscribe();
            contagionSubscription = null;
        }
    }

    public void updateContagion() {
        if (recipeLogic.isWorking()) {
            if (!isCleaning && contagionStrength >= 100000) {
                contagionStrength = 100000;
                // recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
                // doExplosion(12f + getTier());
            } else {

                contagionDelta += 0.05F;
                contagionStrength += contagionDelta;
                isCleaning = false;
            }
        }
        if (recipeLogic.isIdle() || recipeLogic.isSuspend() || recipeLogic.isWaiting() || !this.isWorkingEnabled()) {
            if (contagionStrength != 0) {
                if (sterileHatch != null) {
                    FluidStack sterileThingy = sterileHatch.fluidTank.getFluidInTank(0);
                    if (!sterileThingy.isEmpty() && sterileThingy.getAmount() >= 15) {
                        contagionDelta -= 0.5F;
                        sterileThingy.shrink(15);
                        contagionStrength += contagionDelta;
                        isCleaning = true;
                    } else {
                        isCleaning = false;
                    }
                }
            }
        }
        contagionDelta = clamp(contagionDelta, -150, 50);
        contagionStrength = clamp(contagionStrength, 0, 100000);
    }

    @Override
    public boolean beforeWorking(@org.jetbrains.annotations.Nullable GTRecipe recipe) {
        if (contagionDelta <= 0) {
            contagionDelta = 0;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        updateContagion();
        return super.onWorking();
    }

    // TODO(8.0.0 MUI2): addDisplayText (LDLib status readout: contagion strength/rate, cleaning status) was
    // removed in GTCEu 8.0.0. Rebuild on MUI2 when ported; contagionStrength/contagionDelta/sterileHatch preserved.

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}
