package com.ghostipedia.cosmiccore.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.DropSaved;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Random;

public class HPCAMachine extends WorkableElectricMultiblockMachine
    implements IOpticalComputationProvider, IMachineLife, IDropSaveMachine {

    private static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HPCAMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int MIN_COMPONENTS_SLICES = 3;
    public static final int MAX_COMPONENTS_SLICES = 15;

    @Persisted
    @DescSynced
    @DropSaved
    private long seed = 0L;

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        IMachineLife.super.onMachinePlaced(player, stack);
        if (seed == 0L) this.seed = GTValues.RNG.nextLong();
    }

    public HPCAMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        return 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        return 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        return false;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private int getIndicatorIndex(BlockPos pos) {
        var index = 0;
        var verticalDelta = Math.abs(pos.getY() - getPos().getY());
        var horizontalDelta = Math.abs(pos.getX() - getPos().getX()) + Math.abs(pos.getZ() - getPos().getZ());
        if (verticalDelta < 4) index = verticalDelta;
        else index =  horizontalDelta + 3;
        return index - 1;
    }

    public int getIndicatorColor(BlockPos pos) {
        var state = new int[MAX_COMPONENTS_SLICES + 3];
        var seededRandom = new Random(this.seed);
        for (int i = 0; i < state.length; i++) state[i] = seededRandom.nextInt(3);
        return state[getIndicatorIndex(pos)];
    }
}
