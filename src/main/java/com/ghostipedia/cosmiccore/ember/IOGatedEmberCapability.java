package com.ghostipedia.cosmiccore.ember;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.compat.legacy.LazyOptional;
import com.rekindled.embers.compat.legacy.capabilities.Capability;

public class IOGatedEmberCapability implements IEmberCapability {

    private final IEmberCapability delegate;
    private final IO io;

    public IOGatedEmberCapability(IEmberCapability delegate, IO io) {
        this.delegate = delegate;
        this.io = io;
    }

    @Override
    public double getEmber() {
        return delegate.getEmber();
    }

    @Override
    public double getEmberCapacity() {
        return delegate.getEmberCapacity();
    }

    @Override
    public void setEmber(double value) {
        delegate.setEmber(value);
    }

    @Override
    public void setEmberCapacity(double value) {
        delegate.setEmberCapacity(value);
    }

    @Override
    public double addAmount(double value, boolean doAdd) {
        if (io == IO.OUT) return 0;
        return delegate.addAmount(value, doAdd);
    }

    @Override
    public double removeAmount(double value, boolean doRemove) {
        if (io == IO.IN) return 0;
        return delegate.removeAmount(value, doRemove);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        delegate.writeToNBT(tag);
    }

    @Override
    public void onContentsChanged() {
        delegate.onContentsChanged();
    }

    @Override
    public void invalidate() {
        delegate.invalidate();
    }

    @Override
    public boolean acceptsVolatile() {
        return delegate.acceptsVolatile();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        return delegate.getCapability(capability, facing);
    }

    @Override
    public CompoundTag serializeNBT() {
        return delegate.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        delegate.deserializeNBT(tag);
    }
}
