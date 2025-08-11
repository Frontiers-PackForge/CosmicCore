package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class SterilizationHatchPartMachine extends TieredIOPartMachine
                                           implements ICleanroomProvider, IRecipeHandler, IUIMachine {

    public final NotifiableFluidTank fluidTank;

    public SterilizationHatchPartMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
        fluidTank = new NotifiableFluidTank(this, 1, 4000, IO.IN, IO.IN);

        fluidTank.setFilter(fluid -> fluid.getFluid() == GTMaterials.Chlorine.getFluid(FluidStorageKeys.PLASMA));
    }

    @Override
    public Set<CleanroomType> getTypes() {
        if (!fluidTank.isEmpty() && fluidTank.getFluidInTank(0).getAmount() > 20) {
            return Set.of(CleanroomType.CLEANROOM, CleanroomType.STERILE_CLEANROOM);
        }
        return Set.of(CleanroomType.CLEANROOM);
    }

    @Override
    public boolean isClean() {
        return true;
    }

    @Override
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(this);
        }
    }

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        if (controller instanceof ICleanroomReceiver receiver) {
            receiver.setCleanroom(null);
        }
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List left, boolean simulate) {
        return this.fluidTank.handleRecipeInner(io, recipe, left, !simulate);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(this.fluidTank.getFluidInTank(0));
    }

    @Override
    public double getTotalContentAmount() {
        return this.fluidTank.getFluidInTank(0).getAmount();
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    // GUI
    @Override
    public ModularUI createUI(Player entityPlayer) {
        var group = new WidgetGroup(0, 0, 176, 164);
        group.addWidget(new LabelWidget(5, 5, "gui.cosmiccore.sterilization_hatch"));
        group.addWidget(new TankWidget(this.fluidTank, 79, 30, true, true)
                .setBackground(GuiTextures.FLUID_SLOT));
        return new ModularUI(176, 164, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(group)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
    }
}
