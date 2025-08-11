package com.ghostipedia.cosmiccore.common.machine.multiblock;

import com.ghostipedia.cosmiccore.common.machine.multiblock.part.SterilizationHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class VoraxReactorMachine extends WorkableElectricMultiblockMachine implements IExplosionMachine {

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

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(VoraxReactorMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Nullable
    protected TickableSubscription contagionSubscription;
    @Nullable
    protected EnergyContainerList outputEnergyContainers;

    public VoraxReactorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        List<IEnergyContainer> outputEnergyContainers = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            if (part instanceof SterilizationHatchPartMachine) {
                sterileHatch = (SterilizationHatchPartMachine) part;
            }
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.IN);
            if (io == IO.NONE || io == IO.IN) continue;
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
    public void onStructureInvalid() {
        super.onStructureInvalid();
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

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed) {
            textList.add(Component.translatable("cosmiccore.multiblock.current_contagion",
                    FormattingUtil.formatNumber2Places(contagionStrength)));
            textList.add(Component.translatable("cosmiccore.multiblock.contagion_rate",
                    FormattingUtil.formatNumber2Places(contagionDelta)));
            if (sterileHatch != null && sterileHatch.fluidTank.getFluidInTank(0).getAmount() < 15) {
                textList.add(Component.translatable("cosmiccore.multiblock.cleaning_status.error"));
            } else {
                textList.add(Component.translatable("cosmiccore.multiblock.cleaning_status",
                        isCleaning ? "Cleaning" : "Growing"));
            }

        }
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}
