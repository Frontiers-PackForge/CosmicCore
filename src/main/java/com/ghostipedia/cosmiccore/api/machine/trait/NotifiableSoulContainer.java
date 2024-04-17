package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;

import java.util.Collections;
import java.util.List;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<Integer> {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableSoulContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    public final IO handlerIO;

    @Setter
    protected SoulNetwork cachedNetwork;

    public NotifiableSoulContainer(MetaMachine machine,IO io) {
        super(machine);
        this.handlerIO = io;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public IO getHandlerIO() {
        return this.handlerIO;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        if (cachedNetwork == null) return null;
        int sum = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            if (!simulate) cachedNetwork.syphon(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), sum), false);
            sum = 0;
        } else if (io == IO.OUT) {
            if (!simulate) cachedNetwork.add(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), sum), Integer.MAX_VALUE);
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public List<Object> getContents() {
        return null;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SoulRecipeCapability.CAP;
    }

    public boolean hasNetwork() {
        return this.cachedNetwork != null;
    }
}
