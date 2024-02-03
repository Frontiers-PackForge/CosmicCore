package com.ghostipedia.cosmiccore.gtbridge.machines.parts;

import com.ghostipedia.cosmiccore.Statics;
import com.ghostipedia.cosmiccore.gtbridge.machines.IPressureContainer;
import com.ghostipedia.cosmiccore.gtbridge.machines.MachineCaps;
import com.ghostipedia.cosmiccore.gtbridge.machines.traits.NotifiablePressureContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PressureHatchPartMachine extends TieredIOPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PressureHatchPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private final NotifiablePressureContainer pressureContainer;
    @Nullable
    protected TickableSubscription updateSubs;

    public PressureHatchPartMachine(IMachineBlockEntity holder, int tier, double minPressure, double maxPressure) {
        super(holder, tier, IO.BOTH);
        this.pressureContainer = new NotifiablePressureContainer(this, minPressure, maxPressure, 1.0D);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updatePressureSubscription));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    public void updatePressureSubscription() {
        if (updateSubs == null) {
            updateSubs = new TickableSubscription(this::updatePressure);
        }
    }

    public void updatePressure() {
        if (!getLevel().isClientSide && getOffsetTimer() % 20 == 0) {
            // vacuum container, needs to increase pressure
            boolean needsPressureIncrease = pressureContainer.getPressure() > pressureContainer.getMinPressure() && this.pressureContainer.getMinPressure() < Statics.DEFAULT_PRESSURE;
            // pressure container, needs to decrease pressure
            boolean needsPressureDecrease = pressureContainer.getPressure() < pressureContainer.getMaxPressure() && this.pressureContainer.getMaxPressure() > Statics.DEFAULT_PRESSURE;
            boolean canChangePressure = needsPressureDecrease || needsPressureIncrease;

            if (canChangePressure) {
                BlockEntity te = getLevel().getBlockEntity(getPos().relative(getFrontFacing()));
                if (te != null) {
                    IPressureContainer container = te.getCapability(MachineCaps.CAPABILITY_PRESSURE_CONTAINER, this.getFrontFacing().getOpposite()).resolve().orElse(null);
                    if (container != null) {
                        IPressureContainer.mergeContainers(false, container, pressureContainer);
                    }
                }
            }

            if (!this.pressureContainer.isPressureSafe()) {
                this.pressureContainer.causePressureExplosion(getLevel(), getPos());
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
