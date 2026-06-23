package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.power.DefaultEmberCapability;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableEmberContainer extends NotifiableRecipeHandlerTrait<Double> {

    public static final MachineTraitType<NotifiableEmberContainer> TYPE = new MachineTraitType<>(
            NotifiableEmberContainer.class);

    private final EmberHatchPartMachine emberHatch;
    private final IO handlerIO;

    @Getter
    private final double maxCapacity;

    @Getter
    private final double maxConsumption;

    public final IEmberCapability capability = new DefaultEmberCapability() {

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            emberHatch.cachedEmber = getEmber();
            NotifiableEmberContainer.this.notifyListeners();
        }
    };

    public NotifiableEmberContainer(MetaMachine machine, IO io, double maxCapacity, double maxConsumption) {
        super(machine);
        this.emberHatch = (EmberHatchPartMachine) machine;
        this.handlerIO = io;
        this.maxCapacity = maxCapacity;
        this.maxConsumption = maxConsumption;
        this.capability.setEmberCapacity(maxCapacity);
        this.capability.setEmber(0.0D);
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        capability.setEmberCapacity(maxCapacity);
        capability.setEmber(emberHatch.cachedEmber);
    }

    @Override
    public MachineTraitType<NotifiableEmberContainer> getTraitType() {
        return TYPE;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public List<Double> handleRecipeInner(IO io, GTRecipe recipe, List<Double> left, boolean simulate) {
        double ember = left.stream().reduce(0.0D, Double::sum);
        if (io == IO.IN) {
            var canOutput = Math.min(maxConsumption, capability.getEmber());
            if (!simulate) ember = capability.removeAmount(Math.min(canOutput, ember), true);
            ember -= canOutput;
        } else if (io == IO.OUT) {
            var canInput = maxCapacity - capability.getEmber();
            if (canInput <= 0) return Collections.singletonList(ember);
            if (!simulate) ember = capability.addAmount(Math.min(canInput, ember), true);
            ember -= canInput;
        }
        return ember <= 0 ? null : Collections.singletonList(ember);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(capability.getEmber());
    }

    @Override
    public double getTotalContentAmount() {
        return capability.getEmber();
    }

    @Override
    public RecipeCapability<Double> getCapability() {
        return CosmicRecipeCapabilities.EMBER;
    }

    @Override
    public int getSize() {
        return 1;
    }
}
