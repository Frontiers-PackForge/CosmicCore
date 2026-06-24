package com.ghostipedia.cosmiccore.common.machine.multiblock.part;

import com.ghostipedia.cosmiccore.api.capability.recipe.SterileRecipeCapability;
import com.ghostipedia.cosmiccore.api.machine.trait.NotifiableSterileTank;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SterilizationHatchPartMachine extends TieredIOPartMachine
                                           implements IRecipeHandler {

    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription tankSubs;
    public final NotifiableFluidTank fluidTank;

    public SterilizationHatchPartMachine(BlockEntityCreationInfo holder, int tier, IO io, int tankSize) {
        super(holder, tier, io);
        fluidTank = new NotifiableSterileTank(this, 1, tankSize, IO.IN, IO.IN);
    }

    @Override
    public List<SizedFluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List left, boolean simulate) {
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
    public RecipeCapability<SizedFluidIngredient> getCapability() {
        return SterileRecipeCapability.CAP;
    }

    @Override
    public void onNeighborChanged(net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateTankSubscription();
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        updateTankSubscription(newFacing);
    }

    protected void updateTankSubscription() {
        updateTankSubscription(getFrontFacing());
    }

    protected void updateTankSubscription(Direction newFacing) {
        if (isWorkingEnabled() && ((io.support(IO.OUT) && !fluidTank.isEmpty()) || io.support(IO.IN)) &&
                GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getBlockPos(), newFacing)) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            if (isWorkingEnabled()) {
                if (io == IO.OUT) {
                    fluidTank.exportToNearby(getFrontFacing());
                } else if (io == IO.IN) {
                    fluidTank.importFromNearby(getFrontFacing());
                } else if (io == IO.BOTH) {
                    fluidTank.importFromNearby(getFrontFacing());
                    fluidTank.exportToNearby(getFrontFacing().getOpposite());
                }
            }
            updateTankSubscription();
        }
    }

    // TODO(8.0.0 MUI2): the LDLib IUIMachine createUI (fluid tank readout) was removed in GTCEu 8.0.0.
    //  Rebuild on IUIMachine/buildUI (MUI2) when the sterilization UI is ported; fluidTank supplies the data.
}
