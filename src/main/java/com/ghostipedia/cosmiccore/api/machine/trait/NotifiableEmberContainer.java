package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.EmberRecipeCapability;
import com.ghostipedia.cosmiccore.api.machine.activity.ActivityScope;
import com.ghostipedia.cosmiccore.common.machine.multiblock.part.EmberHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.power.DefaultEmberCapability;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class NotifiableEmberContainer extends NotifiableRecipeHandlerTrait<Double> {

    private EmberHatchPartMachine emberHatch;
    public IEmberCapability capability = new DefaultEmberCapability() {

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
        }

        @Override
        public void setEmber(double value) {
            super.setEmber(value);
            NotifiableEmberContainer.this.syncCachedState();
        }

        @Override
        public void setEmberCapacity(double value) {
            super.setEmberCapacity(value);
            NotifiableEmberContainer.this.syncCachedState();
        }

        @Override
        public double addAmount(double value, boolean doAdd) {
            double added = super.addAmount(value, doAdd);
            if (doAdd) NotifiableEmberContainer.this.syncCachedState();
            return added;
        }

        @Override
        public double removeAmount(double value, boolean doRemove) {
            double removed = super.removeAmount(value, doRemove);
            if (doRemove) NotifiableEmberContainer.this.syncCachedState();
            return removed;
        }
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

    private void syncCachedState() {
        emberHatch.cachedEmber = capability.getEmber();
        emberHatch.cachedEmberCapacity = capability.getEmberCapacity();
        if (!emberHatch.isRemote()) {
            emberHatch.getSyncDataHolder().markClientSyncFieldDirty("cachedEmber");
            emberHatch.getSyncDataHolder().markClientSyncFieldDirty("cachedEmberCapacity");
        }
        notifyListeners();
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
        double before = capability.getEmber();
        double requested = left.stream().reduce(0.0D, Double::sum);
        double transferable = 0;
        if (io == IO.IN) {
            transferable = Math.min(maxConsumption, capability.getEmber());
            if (!simulate) capability.removeAmount(Math.min(transferable, requested), true);
        } else if (io == IO.OUT) {
            transferable = Math.max(0, maxCapacity - capability.getEmber());
            if (!simulate) capability.addAmount(Math.min(transferable, requested), true);
        }
        recordDelta(before, capability.getEmber(), simulate);
        double remainder = transferRemainder(requested, transferable);
        return remainder <= 0 ? Collections.emptyList() : Collections.singletonList(remainder);
    }

    static double transferRemainder(double requested, double transferable) {
        return requested - Math.min(requested, Math.max(0, transferable));
    }

    static void recordDelta(double before, double after, boolean simulate) {
        if (simulate) return;
        double delta = after - before;
        if (delta < 0) ActivityScope.ember(-delta, true);
        else if (delta > 0) ActivityScope.ember(delta, false);
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
