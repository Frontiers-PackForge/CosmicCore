package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.power.DefaultEmberCapability;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableEmberContainer extends NotifiableRecipeHandlerTrait<Double> {

    public static final MachineTraitType<NotifiableEmberContainer> TYPE = new MachineTraitType<>(
            NotifiableEmberContainer.class);

    @Override
    public MachineTraitType<?> getTraitType() {
        return TYPE;
    }

    private EmberHatchPartMachine emberHatch;
    public IEmberCapability capability = new DefaultEmberCapability() {

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            emberHatch.cachedEmber = getEmber();
            emberHatch.cachedEmberCapacity = NotifiableEmberContainer.this.maxCapacity;
            if (!emberHatch.isRemote()) {
                emberHatch.getSyncDataHolder().markClientSyncFieldDirty("cachedEmber");
                emberHatch.getSyncDataHolder().markClientSyncFieldDirty("cachedEmberCapacity");
            }
            notifyListeners();
            NotifiableEmberContainer.this.notifyListeners();
        }

        /*
         * @Override
         * public double getEmber() {
         * return getTotalContentAmount();
         * }
         * 
         * @Override
         * public double addAmount(double value, boolean doAdd) {
         * return super.addAmount(value, doAdd);
         * }
         */
    };

    private final IO handlerIO;

    @SaveField
    @Getter
    private double maxCapacity;

    @SaveField
    @Getter
    private double maxConsumption;

    public NotifiableEmberContainer(MetaMachine machine, IO io, double maxCapacity, double maxConsumption) {
        super();
        this.emberHatch = (EmberHatchPartMachine) machine;
        this.capability.setEmberCapacity(maxCapacity);
        this.capability.setEmber(0.0D);
        this.handlerIO = io;
        this.maxCapacity = maxCapacity;
        this.maxConsumption = maxConsumption;
        // 8.0.0: NotifiableRecipeHandlerTrait no longer takes the machine in its ctor; attach explicitly so
        // getMachine()/capability registration are wired (mirrors EnergyHatchPartMachine#attachTrait).
        machine.attachTrait(this);
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (!emberHatch.isRemote()) {
            capability.setEmber(emberHatch.cachedEmber);
        }
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
        return ember <= 0 ? Collections.emptyList() : Collections.singletonList(ember);
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
        return EmberRecipeCapability.CAP;
    }

    @Override
    public int getSize() {
        return 1;
    }
}
