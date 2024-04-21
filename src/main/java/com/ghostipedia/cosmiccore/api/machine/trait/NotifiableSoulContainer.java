package com.ghostipedia.cosmiccore.api.machine.trait;

import com.ghostipedia.cosmiccore.api.capability.recipe.SoulRecipeCapability;
import com.ghostipedia.cosmiccore.api.capability.ISoulContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.util.helper.PlayerHelper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotifiableSoulContainer extends NotifiableRecipeHandlerTrait<Integer> implements ISoulContainer {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableSoulContainer.class, NotifiableRecipeHandlerTrait.MANAGED_FIELD_HOLDER);

    @Getter
    public final IO handlerIO;

    @Getter @Setter
    @Persisted @DescSynced
    public UUID owner;

    public NotifiableSoulContainer(MetaMachine machine,IO io) {
        super(machine);
        this.handlerIO = io;
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, @Nullable String slotName, boolean simulate) {
        ISoulContainer container = this;
        if (container.getOwner() == null) return null;

        int lifeEssence = left.stream().reduce(0, Integer::sum);
        if (io == IO.IN) {
            if (!simulate) lifeEssence = container.getSoulNetwork().syphon(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), lifeEssence), false);
        } else if (io == IO.OUT) {
            if (!simulate) lifeEssence = container.getSoulNetwork().add(SoulTicket.block(this.machine.getLevel(), this.machine.getPos(), lifeEssence), Integer.MAX_VALUE);
        }

        return lifeEssence <= 0 ? null : Collections.singletonList(lifeEssence);
    }

    @Override
    public List<Object> getContents() {
        if (this.owner == null) return Collections.emptyList();
        return List.of(this.getNetwork().getCurrentEssence());
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return SoulRecipeCapability.CAP;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private SoulNetwork getNetwork() {
        return NetworkHelper.getSoulNetwork(this.owner);
    }

    public void buildDisplayInfo(List<net.minecraft.network.chat.Component> textList) {
        if (this.owner != null) {
            textList.add(Component.translatable("gui.cosmiccore.soul_hatch.owner", PlayerHelper.getUsernameFromUUID(this.owner)));
            textList.add(Component.translatable("gui.cosmiccore.soul_hatch.lp", this.getNetwork().getCurrentEssence()));
        } else {
            textList.add(Component.translatable("gui.cosmiccore.soul_hatch.no_network"));
        }
    }

    @Override
    public SoulNetwork getSoulNetwork() {
        return NetworkHelper.getSoulNetwork(this.owner);
    }
}
